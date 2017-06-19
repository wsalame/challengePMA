package me.amp.challenge.elasticsearch;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.amp.challenge.model.PropertyLoader;
import me.amp.challenge.model.fields.ManagerField;
import me.amp.challenge.model.fields.PartyField;

@Singleton
public class ElasticsearchReadController extends AbstractElasticsearchReadController {

	private final String separator = ",";

	@Inject
	public ElasticsearchReadController(PropertyLoader propertyLoader) {
		this.propertyLoader = propertyLoader;
	}

	@Override
	public String findClosestManagers(String index, double latitude, double longitude, int nbOfManagers) {
		String fieldName = ManagerField.LOCATION.getJsonFieldName();
		QueryBuilder qb = QueryBuilders.matchAllQuery();

		SearchRequestBuilder requestBuilder = getClient().prepareSearch(index).setQuery(qb);

		SortBuilder sb = SortBuilders.geoDistanceSort(fieldName).point(latitude, longitude)
		        .unit(DistanceUnit.KILOMETERS).order(SortOrder.ASC);

		requestBuilder = requestBuilder.addSort(sb).setSize(nbOfManagers)
		        .setFetchSource(ManagerField.HOSTNAME.getJsonFieldName(), null);

		SearchResponse response = requestBuilder.execute().actionGet();

		return joinSourceHits(response, separator, 2);
	}

	@Override
	public String getParticipants(String index, String type, String id) {
		String[] fieldNames = { PartyField.GUESTS.getJsonFieldName(), PartyField.PRIMARY_DEVICE_ID.getJsonFieldName() };
		GetResponse response = getClient().prepareGet(index, type, id).setFetchSource(fieldNames, null).execute()
		        .actionGet();

		return response.getSourceAsString();
	}

	@Override
	public String findClosestParties(String index, double latitude, double longitude, int nbOfParties) {
		String fieldName = PartyField.PRIMARY_HOST_USER_LOCATION.getJsonFieldName();
		QueryBuilder qb = QueryBuilders.matchAllQuery();

		SearchRequestBuilder requestBuilder = getClient().prepareSearch(index).setQuery(qb);

		SortBuilder sb = SortBuilders.geoDistanceSort(fieldName).point(latitude, longitude)
		        .unit(DistanceUnit.KILOMETERS).order(SortOrder.ASC);

		String[] fieldNames = { PartyField.PRIMARY_DEVICE_ID.getJsonFieldName(),
		        PartyField.MANAGER_HOSTNAME.getJsonFieldName() };

		requestBuilder = requestBuilder.addSort(sb).setSize(nbOfParties).setFetchSource(fieldNames, null);

		SearchResponse response = requestBuilder.execute().actionGet();

		return joinSourceAndSortHits(response, separator, 300);
	}
}