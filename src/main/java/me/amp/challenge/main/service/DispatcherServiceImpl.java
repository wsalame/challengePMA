package me.amp.challenge.main.service;

import static spark.Spark.get;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import me.amp.challenge.main.PropertyConstant;
import me.amp.challenge.model.DispatcherService;
import me.amp.challenge.model.IsConnected;
import me.amp.challenge.model.PropertyLoader;
import spark.Spark;

public class DispatcherServiceImpl implements DispatcherService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private PropertyLoader propertyLoader;

	@Inject
	public DispatcherServiceImpl(PropertyLoader propertyLoader) {
		this.propertyLoader = propertyLoader;
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
		Spark.awaitInitialization();
		// TODO make it available in the cluster, inserer dans elasticsearch

		logger.info(String.format("%s is ready on %s:%s, args)", this.getClass().getSimpleName(), "localhost",
		        Spark.port()));
	}

	@Override
	public void start() throws IOException, TimeoutException {
		Spark.ipAddress(propertyLoader.getProperty(PropertyConstant.DISPATCHER_HOST));
		Spark.port(propertyLoader.getPropertyAsInteger(PropertyConstant.DISPATCHER_PORT));
	}

	private void initFaviconIcon() {
		get("/favicon.ico", (request, response) -> {
			return "";
		});
	}
}
