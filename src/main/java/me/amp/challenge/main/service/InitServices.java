package me.amp.challenge.main.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import me.amp.challenge.model.DataIndexer;
import me.amp.challenge.model.DataRetriever;
import me.amp.challenge.model.DispatcherService;
import me.amp.challenge.model.IsConnected;
import me.amp.challenge.model.Party;
import me.amp.challenge.model.PartyManagerHealthMonitor;
import me.amp.challenge.model.PartyManagerService;

public class InitServices {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private PartyManagerService partyManagerService;
	private DispatcherService dispatcherService;
	private DataIndexer dataIndexer;
	private PartyManagerHealthMonitor partyManagerDispatcherHealthMonitor;
	private DataRetriever dataRetriever;

	@Inject
	public InitServices(DispatcherService dispatcherService, PartyManagerService partyManagerService,
	        DataIndexer dataIndexer, PartyManagerHealthMonitor partyManagerDispatcherHealthMonitor, DataRetriever dataRetriever) {
		this.dispatcherService = dispatcherService;
		this.partyManagerService = partyManagerService;
		this.dataIndexer = dataIndexer;
		this.partyManagerDispatcherHealthMonitor = partyManagerDispatcherHealthMonitor;
		this.dataRetriever = dataRetriever;
	}

	public void startServices() throws IOException, ExecutionException {
		List<IsConnected> dependenciesServices = Lists.newArrayList(this.dataIndexer, this.dataRetriever);

		List<IsConnected> failedServices = Collections.synchronizedList(new ArrayList<IsConnected>());
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(4, dependenciesServices.size()));

		dependenciesServices.stream().forEach(service -> executor.submit(() -> {
			try {
				service.start();
				service.awaitInitialization();
				logger.info(service.getClass().getSimpleName() + " was started");
			} catch (Exception e) {
				logger.fatal(service.getClass().getSimpleName() + " could not be started", e);
				failedServices.add(service);
			}
		}));

		executor.shutdown();
		

		try {
			boolean allFinished = executor.awaitTermination(15, TimeUnit.SECONDS) && failedServices.isEmpty();

			if (allFinished) {
				
				
				//TODO cleanup
				try {
					this.partyManagerDispatcherHealthMonitor.start();
					this.partyManagerDispatcherHealthMonitor.awaitInitialization();
					dataIndexer.createIndex(Party.INDEX_NAME, true);

				} catch (Exception e) {
					logger.fatal(partyManagerDispatcherHealthMonitor.getClass().getSimpleName() + " could not be started", e);
					failedServices.add(partyManagerDispatcherHealthMonitor);
				}
				
				//
				
				
				
				this.partyManagerService.start();
				this.dispatcherService.start();

				this.dispatcherService.awaitInitialization();
				this.partyManagerService.awaitInitialization();
			} else {
				throwCouldNotStartServices(failedServices);
			}
		} catch (Exception e) {
			logger.fatal(e, e);
			throwCouldNotStartServices(failedServices);
		}
	}

	private void throwCouldNotStartServices(List<IsConnected> failedServices) {
		System.err.println("Something happened...");
		throw new IllegalStateException(
		        "Could not start some of the services : " + Arrays.toString(failedServices.toArray()));
	}
}
