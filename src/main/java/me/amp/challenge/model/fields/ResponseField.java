package me.amp.challenge.model.fields;

public enum ResponseField {
	STATUS_CODE("status_code"),
	PARTY_CODE("party_code"),
	PARTICIPANT_ID("participant_id"),
	NUMBER_OF_GUESTS("number_guests"),
	PARTICIPANTS("participants"),
	NEARBY_PARTIES("nearby_parties");

	private final String jsonFieldName;

	private ResponseField(String jsonFieldName) {
		this.jsonFieldName = jsonFieldName;
	}

	public String getJsonFieldName() {
		return jsonFieldName;
	}
}