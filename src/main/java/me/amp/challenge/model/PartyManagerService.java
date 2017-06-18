package me.amp.challenge.model;

import com.google.inject.ImplementedBy;

import me.amp.challenge.main.service.PartyManagerServiceImpl;

@ImplementedBy(PartyManagerServiceImpl.class)
public interface PartyManagerService extends IsConnected{

}
