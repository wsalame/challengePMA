package me.amp.challenge.main.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.amp.challenge.elasticsearch.exception.DataStoreException;
import me.amp.challenge.elasticsearch.model.IsElasticsearchIndexable;
import me.amp.challenge.model.DataIndexer;
import me.amp.challenge.model.DataRetriever;
import me.amp.challenge.model.JsonFormatter;
import me.amp.challenge.model.PartyManager;
import me.amp.challenge.model.PartyManagerHealthMonitor;

/**
 * In this implementation, it's not really a health monitor, as in it's not going to
 * actually check if every server is up and in good health.
 * 
 * But this monitor will know about about each party manager, by retrieving the info from
 * the config files, and making them available by inserting them in a centralized place
 * (in this case in Elasticsearch)
 *
 */
@Singleton
public class PartyManagerHealthMonitorImpl implements PartyManagerHealthMonitor {

	private JsonFormatter jsonFormatter;
	private DataIndexer dataIndexer;
	private DataRetriever dataRetriever;

	@Inject
	public PartyManagerHealthMonitorImpl(JsonFormatter jsonFormatter, DataIndexer dataIndexer,
	        DataRetriever dataRetriever) {
		this.dataRetriever = dataRetriever;
		this.jsonFormatter = jsonFormatter;
		this.dataIndexer = dataIndexer;
	}

	@Override
	public List<PartyManager> getManagers() {
		return null;
	}

	@Override
	public void addClientShutDownHook() {
		// Nothing to do
	}

	@Override
	public void awaitInitialization() {
		// Nothing to do
	}

	@Override
	public void start() throws Exception {
		URL url = Resources.getResource("managers.json");
		String text = Resources.toString(url, Charsets.UTF_8);

		List<PartyManager> managers = new ArrayList<>();
		Map<String, Object> managersMap = jsonFormatter.toMap(text);
		
		for (Entry<String, Object> entry : managersMap.entrySet()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> coordinates = ((Map<String, Object>) entry.getValue());
			String hostname = entry.getKey();

			managers.add(new PartyManager(hostname, (double) coordinates.get("lon"), (double) coordinates.get("lat")));
		}

		dataIndexer.createIndex(PartyManager.INDEX_NAME, true);

		managers.forEach(manager -> uncheckedIndexDocument(manager));
		
		dataIndexer.refresh(PartyManager.INDEX_NAME);
	}

	private void uncheckedIndexDocument(IsElasticsearchIndexable o) {
		try {
			dataIndexer.indexDocument(o);
		} catch (DataStoreException e) {
			throw new UncheckedExecutionException(e);
		}
	}
}
