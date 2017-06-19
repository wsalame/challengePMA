package me.amp.challenge.main.service;

import static spark.Spark.halt;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import me.amp.challenge.exception.JsonException;
import me.amp.challenge.main.PropertyConstant;
import me.amp.challenge.model.DataIndexer;
import me.amp.challenge.model.DataRetriever;
import me.amp.challenge.model.JsonFormatter;
import me.amp.challenge.model.Manager;
import me.amp.challenge.model.Party;
import me.amp.challenge.model.PartyManagerService;
import me.amp.challenge.model.PropertyLoader;
import me.amp.challenge.model.fields.ManagerField;
import me.amp.challenge.model.fields.PartyField;
import me.amp.challenge.model.fields.ResponseField;
import spark.Request;
import spark.Route;
import spark.Service;
import spark.Spark;

public class PartyManagerServiceImpl implements PartyManagerService {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private PropertyLoader propertyLoader;
	private DataRetriever dataRetriever;
	private DataIndexer dataIndexer;
	private Service service;
	private JsonFormatter jsonFormatter;

	@Inject
	public PartyManagerServiceImpl(PropertyLoader propertyLoader, DataRetriever dataRetriever, DataIndexer dataIndexer,
	        JsonFormatter jsonFormatter) {
		this.propertyLoader = propertyLoader;
		this.dataRetriever = dataRetriever;
		this.dataIndexer = dataIndexer;
		this.jsonFormatter = jsonFormatter;
	}

	@Override
	public void addClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Spark.stop();
				logger.info(String.format("%s was shutdown", this.getClass().getSimpleName()));
			}
		});
	}

	@Override
	public void awaitInitialization() {
		service.awaitInitialization();

		logger.info(String.format("%s is ready on %s:%s", this.getClass().getSimpleName(),
		        propertyLoader.getProperty(PropertyConstant.PARTY_MANAGER_HOST),
		        propertyLoader.getPropertyAsInteger(PropertyConstant.PARTY_MANAGER_PORT)));
	}

	@Override
	public void start() throws IOException, TimeoutException {
		service = Service.ignite().ipAddress(propertyLoader.getProperty(PropertyConstant.PARTY_MANAGER_HOST))
		        .port(propertyLoader.getPropertyAsInteger(PropertyConstant.PARTY_MANAGER_PORT));

		service.path("/parties", () -> {
			service.before((req, res) -> {
				boolean authenticated = true; // TODO OAuth
				if (!authenticated) {
					halt(401, "Please connect");
				}
			});

			// Create a party
			service.post("", createPartyRoute);

			// Join a party
			service.put("/:party_code/participants", joinPartyRoute);
			
			// Leave part
			service.delete("/:party_code", leavePartyRoute);

			// Implement party participants listing in the API
			service.get("/:party_code/participants", partyParticipantsListingRoute);

			// Implement party discovery API (nearby parties)
			service.get("/find", partyDiscovery);
		});

	}

	final Route partyDiscovery = (request, response) -> {
		double latitude = Double.parseDouble(request.queryParams("lat"));
		double longitude = Double.parseDouble(request.queryParams("lon"));
		int nbOfParties = request.queryParams("size") != null ? Integer.parseInt(request.queryParams("size")) : 10;

		String nearbyPartiesJson = dataRetriever.findClosestParties(Party.INDEX_NAME, latitude, longitude, nbOfParties);

		response.status(200);
		response.type("application/json");

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put(ResponseField.STATUS_CODE.getJsonFieldName(), response.status());
		responseMap.put(ResponseField.NEARBY_PARTIES.getJsonFieldName(), nearbyPartiesJson);

		return jsonFormatter.toJson(responseMap);
	};

	final Route partyParticipantsListingRoute = (request, response) -> {
		String participants = dataRetriever.getParticipants(Party.INDEX_NAME, Party.TYPE_NAME,
		        request.params("party_code"));

		response.type("application/json");
		response.status(participants != null ? 200 : 400); // TODO magic numbers

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put(ResponseField.STATUS_CODE.getJsonFieldName(), response.status());
		if (participants != null) {
			responseMap.put(ResponseField.PARTY_CODE.getJsonFieldName(), request.params("party_code"));
			responseMap.put(ResponseField.PARTICIPANTS.getJsonFieldName(), jsonFormatter.toMap(participants));
		}

		return jsonFormatter.toJson(responseMap);
	};

	final Route leavePartyRoute = (request, response) -> {
		String partyCode = request.params("party_code");
		Party party = buildParty(request, getPrimaryDeviceId(partyCode), partyCode);

		Set<String> participants = getGuests(partyCode);

		String leavingParticipant = jsonFormatter.toMap(request.body()).get("device_id").toString();

		party.getGuests().addAll(participants);
		party.getGuests().remove(leavingParticipant);

		dataIndexer.indexDocument(party);

		response.status(200);
		response.type("application/json");

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put(ResponseField.STATUS_CODE.getJsonFieldName(), response.status());

		return jsonFormatter.toJson(responseMap);
	};

	private Set<String> getGuests(String partyCode) throws JsonException {
		String participantsJson = dataRetriever.getParticipants(Party.INDEX_NAME, Party.TYPE_NAME, partyCode);

		@SuppressWarnings("unchecked")
		List<String> guests = (List<String>) (jsonFormatter.toMap(participantsJson)
		        .get(PartyField.GUESTS.getJsonFieldName()));
		return new HashSet<>(guests);
	}

	private String getPrimaryDeviceId(String partyCode) throws JsonException {
		String participantsJson = dataRetriever.getParticipants(Party.INDEX_NAME, Party.TYPE_NAME, partyCode);

		return jsonFormatter.toMap(participantsJson).get(PartyField.PRIMARY_DEVICE_ID.getJsonFieldName()).toString();
	}

	final Route joinPartyRoute = (request, response) -> {
		String partyCode = request.params("party_code");
		Party party = buildParty(request, getPrimaryDeviceId(partyCode), partyCode);

		Set<String> guests = getGuests(partyCode);

		String newParticipant = jsonFormatter.toMap(request.body()).get("device_id").toString();

		try {
			party.getGuests().addAll(guests);
			party.getGuests().add(newParticipant);
		} catch (Exception e) {
			e.printStackTrace();
		}

		dataIndexer.indexDocument(party);

		response.status(200);
		response.type("application/json");

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put(ResponseField.STATUS_CODE.getJsonFieldName(), response.status());
		responseMap.put(ResponseField.PARTY_CODE.getJsonFieldName(), party.getId());
		responseMap.put(ResponseField.PARTICIPANT_ID.getJsonFieldName(), newParticipant);
		responseMap.put(ResponseField.NUMBER_OF_GUESTS.getJsonFieldName(), party.getGuests().size());
		return jsonFormatter.toJson(responseMap);
	};

	final Route createPartyRoute = (request, response) -> {
		Party party = buildParty(request);

		dataIndexer.indexDocument(party);

		response.status(200);
		response.type("application/json");

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put(ResponseField.STATUS_CODE.getJsonFieldName(), response.status());
		responseMap.put(ResponseField.PARTY_CODE.getJsonFieldName(), party.getId());
		responseMap.put(ResponseField.PARTICIPANT_ID.getJsonFieldName(), party.getPrimaryDeviceId());

		return jsonFormatter.toJson(responseMap);
	};

	private Party buildParty(Request request) throws JsonException {
		return buildParty(request, jsonFormatter.toMap(request.body()).get("device_id").toString(), null);
	}

	private Party buildParty(Request request, String primaryDeviceId, String partyCode) throws JsonException {
		double latitude = Double.parseDouble(request.queryParams("lat"));
		double longitude = Double.parseDouble(request.queryParams("lon"));

		String closestManager = jsonFormatter
		        .toMap(dataRetriever.findClosestManagers(Manager.INDEX_NAME, latitude, longitude, 1))
		        .get(ManagerField.HOSTNAME.getJsonFieldName()).toString();

		return partyCode == null
		        ? new Party(primaryDeviceId, new HashSet<String>(), closestManager, latitude, longitude)
		        : new Party(primaryDeviceId, new HashSet<String>(), closestManager, latitude, longitude, partyCode);
	}
}
