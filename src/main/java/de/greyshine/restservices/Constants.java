package de.greyshine.restservices;

public abstract class Constants {
	
	public static final String SERVLETCONTEXT_KEY_MAIN_ARGS = ServerMain.class.getCanonicalName() +".ARGS";
	
	public static final String PROPERTY_$ID = "$id";
	public static final String PROPERTY_$UPDATED = "$updated";
	public static final String PROPERTY_$CREATED = "$created";
	
	public static final String REGEX_COLLECTION_NAME = "[a-zA-Z0-9][a-zA-Z0-9\\-_]*s";
	public static final String REGEX_COLLECTION_ITEM_ID = "[a-zA-Z0-9-]+";
	public static final String REGEX_PROPERTYNAME = "[a-zA-Z0-9]+(_[a-zA-Z0-9]+)*";

	public static final String REGEX_PROPERTYNAME_SORTABLE = "[+-]?"+REGEX_PROPERTYNAME;
}
