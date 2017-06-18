package me.amp.challenge.elasticsearch.model;

import java.util.Map;

/**
 * Your model should implements this interface if it is intended to be indexed in
 * Elasticsearch. By implementing this interface, your model will know how to build itself
 * to make itself indexable and searchable.
 */
public interface IsElasticsearchIndexable {
	/**
	 * Builds document that will be inserted into an Elasticsearch Index in a map format.
	 * They key represents the field name, and the value can be pretty much any Object
	 * that can be serialized to a JSON format representations. This includes all
	 * primitives types and collections.
	 * 
	 * @return Document to be indexed
	 */
	Map<String, Object> buildDocument();

	/**
	 * Builds the index name where document will be stored or retrieved
	 * 
	 * @see {@link #buildGamesIndex(Game)}
	 * @return Elasticsearch index name where the teams will be stored
	 */
	String buildIndex();

	/**
	 * Builds the type name where document will be stored or retrieved
	 * 
	 * @see {@link #buildIndex()}
	 * @return Elasticsearch type name where the document will be stored
	 */
	String buildType();

	/**
	 * Returns array of fields that will be stored. The number of fields doesn't
	 * necessarily match the number of attributes in the class.
	 * 
	 * @return Fields that will be stored
	 */
	IsElasticsearchField<?>[] getFields();

	/**
	 * Builds ID that will be used for storage. Usage of Java 8's default, just for the
	 * sake of using it. Intend of default in interface is to preserve backward
	 * compatibility of the most common interfaces, while still providing new
	 * functionalities.
	 * 
	 * @return ID that will be used for storage
	 */
	default String getId() {
		return null;
	}
}
