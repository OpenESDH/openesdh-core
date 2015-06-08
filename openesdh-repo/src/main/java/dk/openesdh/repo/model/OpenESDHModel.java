package dk.openesdh.repo.model;

import org.alfresco.service.namespace.*;

/**
 * Created by torben on 15/08/14.
 */
public interface OpenESDHModel {

    public static final String CASE_URI = "http://openesdh.dk/model/case/base/1.0/";
    public static final String CASE_PREFIX = "base";
    public static final String DOC_URI = "http://openesdh.dk/model/document/1.0/";
    public static final String DOC_PREFIX = "doc";
    public static final String OE_URI = "http://openesdh.dk/model/openesdh/1.0/";
    public static final String OE_PREFIX = "oe";
    public static final String TYPE_SIMPLE_NAME = "simple";
    public static final String TYPE_BASE_NAME = "base";
    public static final String CASE_MODEL_NAME = "caseModel";

    public static final String CONTACT_PREFIX = "contact";
    public static final String CONTACT_URI = "http://openesdh.dk/model/contact/1.0/";

    public static final String NOTE_PREFIX = "note";
    public static final String NOTE_URI = "http://openesdh.dk/model/note/1.0/";

    /**
     * Models
     */
    public static final QName DOCUMENT_MODEL = QName.createQName(DOC_URI, "documentModel");
    public static final QName CASE_MODEL = QName.createQName(CASE_URI, CASE_MODEL_NAME);

    /**
     * Types
     */
    public static final QName TYPE_OE_BASE = QName.createQName(OE_URI, TYPE_BASE_NAME);

    public static final QName TYPE_CASE_BASE = QName.createQName(CASE_URI, "case");
    public static final QName TYPE_CASE_SIMPLE = QName.createQName(CASE_URI, TYPE_SIMPLE_NAME);
    public static final QName TYPE_CASE_COMPLAINT = QName.createQName(CASE_URI, "complaint");

    public static final QName TYPE_DOC_BASE = QName.createQName(DOC_URI, TYPE_BASE_NAME);
    public static final QName TYPE_DOC_SIMPLE = QName.createQName(DOC_URI, TYPE_SIMPLE_NAME);

    public static final QName TYPE_DOC_FILE = QName.createQName(DOC_URI, "file");
    public static final QName TYPE_DOC_DIGITAL_FILE = QName.createQName(DOC_URI, "digitalFile");
    public static final QName TYPE_DOC_PHYSICAL_FILE = QName.createQName(DOC_URI, "physicalFile");

    public static final QName TYPE_CONTACT_BASE = QName.createQName(CONTACT_URI, "base");
    public static final QName TYPE_CONTACT_PERSON = QName.createQName(CONTACT_URI, "person");
    public static final QName TYPE_CONTACT_ORGANIZATION = QName.createQName (CONTACT_URI, "organization");

    public static final QName TYPE_NOTE_NOTE = QName.createQName(NOTE_URI, "note");


    /**
     * Aspects
     */
    public static final QName ASPECT_OE_JOURNALIZED = QName.createQName(OE_URI, "journalized");
    public static final QName ASPECT_OE_CASE_ID = QName.createQName(OE_URI, "caseId");

    public static final QName ASPECT_CASE_COUNTER = QName.createQName(CASE_URI, "counter");
    public static final QName ASPECT_DOCUMENT_CONTAINER = QName.createQName(DOC_URI, "documentContainer");

    public static final QName ASPECT_CONTACT_ADDRESS = QName.createQName(CONTACT_URI, "address");

    public static final QName ASPECT_NOTE_NOTABLE = QName.createQName(NOTE_URI, "notable");
    public static final QName ASPECT_DOC_RECORD = QName.createQName(DOC_URI, "record");

    /**
     * Associations
     */
    public static final QName ASSOC_CASE_OWNERS = QName.createQName(CASE_URI, "owners");

    public static final QName ASSOC_DOC_OWNER = QName.createQName(DOC_URI, "owner");
    public static final QName ASSOC_DOC_RESPONSIBLE_PERSON = QName.createQName(DOC_URI, "responsible");
    public static final QName ASSOC_DOC_MAIN = QName.createQName(DOC_URI, "main");
    public static final QName ASSOC_DOC_ATTACHMENTS = QName.createQName(DOC_URI, "attachments");

    public static final QName ASSOC_DOC_CONCERNED_PARTIES = QName.createQName(DOC_URI, "concernedParties");
    public static final QName ASSOC_DOC_CASE_REFERENCES = QName.createQName(DOC_URI, "caseReferences");
    public static final QName ASSOC_DOC_DOCUMENT_REFERENCES = QName.createQName(DOC_URI, "documentReferences");

    public static final QName ASSOC_DOC_FILE_CONTENT = QName.createQName(DOC_URI, "fileContent");
    public static final QName ASSOC_DOC_FILE_POSITION = QName.createQName(DOC_URI, "filePosition");

    public static final QName ASSOC_CONTACT_MEMBERS = QName.createQName(CONTACT_URI, "members");

    public static final QName ASSOC_CONTACT_LOGIN = QName.createQName(CONTACT_URI, "userLogin");

    public static final QName ASSOC_NOTE_NOTES = QName.createQName(NOTE_URI, "notes");

    /**
     * Properties
     */
    public static final QName PROP_OE_ID = QName.createQName(OE_URI, "id");
    public static final QName PROP_OE_STATUS = QName.createQName(OE_URI, "status");
    public static final QName PROP_OE_JOURNALIZED_BY = QName.createQName(OE_URI, "journalizedBy");
    public static final QName PROP_OE_JOURNALIZED_DATE = QName.createQName(OE_URI, "journalizedDate");
    public static final QName PROP_OE_JOURNALKEY = QName.createQName(OE_URI, "journalKey");
    public static final QName PROP_OE_ORIGINAL_OWNER = QName.createQName(OE_URI, "originalOwner");
    public static final QName PROP_OE_CASE_ID = QName.createQName(OE_URI, "caseId");

    public static final QName PROP_CASE_STARTDATE = QName.createQName(CASE_URI, "startDate");
    public static final QName PROP_CASE_ENDDATE = QName.createQName(CASE_URI, "endDate");
    public static final QName PROP_CASE_SUBJECT = QName.createQName(CASE_URI, "subject");//TODO This is a value used for testing

    public static final QName PROP_CASE_UNIQUE_NUMBER = QName.createQName(CASE_URI, "uniqueNumber");


    public static final QName PROP_DOC_ARRIVAL_DATE = QName.createQName(DOC_URI, "arrivalDate");
    public static final QName PROP_DOC_CATEGORY = QName.createQName(DOC_URI, "category");
    public static final QName PROP_DOC_IS_MAIN_ENTRY = QName.createQName(DOC_URI, "isMainEntry");
    public static final QName PROP_DOC_VARIANT = QName.createQName(DOC_URI, "variant");
    public static final QName PROP_DOC_TYPE = QName.createQName(DOC_URI, "type");
    public static final QName PROP_DOC_STATE = QName.createQName(DOC_URI, "state");


    public static final QName PROP_CONTACT_EMAIL = QName.createQName(CONTACT_URI, "email");
    public static final QName PROP_CONTACT_TYPE = QName.createQName (CONTACT_URI, "contactType");

    public static final QName PROP_CONTACT_FIRST_NAME = QName.createQName (CONTACT_URI, "firstName");
    public static final QName PROP_CONTACT_LAST_NAME = QName.createQName (CONTACT_URI, "lastName");
    public static final QName PROP_CONTACT_MIDDLE_NAME = QName.createQName (CONTACT_URI, "middleName");
    public static final QName PROP_CONTACT_CPR_NUMBER = QName.createQName (CONTACT_URI, "cprNumber");

    public static final QName PROP_CONTACT_ORGANIZATION_NAME = QName.createQName (CONTACT_URI, "organizationName");
    public static final QName PROP_CONTACT_CVR_NUMBER = QName.createQName (CONTACT_URI, "cvrNumber");

    public static final QName PROP_CONTACT_ADDRESS = QName.createQName (CONTACT_URI, "address");
    public static final QName PROP_CONTACT_ADDRESS_LINE1 = QName.createQName (CONTACT_URI, "addressLine1");
    public static final QName PROP_CONTACT_ADDRESS_LINE2 = QName.createQName (CONTACT_URI, "addressLine2");
    public static final QName PROP_CONTACT_ADDRESS_LINE3 = QName.createQName (CONTACT_URI, "addressLine3");
    public static final QName PROP_CONTACT_ADDRESS_LINE4 = QName.createQName (CONTACT_URI, "addressLine4");
    public static final QName PROP_CONTACT_ADDRESS_LINE5 = QName.createQName (CONTACT_URI, "addressLine5");
    public static final QName PROP_CONTACT_ADDRESS_LINE6 = QName.createQName (CONTACT_URI, "addressLine6");
    public static final QName PROP_CONTACT_FLOOR_IDENTIFIER = QName.createQName (CONTACT_URI, "floorIdentifier");
    public static final QName PROP_CONTACT_SUITE_IDENTIFIER = QName.createQName (CONTACT_URI, "suiteIdentifier");
    public static final QName PROP_CONTACT_POST_BOX = QName.createQName (CONTACT_URI, "postBox");
    public static final QName PROP_CONTACT_POST_CODE = QName.createQName (CONTACT_URI, "postCode");
    public static final QName PROP_CONTACT_POST_DISTRICT = QName.createQName (CONTACT_URI, "postDistrict");
    public static final QName PROP_CONTACT_COUNTRY_CODE = QName.createQName (CONTACT_URI, "countryCode");
    public static final QName PROP_CONTACT_CITY_NAME = QName.createQName (CONTACT_URI, "cityName");
    public static final QName PROP_CONTACT_STREET_NAME = QName.createQName (CONTACT_URI, "streetName");
    public static final QName PROP_CONTACT_STREET_CODE = QName.createQName (CONTACT_URI, "streetCode");
    public static final QName PROP_CONTACT_HOUSE_NUMBER = QName.createQName (CONTACT_URI, "houseNumber");
    public static final QName PROP_CONTACT_MUNICIPALITY_CODE = QName.createQName (CONTACT_URI, "municipalityCode");
    public static final QName PROP_CONTACT_MAIL_SUBLOCATION_ID = QName.createQName (CONTACT_URI, "mailDeliverySublocationIdentifier");

    public static final QName PROP_CONTACT_REGISTERED = QName.createQName (CONTACT_URI, "registered");
    public static final QName PROP_CONTACT_INTERNAL = QName.createQName (CONTACT_URI, "internal");

    public static final QName PROP_NOTE_CONTENT = QName.createQName(NOTE_URI, "content");
    public static final QName PROP_ATTACHMENT_COUNT = QName.createQName(DOC_URI, "attachmentCount");

    /**
     * Association Names
     */

    public static final QName PROP_CONTACT_LOGIN_ASSOC = QName.createQName (CONTACT_URI, "contactLogin");

    /**
     * Constraints
     */
    public static final QName CONSTRAINT_CASE_SIMPLE_STATUS = QName.createQName(CASE_URI, "simpleStatusConstraint");
    public static final QName CONSTRAINT_CASE_ALLOWED_PARTY_ROLES = QName.createQName(CASE_URI, "allowedPartyRoles");

    //Document constraints
    public static final QName CONSTRAINT_DOC_STATUS = QName.createQName(DOC_URI, "statusConstraint");
    public static final QName CONSTRAINT_DOC_TYPES = QName.createQName(DOC_URI, "typeConstraint");
    public static final QName CONSTRAINT_DOC_CATEGORY = QName.createQName(DOC_URI, "categoryConstraint");
    public static final QName CONSTRAINT_DOC_STATE = QName.createQName(DOC_URI, "stateConstraint");
    public static final QName CONSTRAINT_DOC_RELATION = QName.createQName(DOC_URI, "relationConstraint");
    public static final QName CONSTRAINT_DOC_VARIANT = QName.createQName(DOC_URI, "variantConstraint");

    /**
     * Documents
     */
    public static final String DOCUMENTS_FOLDER_NAME = "documents";

    /**
     * Various constants
     */
    // currently 7 days in miliseconds - one day is 86400000
    public static final String MYCASES_DAYS_IN_THE_PAST = "604800000";

    public static final int AUDIT_LOG_MAX = 1000;

}
