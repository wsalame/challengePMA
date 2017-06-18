package me.amp.challenge.elasticsearch;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import me.amp.challenge.elasticsearch.model.IsElasticsearchField;
import me.amp.challenge.exception.JsonException;
import me.amp.challenge.main.injector.GuiceInjector;
import me.amp.challenge.model.JsonFormatter;

public class ElasticsearchUtils {
	private static final Logger logger = LogManager.getLogger(ElasticsearchUtils.class);
	private static JsonFormatter jsonFormatter = GuiceInjector.get(JsonFormatter.class);

	public static String buildMappingParametersAsJson(IsElasticsearchField<?>[] values) throws IOException {
		XContentBuilder builder = null;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			builder.startObject("_all").field("enabled", false).endObject();
			builder.startObject("properties");

			for (IsElasticsearchField<?> param : values) {
				builder.startObject(param.getJsonFieldName());
				param.getFieldDatatype().buildMappingField(builder);
				builder.endObject();
			}

			builder.endObject().endObject();
			return builder.string();
		} catch (IOException e) {
			logger.error("Error build JSON mapping", e);
			throw e;
		}
	}

	public static String toJson(Map<String, Object> document) throws JsonException {
		return jsonFormatter.toJson(document);
	}
}
