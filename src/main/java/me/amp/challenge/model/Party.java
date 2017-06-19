package me.amp.challenge.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import me.amp.challenge.elasticsearch.model.IsElasticsearchField;
import me.amp.challenge.elasticsearch.model.IsElasticsearchIndexable;
import me.amp.challenge.model.fields.PartyField;

public class Party implements IsElasticsearchIndexable {
	public static String INDEX_NAME = "parties";
	public static String TYPE_NAME = "parties";

	private final String primaryDeviceId;
	private Set<String> guests;
	private final String managerHostname;
	private final double longitude;
	private final double latitude;
	private final String id;

	public Party(String primaryDeviceId, Set<String> guests, String managerHostname, double latitude,
	        double longitude) {
		this(primaryDeviceId, guests, managerHostname, latitude, longitude, UUID.randomUUID().toString());
	}

	public Party(String primaryDeviceId, Set<String> guests, String managerHostname, double latitude, double longitude,
	        String id) {
		this.primaryDeviceId = primaryDeviceId;
		this.guests = guests;
		this.managerHostname = managerHostname;
		this.longitude = longitude;
		this.latitude = latitude;
		this.id = id;
	}

	public String getPrimaryDeviceId() {
		return primaryDeviceId;
	}

	public Set<String> getGuests() {
		return guests;
	}

	public String getManagerHostname() {
		return managerHostname;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	@Override
	public Map<String, Object> buildDocument() {
		Map<String, Object> document = Arrays.asList(PartyField.values()).stream()
		        .collect(Collectors.toMap(PartyField::getJsonFieldName, f -> f.getIndexingValue(this)));

		return document;
	}

	@Override
	public String buildIndex() {
		return INDEX_NAME;
	}

	@Override
	public String buildType() {
		return TYPE_NAME;
	}

	@Override
	public String getId() {
		return id.toString();
	}

	@Override
	public IsElasticsearchField<?>[] getFields() {
		return PartyField.values();
	}
}