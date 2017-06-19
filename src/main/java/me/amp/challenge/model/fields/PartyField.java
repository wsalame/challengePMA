package me.amp.challenge.model.fields;

import me.amp.challenge.elasticsearch.model.FieldDatatype;
import me.amp.challenge.elasticsearch.model.IsElasticsearchField;
import me.amp.challenge.model.Party;

public enum PartyField implements IsElasticsearchField<Party> {
	PRIMARY_DEVICE_ID("primary_device_id", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(Party o) {
			return o.getPrimaryDeviceId();
		}
	},
	GUESTS("guests", FieldDatatype.RAW_STRING) {

		@Override
		public Object getIndexingValue(Party o) {
			return o.getGuests();
		}
	},
	MANAGER_HOSTNAME("manager_hostname", FieldDatatype.RAW_STRING) {

		@Override
		public Object getIndexingValue(Party o) {
			return o.getManagerHostname();
		}
	},
	PRIMARY_HOST_USER_LOCATION("primary_host_user_location", FieldDatatype.GEO_POINT) {
		@Override
		public Object getIndexingValue(Party o) {
			// The order of [lon, lat] here is in order to conform with GeoJSON.
			double[] coordinates = { o.getLongitude(), o.getLatitude() };
			return coordinates;
		}
	};

	private final String jsonFieldName;
	private final FieldDatatype fieldDatatype;

	private PartyField(String jsonFieldName, FieldDatatype fieldDatatype) {
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
