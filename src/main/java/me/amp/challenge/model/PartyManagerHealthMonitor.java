package me.amp.challenge.model;

import java.util.List;

import com.google.inject.ImplementedBy;

import me.amp.challenge.main.service.PartyManagerHealthMonitorImpl;

@ImplementedBy(PartyManagerHealthMonitorImpl.class)
public interface PartyManagerHealthMonitor extends IsConnected {
	List<Manager> getManagers();
}