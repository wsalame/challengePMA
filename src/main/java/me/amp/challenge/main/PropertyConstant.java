package me.amp.challenge.main;

public final class PropertyConstant {
	private PropertyConstant() {
	}

	// Elasticsearch cluster
	public static String ES_HOST = "es.host";
	public static String ES_TRANSPORT_PORT = "es.transport.port";
	public static String ES_NUMBER_SHARDS = "es.numberOfShards";
	public static String ES_NUMBER_REPLICAS = "es.numberOfReplicas";
	public static String ES_MAX_WINDOW_SIZE = "es.windowSize";
	
	// Dispatcher service
	public static String DISPATCHER_HOST = "dispatcher.host";
	public static String DISPATCHER_PORT = "dispatcher.port";
	public static String DISPATCHER_LATITUDE = "dispatcher.latitude";
	public static String DISPATCHER_LONGITUDE = "dispatcher.longitude";
	
	// Party manager service
	public static String PARTY_MANAGER_HOST = "partyManager.host";
	public static String PARTY_MANAGER_PORT = "partyManager.port";
	public static String PARTY_MANAGER_LATITUDE = "dispatcher.latitude";
	public static String PARTY_MANAGER_LONGITUDE = "dispatcher.longitude";
}
