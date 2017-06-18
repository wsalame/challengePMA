package me.amp.challenge.model;

import com.google.inject.ImplementedBy;

import me.amp.challenge.main.service.DispatcherServiceImpl;

@ImplementedBy(DispatcherServiceImpl.class)
public interface DispatcherService extends IsConnected {

}
