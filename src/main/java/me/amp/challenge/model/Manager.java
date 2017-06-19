package me.amp.challenge.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import me.amp.challenge.elasticsearch.model.IsElasticsearchField;
import me.amp.challenge.elasticsearch.model.IsElasticsearchIndexable;
import me.amp.challenge.model.fields.ManagerField;

public class Manager implements IsElasticsearchIndexable {
	public static String INDEX_NAME = "managers";
	public static String TYPE_NAME = "managers";

	private final String hostname;
	private final double longitude;
	private final double latitude;

	public Manager(String hostname, double longitude, double latitude) {
		this.hostname = hostname;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public String getHostname() {
		return hostname;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public Map<String, Object> buildDocument() {
		Map<String, Object> document = Arrays.asList(ManagerField.values()).stream()
		        .collect(Collectors.toMap(ManagerField::getJsonFieldName, f -> f.getIndexingValue(this)));

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
	public String getId(){
		return this.hostname;
	}
	
	@Override
	public String toString() {
		return String.format("%s [lat=%s , lon=%s]", hostname, latitude, longitude);
	}

	@Override
	public IsElasticsearchField<? extends IsElasticsearchIndexable>[] getFields() {
		return ManagerField.values();
	}
}