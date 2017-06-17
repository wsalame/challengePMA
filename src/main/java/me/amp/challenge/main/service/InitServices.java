package me.amp.challenge.main.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import me.amp.challenge.model.DispatcherService;
import me.amp.challenge.model.PartyManagerService;

public class InitServices {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private PartyManagerService partyManagerService;
	private DispatcherService dispatcherService;

	@Inject
	public InitServices(DispatcherService dispatcherService, PartyManagerService partyManagerService) {
		this.dispatcherService = dispatcherService;
		this.partyManagerService = partyManagerService;
	}

	public void startServices() {
		// TODO Auto-generated method stub

	}
}
