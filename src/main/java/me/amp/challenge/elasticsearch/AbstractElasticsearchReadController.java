package me.amp.challenge.elasticsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;

import me.amp.challenge.elasticsearch.exception.DataStoreException;
import me.amp.challenge.elasticsearch.exception.ElasticsearchRetrieveException;
import me.amp.challenge.exception.JsonException;
import me.amp.challenge.main.injector.GuiceInjector;
import me.amp.challenge.model.DataRetriever;
import me.amp.challenge.model.JsonFormatter;

public abstract class AbstractElasticsearchReadController extends AbstractElasticsearchController
        implements DataRetriever {

	private JsonFormatter jsonFormatter = GuiceInjector.get(JsonFormatter.class);

	/**
	 * Joins all the hits separated by the specified separator, into a String
	 * 
	 * @param response
	 *            response containing the hits to extract
	 * @param separator
	 *            String value to use to seperate the hits
	 * @param estimatedTotalCapacity
	 *            Estimation of final total characters
	 * @return All hits separated by the specified separator. Hits are in JSON format. If
	 *         the separator is a comma, the returned value will be in a valid JSON format
	 */
	protected String joinSourceHits(SearchResponse response, String separator, int estimatedTotalCapacity) {
		// To avoid constant resizing of StringBuilder's internal array, we estimate what
		// will be the total number of characters.
		//
		// The current numbers provided in the constants were arbitrary, but we could
		// actually analyze a large set of days and have better averages.
		//
		// Of course, there's the possibility that we will make the array too big from
		// time to time. We'd have to test the function with several values to find the
		// sweet spot of ideal starting capacity
		StringBuilder sb = new StringBuilder(estimatedTotalCapacity);
		Arrays.asList(response.getHits().hits()).stream()
		        .forEach(x -> sb.append(x.getSourceAsString()).append(separator));

		if (response.getHits().hits().length > 0) {
			sb.delete(sb.length() - separator.length(), sb.length());
		}

		return sb.toString();
	}

	protected String joinSourceAndSortHits(SearchResponse response, String separator, int estimatedTotalCapacity) {
		StringBuilder sb = new StringBuilder(estimatedTotalCapacity);

		List<Map<String, Object>> list = new ArrayList<>();
		Arrays.asList(response.getHits().hits()).stream().forEach(x -> {
			try {
				Map<String, Object> map = jsonFormatter.toMap(x.getSourceAsString());
				map.put("distance", x.getSortValues()[0]);
				list.add(map);
			} catch (JsonException e) {
				e.printStackTrace();
			}
		});

		try {
			sb.append(jsonFormatter.toJson(list)).append(separator);
		} catch (JsonException e) {
			e.printStackTrace();
		}

		if (response.getHits().hits().length > 0) {
			sb.delete(sb.length() - separator.length(), sb.length());
		}

		return sb.toString();
	}

	protected SearchResponse executeActionGet(SearchRequestBuilder request) throws DataStoreException {
		SearchResponse response = null;

		try {
			response = request.execute().actionGet();
		} catch (Exception e) {
			throw new ElasticsearchRetrieveException("Error retrieving data", e);
		}

		if ((response.isTerminatedEarly() != null && !response.isTerminatedEarly()) || response.isTimedOut()) {
			throw new ElasticsearchRetrieveException("Error retrieving data");
		}

		return response;
	}
}