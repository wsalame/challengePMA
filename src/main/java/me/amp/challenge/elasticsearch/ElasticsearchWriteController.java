package me.amp.challenge.elasticsearch;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.amp.challenge.elasticsearch.exception.DataStoreException;
import me.amp.challenge.elasticsearch.model.IsElasticsearchIndexable;
import me.amp.challenge.model.DataIndexer;
import me.amp.challenge.model.PropertyLoader;

@Singleton
public class ElasticsearchWriteController extends AbstractElasticsearchWriteController implements DataIndexer {

	private final Logger logger = LogManager.getLogger(this.getClass());

	@Inject
	public ElasticsearchWriteController(PropertyLoader propertyLoader) {
		this.propertyLoader = propertyLoader;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void deleteIndex(String indexName) throws DataStoreException {
		if(isExists(indexName)){
			DeleteIndexResponse deleteIndexResponse = getClient().admin().indices().prepareDelete(indexName).execute()
			        .actionGet();
			if (!deleteIndexResponse.isAcknowledged()) {
				logger.error("Could not delete index");
				throw new DataStoreException("Could not delete index");
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void createIndex(String indexName, boolean deleteOldIndexIfExists) throws IOException, DataStoreException {
		boolean isExists = isExists(indexName); // Blocking call
		if (isExists && deleteOldIndexIfExists) {
			deleteIndex(indexName);
		}

		if (!isExists || (isExists && deleteOldIndexIfExists)) {
			XContentBuilder settingsBuilder = null;
			settingsBuilder = XContentFactory.jsonBuilder().startObject();

			/**
			 * Dynamic mapping is disabled to force user to define explicit mapping when
			 * creating new type, which, among others, will reduce risk of bugs. For
			 * example, if we index a list of documents, and the first document happens to
			 * have a NULL value in one of its attribute, ES will decide for us which
			 * datatype it is (String, int, etc.). In other words, ES could pick the wrong
			 * datatype, and subsequent documents will fail indexing.
			 * 
			 * The number of shards and replicas depends on our infrastructure and how big
			 * the data set is. If we have only one node, then it might be not smart to
			 * add replicas. If we add a replica, they will just fight for resources (i.e
			 * : CPU scheduling, memory). Although, there is a point that we might want a
			 * failover solution, if reindexing a whole new shard is complicated and/or
			 * too long. In other words, ideally, we'd have two nodes.
			 */
			settingsBuilder.startObject("index").field("number_of_shards", 1).field("number_of_replicas", 1)
			        .field("mapper.dynamic", false);

			settingsBuilder.endObject();

			CreateIndexRequestBuilder createIndexRequestBuilder = getClient().admin().indices().prepareCreate(indexName)
			        .setSettings(settingsBuilder.string());

			CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
			if (!response.isAcknowledged()) {
				logger.error("Could not create index");
				throw new DataStoreException("Could not create index");
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void indexDocument(IsElasticsearchIndexable indexableObject) throws DataStoreException {
		final String index = indexableObject.buildIndex();
		final String type = indexableObject.buildType();

		indexDocument(index, type, indexableObject);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void indexDocument(String index, String type, IsElasticsearchIndexable indexableObject)
	        throws DataStoreException {
		try {
			putMapping(index, type, indexableObject);
			String documentAsJson = ElasticsearchUtils.toJson(indexableObject.buildDocument());

			// Sending the document asynchronously
			IndexRequestBuilder requestBuilder = getClient().prepareIndex(index, type).setId(indexableObject.getId())
			        .setSource(documentAsJson);
			
			requestBuilder.execute();
		} catch (Exception e) {
			System.out.println("ERROR");
			logger.error("Could not insert document in " + index + "/" + type, e);
			throw new DataStoreException("Could not insert document in " + index + "/" + type);
		}
	}

	private void putMapping(String index, String type, IsElasticsearchIndexable indexableObject) throws IOException {
		String mapping = ElasticsearchUtils.buildMappingParametersAsJson(indexableObject.getFields());

		getClient().admin().indices().preparePutMapping(index).setType(type).setSource(mapping).execute().actionGet();
	}
	
	
	
}
