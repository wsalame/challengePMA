package me.amp.challenge.model;

import java.util.Map;

import com.google.inject.ImplementedBy;

import me.amp.challenge.exception.JsonException;
import me.amp.challenge.util.DefaultJsonFormatter;

@ImplementedBy(DefaultJsonFormatter.class)
public interface JsonFormatter {
	/**
	 * Serializes any object into a String object in a JSON format
	 * 
	 * @param o
	 *            Java object to be serialized
	 * @return A JSON representation of the object
	 * @see {{@link #toPrettyJson(Object, int)}
	 * @throws JsonException
	 *             If any error occured during the serialization
	 */
	String toJson(Object o) throws JsonException;

	/**
	 * Same as {{@link #toJson(Object)}, but with defined indentation
	 */
	String toPrettyJson(Object o, int indent) throws JsonException;

	/**
	 * Adds indentation to String already in JSON format, also know as pretty JSON
	 * 
	 * @param json
	 *            The ugly JSON
	 * @param indent
	 *            Value of indentation (spaces)
	 * @return A pretty JSON representation of the object
	 * @see {{@link #toJson(Object)}
	 * @throws JsonException
	 *             If any error occured during the serialization
	 */
	String toPrettyJson(String json, int indent) throws JsonException;

	/**
	 * Transform a valid JSON into it's equivalent object in form of a map. If an
	 * attribute is made of another object, there will be nested maps. They name of the
	 * attribute will be key in the map.
	 * 
	 * @param json
	 *            JSON to transform
	 * @return A map containing the values from the original JSON i
	 * @throws JsonException
	 *             If any error occured during the deserialization
	 */
	Map<String, Object> toMap(String json) throws JsonException;
}