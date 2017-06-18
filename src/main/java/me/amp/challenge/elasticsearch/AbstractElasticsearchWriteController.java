package me.amp.challenge.elasticsearch;

public abstract class AbstractElasticsearchWriteController extends AbstractElasticsearchController {

	protected boolean isExists(String index) {
		return getClient().admin().indices().prepareExists(index).execute().actionGet().isExists();
	}

	public void refresh(String... indices) {
		getClient().admin().indices().prepareRefresh(indices).execute().actionGet();
	}
}
