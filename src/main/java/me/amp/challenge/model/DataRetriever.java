package me.amp.challenge.model;

import com.google.inject.ImplementedBy;

import me.amp.challenge.elasticsearch.ElasticsearchReadController;

@ImplementedBy(ElasticsearchReadController.class)
public interface DataRetriever extends IsConnected {
	String findClosestParties(String index, double latitude, double longitude, int nbOfParties);
	
	String findClosestManagers(String index, double latitude, double longitude, int nbOfManagers);

	String getParticipants(String index, String type, String id);
}