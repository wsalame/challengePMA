package me.amp.challenge.model.fields;

import me.amp.challenge.elasticsearch.model.FieldDatatype;
import me.amp.challenge.elasticsearch.model.IsElasticsearchField;
import me.amp.challenge.model.PartyManager;

public enum PartyManagerField implements IsElasticsearchField<PartyManager> {
	HOSTNAME("hostname", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(PartyManager o) {
			return o.getHostname();
		}

	},
	LOCATION("location", FieldDatatype.GEO_POINT) {
		@Override
		public Object getIndexingValue(PartyManager o) {
			// The order of [lon, lat] here is in order to conform with GeoJSON.
			double[] coordinates = { o.getLongitude(), o.getLatitude() };
			return coordinates;
		}

	};

	private final String jsonFieldName;
	private final FieldDatatype fieldDatatype;

	private PartyManagerField(String jsonFieldName, FieldDatatype fieldDatatype) {
		this.jsonFieldName = jsonFieldName;
		this.fieldDatatype = fieldDatatype;
	}

	@Override
	public String getJsonFieldName() {
		return jsonFieldName;
	}

	@Override
	public FieldDatatype getFieldDatatype() {
		return fieldDatatype;
	}
}