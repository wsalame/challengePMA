package me.amp.challenge.model;

import com.google.inject.ImplementedBy;

import me.amp.challenge.elasticsearch.ElasticsearchReadController;

@ImplementedBy(ElasticsearchReadController.class)
public interface DataRetriever extends IsConnected {
	String findClosestManager(String index, double longitude, double latitude);
}