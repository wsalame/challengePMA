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
	
	// Party manager service
	public static String PARTY_MANAGER_HOST = "partyManager.host";
	public static String PARTY_MANAGER_PORT = "partyManager.port";
}
