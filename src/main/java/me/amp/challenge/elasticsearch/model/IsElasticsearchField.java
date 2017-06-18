package me.amp.challenge.elasticsearch.model;

public interface IsElasticsearchField<T extends IsElasticsearchIndexable> {
	Object getIndexingValue(T o);
	
	FieldDatatype getFieldDatatype();
	
	String getJsonFieldName();
}
