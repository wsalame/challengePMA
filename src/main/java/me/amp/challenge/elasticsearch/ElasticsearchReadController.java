package me.amp.challenge.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.amp.challenge.main.PropertyConstant;
import me.amp.challenge.model.PropertyLoader;
import me.amp.challenge.model.fields.PartyManagerField;

@Singleton
public class ElasticsearchReadController extends AbstractElasticsearchReadController {

	private final String separator = ",";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final int DEFAULT_SIZE = 10;
	private final int MAX_WINDOW_SIZE;

	@Inject
	public ElasticsearchReadController(PropertyLoader propertyLoader) {
		this.propertyLoader = propertyLoader;
		this.MAX_WINDOW_SIZE = propertyLoader.getPropertyAsInteger(PropertyConstant.ES_MAX_WINDOW_SIZE);
	}

	@Override
	public String findClosestManager(String index, double latitude, double longitude) {
		String fieldName = PartyManagerField.LOCATION.getJsonFieldName();
		QueryBuilder qb = QueryBuilders.matchAllQuery();

		SearchRequestBuilder requestBuilder = getClient().prepareSearch(index).setQuery(qb);

		SortBuilder sb = SortBuilders.geoDistanceSort(fieldName).point(latitude, longitude)
		        .unit(DistanceUnit.KILOMETERS).order(SortOrder.ASC);

		requestBuilder = requestBuilder.addSort(sb).setSize(1)
		        .setFetchSource(PartyManagerField.HOSTNAME.getJsonFieldName(), null);

		SearchResponse response = requestBuilder.execute().actionGet();

		return joinSourceHits(response, separator, 2);
	}
}