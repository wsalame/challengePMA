package me.amp.challenge.main.service;

import static spark.Spark.halt;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.amp.challenge.main.PropertyConstant;
import me.amp.challenge.model.DataRetriever;
import me.amp.challenge.model.DispatcherService;
import me.amp.challenge.model.Manager;
import me.amp.challenge.model.PropertyLoader;
import spark.Route;
import spark.Service;
import spark.Spark;

@Singleton
public class DispatcherServiceImpl implements DispatcherService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private PropertyLoader propertyLoader;
	private DataRetriever dataRetriever;
	private Service service;

	@Inject
	public DispatcherServiceImpl(PropertyLoader propertyLoader, DataRetriever dataRetriever) {
		this.propertyLoader = propertyLoader;
		this.dataRetriever = dataRetriever;
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
		        propertyLoader.getProperty(PropertyConstant.DISPATCHER_HOST),
		        propertyLoader.getPropertyAsInteger(PropertyConstant.DISPATCHER_PORT)));
	}

	@Override
	public void start() throws IOException, TimeoutException {
		service = Service.ignite().port(propertyLoader.getPropertyAsInteger(PropertyConstant.DISPATCHER_PORT));

		service.before("/managers", (req, res) -> {
			boolean authenticated = true; // TODO OAuth
			if (!authenticated) {
				halt(401, "Please connect");
			}
		});

		service.get("/managers/find", findManagersRoute);
	}

	final Route findManagersRoute = (request, response) -> {
		double lat = Double.parseDouble(request.queryParams("lat"));
		double lon = Double.parseDouble(request.queryParams("lon"));

		String findClosestManager = dataRetriever.findClosestManagers(Manager.INDEX_NAME, lat, lon, 1);
		response.status(200);
		response.type("application/json");
		return findClosestManager;
	};
}
