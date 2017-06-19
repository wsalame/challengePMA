package me.amp.challenge.model.fields;

import me.amp.challenge.elasticsearch.model.FieldDatatype;
import me.amp.challenge.elasticsearch.model.IsElasticsearchField;
import me.amp.challenge.model.Manager;

public enum ManagerField implements IsElasticsearchField<Manager> {
	HOSTNAME("hostname", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(Manager o) {
			return o.getHostname();
		}

	},
	LOCATION("location", FieldDatatype.GEO_POINT) {
		@Override
		public Object getIndexingValue(Manager o) {
			// The order of [lon, lat] here is in order to conform with GeoJSON.
			double[] coordinates = { o.getLongitude(), o.getLatitude() };
			return coordinates;
		}

	};

	private final String jsonFieldName;
	private final FieldDatatype fieldDatatype;

	private ManagerField(String jsonFieldName, FieldDatatype fieldDatatype) {
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