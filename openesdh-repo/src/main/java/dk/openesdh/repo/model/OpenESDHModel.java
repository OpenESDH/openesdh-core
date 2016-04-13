package dk.openesdh.repo.model;

import org.alfresco.service.namespace.QName;

/**
 * Created by torben on 15/08/14.
 */
public interface OpenESDHModel {

    String OPENESDH_REPO_MODULE_ID = "openesdh-repo";

    String CLASSIF_URI = "http://openesdh.dk/model/classif/1.0";
    String CLASSIF_PREFIX = "classif";

    String CASE_URI = "http://openesdh.dk/model/case/base/1.0";
    String CASE_PREFIX = "base";
    String DOC_URI = "http://openesdh.dk/model/document/1.0";
    String DOC_PREFIX = "doc";
    String OE_URI = "http://openesdh.dk/model/openesdh/1.0";
    String OE_PREFIX = "oe";
    String TYPE_SIMPLE_NAME = "simple";
    String TYPE_BASE_NAME = "base";
    String CASE_MODEL_NAME = "caseModel";

    String CONTACT_PREFIX = "contact";
    String CONTACT_URI = "http://openesdh.dk/model/contact/1.0";

    String NOTE_PREFIX = "note";
    String NOTE_URI = "http://openesdh.dk/model/note/1.0";

    String FROZEN_CASE_PARTIES_PROP_PREFIX = "frozenParties";

    String STYPE_DOC_TYPE = "dtype";
    String STYPE_DOC_CATEGORY = "dcategory";
    String STYPE_PARTY_ROLE = "partyRole";
    String STYPE_PARTY = "party";

    /**
     * Models
     */
    QName DOCUMENT_MODEL = QName.createQName(DOC_URI, "documentModel");
    QName CASE_MODEL = QName.createQName(CASE_URI, CASE_MODEL_NAME);

    /**
     * Types
     */
    QName TYPE_CLASSIF_VALUE = QName.createQName(CLASSIF_URI, "classifValue");

    QName TYPE_OE_BASE = QName.createQName(OE_URI, TYPE_BASE_NAME);
    QName TYPE_OE_PARAMETER = QName.createQName(OE_URI, "parameter");
    QName TYPE_OE_AUTHORITY_FILES_FOLDER = QName.createQName(OE_URI, "authorityFilesFolder");

    QName TYPE_CASE_BASE = QName.createQName(CASE_URI, "case");
    QName TYPE_CASE_COMPLAINT = QName.createQName(CASE_URI, "complaint");

    QName TYPE_DOC_BASE = QName.createQName(DOC_URI, TYPE_BASE_NAME);
    QName TYPE_DOC_SIMPLE = QName.createQName(DOC_URI, TYPE_SIMPLE_NAME);

    QName TYPE_DOC_TYPE = QName.createQName(DOC_URI, STYPE_DOC_TYPE);
    QName TYPE_DOC_CATEGORY = QName.createQName(DOC_URI, STYPE_DOC_CATEGORY);
    QName TYPE_DOC_FILE = QName.createQName(DOC_URI, "file");
    QName TYPE_DOC_DIGITAL_FILE = QName.createQName(DOC_URI, "digitalFile");
    QName TYPE_DOC_PHYSICAL_FILE = QName.createQName(DOC_URI, "physicalFile");

    QName TYPE_CONTACT_BASE = QName.createQName(CONTACT_URI, "base");
    QName TYPE_CONTACT_PERSON = QName.createQName(CONTACT_URI, "person");
    QName TYPE_CONTACT_ORGANIZATION = QName.createQName(CONTACT_URI, "organization");
    QName TYPE_CONTACT_PARTY = QName.createQName(CONTACT_URI, STYPE_PARTY);
    QName TYPE_CONTACT_PARTY_ROLE = QName.createQName(CONTACT_URI, STYPE_PARTY_ROLE);

    QName TYPE_NOTE_NOTE = QName.createQName(NOTE_URI, "note");

    /**
     * Aspects
     */
    QName ASPECT_OE_OPENE_TYPE = QName.createQName(OpenESDHModel.OE_URI, "openeType");
    QName ASPECT_OE_JOURNALIZABLE = QName.createQName(OE_URI, "journalizable");
    QName ASPECT_OE_LOCKED = QName.createQName(OE_URI, "locked");
    QName ASPECT_OE_CASE_ID = QName.createQName(OE_URI, "caseId");

    QName ASPECT_CASE_COUNTER = QName.createQName(CASE_URI, "counter");
    QName ASPECT_CASE_FREEZABLE_PARTIES = QName.createQName(CASE_URI, "freezableParties");

    QName ASPECT_DOCUMENT_CONTAINER = QName.createQName(DOC_URI, "documentContainer");

    QName ASPECT_CONTACT_ADDRESS = QName.createQName(CONTACT_URI, "address");

    QName ASPECT_NOTE_NOTABLE = QName.createQName(NOTE_URI, "notable");
    QName ASPECT_DOC_RECORD = QName.createQName(DOC_URI, "record");

    QName ASPECT_DOC_IS_MAIN_FILE = QName.createQName(DOC_URI, "isMainFile");

    /**
     * Associations
     */
    QName ASSOC_CASE_OWNERS = QName.createQName(CASE_URI, "owners");

    QName ASSOC_DOC_TYPE = QName.createQName(DOC_URI, "atype");
    QName ASSOC_DOC_CATEGORY = QName.createQName(DOC_URI, "acategory");
    QName ASSOC_DOC_OWNER = QName.createQName(DOC_URI, "owner");
    QName ASSOC_DOC_RESPONSIBLE_PERSON = QName.createQName(DOC_URI, "responsible");
    QName ASSOC_DOC_MAIN = QName.createQName(DOC_URI, "main");
    QName ASSOC_DOC_ATTACHMENTS = QName.createQName(DOC_URI, "attachments");

    QName ASSOC_DOC_CONCERNED_PARTIES = QName.createQName(DOC_URI, "concernedParties");
    QName ASSOC_DOC_CASE_REFERENCES = QName.createQName(DOC_URI, "caseReferences");
    QName ASSOC_DOC_DOCUMENT_REFERENCES = QName.createQName(DOC_URI, "documentReferences");

    QName ASSOC_DOC_FILE_CONTENT = QName.createQName(DOC_URI, "fileContent");
    QName ASSOC_DOC_FILE_POSITION = QName.createQName(DOC_URI, "filePosition");

    QName ASSOC_CONTACT_MEMBERS = QName.createQName(CONTACT_URI, "members");

    QName ASSOC_CONTACT_LOGIN = QName.createQName(CONTACT_URI, "userLogin");

    QName ASSOC_CONTACT_PARTY_ROLE = QName.createQName(CONTACT_URI, "aPartyRole");

    QName ASSOC_NOTE_NOTES = QName.createQName(NOTE_URI, "notes");

    QName ASSOC_NOTE_CONCERNED_PARTIES = QName.createQName(NOTE_URI, "concernedParties");

    /**
     * Properties
     */
    QName PROP_CLASSIF_DISPLAY_NAME = QName.createQName(CLASSIF_URI, "displayName");
    QName PROP_CLASSIF_DISABLED = QName.createQName(CLASSIF_URI, "disabled");
    QName PROP_CLASSIF_IS_SYSTEM = QName.createQName(CLASSIF_URI, "isSystem");

    QName PROP_OE_OPENE_TYPE = QName.createQName(OpenESDHModel.OE_URI, "openeType");
    QName PROP_OE_ID = QName.createQName(OE_URI, "id");
    QName PROP_OE_LOCKED_BY = QName.createQName(OE_URI, "lockedBy");
    QName PROP_OE_LOCKED_DATE = QName.createQName(OE_URI, "lockedDate");
    QName PROP_OE_ORIGINAL_OWNER = QName.createQName(OE_URI, "originalOwner");
    QName PROP_OE_JOURNALKEY = QName.createQName(OE_URI, "journalKey");
    QName PROP_OE_JOURNALFACET = QName.createQName(OE_URI, "journalFacet");
    QName PROP_OE_JOURNALKEY_INDEXED = QName.createQName(OE_URI, "journalKeyIndexed");
    QName PROP_OE_JOURNALFACET_INDEXED = QName.createQName(OE_URI, "journalFacetIndexed");
    QName PROP_OE_CASE_ID = QName.createQName(OE_URI, "caseId");
    QName PROP_OE_OWNERS = QName.createQName(OE_URI, "owners");
    QName PROP_OE_STATUS = QName.createQName(OE_URI, "status");
    QName PROP_OE_PARAMETER_BOOL_VALUE = QName.createQName(OE_URI, "boolValue");

    QName PROP_CASE_STARTDATE = QName.createQName(CASE_URI, "startDate");
    QName PROP_CASE_ENDDATE = QName.createQName(CASE_URI, "endDate");
    QName PROP_CASE_SUBJECT = QName.createQName(CASE_URI, "subject");//TODO This is a value used for testing
    QName PROP_CASE_UNIQUE_NUMBER = QName.createQName(CASE_URI, "uniqueNumber");

    QName PROP_DOC_ARRIVAL_DATE = QName.createQName(DOC_URI, "arrivalDate");
    QName PROP_DOC_VARIANT = QName.createQName(DOC_URI, "variant");
    //document types
    QName PROP_DOC_TYPE = QName.createQName(DOC_URI, "type");
    //document categories
    QName PROP_DOC_CATEGORY = QName.createQName(DOC_URI, "category");

    QName PROP_CONTACT_EMAIL = QName.createQName(CONTACT_URI, "email");
    QName PROP_CONTACT_TYPE = QName.createQName(CONTACT_URI, "contactType");
    QName PROP_CONTACT_PHONE = QName.createQName(CONTACT_URI, "phone");
    QName PROP_CONTACT_MOBILE = QName.createQName(CONTACT_URI, "mobile");
    QName PROP_CONTACT_WEBSITE = QName.createQName(CONTACT_URI, "website");
    QName PROP_CONTACT_LINKEDIN = QName.createQName(CONTACT_URI, "linkedin");
    QName PROP_CONTACT_IM = QName.createQName(CONTACT_URI, "IM");
    QName PROP_CONTACT_NOTES = QName.createQName(CONTACT_URI, "notes");

    QName PROP_CONTACT_FIRST_NAME = QName.createQName(CONTACT_URI, "firstName");
    QName PROP_CONTACT_LAST_NAME = QName.createQName(CONTACT_URI, "lastName");
    QName PROP_CONTACT_MIDDLE_NAME = QName.createQName(CONTACT_URI, "middleName");
    QName PROP_CONTACT_CPR_NUMBER = QName.createQName(CONTACT_URI, "cprNumber");

    QName PROP_CONTACT_ORGANIZATION_NAME = QName.createQName(CONTACT_URI, "organizationName");
    QName PROP_CONTACT_DEPARTMENT = QName.createQName(CONTACT_URI, "department");
    QName PROP_CONTACT_CVR_NUMBER = QName.createQName(CONTACT_URI, "cvrNumber");

    QName PROP_CONTACT_ADDRESS = QName.createQName(CONTACT_URI, "address");
    QName PROP_CONTACT_ADDRESS_LINE1 = QName.createQName(CONTACT_URI, "addressLine1");
    QName PROP_CONTACT_ADDRESS_LINE2 = QName.createQName(CONTACT_URI, "addressLine2");
    QName PROP_CONTACT_ADDRESS_LINE3 = QName.createQName(CONTACT_URI, "addressLine3");
    QName PROP_CONTACT_ADDRESS_LINE4 = QName.createQName(CONTACT_URI, "addressLine4");
    QName PROP_CONTACT_ADDRESS_LINE5 = QName.createQName(CONTACT_URI, "addressLine5");
    QName PROP_CONTACT_ADDRESS_LINE6 = QName.createQName(CONTACT_URI, "addressLine6");
    QName PROP_CONTACT_FLOOR_IDENTIFIER = QName.createQName(CONTACT_URI, "floorIdentifier");
    QName PROP_CONTACT_SUITE_IDENTIFIER = QName.createQName(CONTACT_URI, "suiteIdentifier");
    QName PROP_CONTACT_POST_BOX = QName.createQName(CONTACT_URI, "postBox");
    QName PROP_CONTACT_POST_CODE = QName.createQName(CONTACT_URI, "postCode");
    QName PROP_CONTACT_POST_DISTRICT = QName.createQName(CONTACT_URI, "postDistrict");
    QName PROP_CONTACT_COUNTRY_CODE = QName.createQName(CONTACT_URI, "countryCode");
    QName PROP_CONTACT_CITY_NAME = QName.createQName(CONTACT_URI, "cityName");
    QName PROP_CONTACT_STREET_NAME = QName.createQName(CONTACT_URI, "streetName");
    QName PROP_CONTACT_STREET_CODE = QName.createQName(CONTACT_URI, "streetCode");
    QName PROP_CONTACT_HOUSE_NUMBER = QName.createQName(CONTACT_URI, "houseNumber");
    QName PROP_CONTACT_MUNICIPALITY_CODE = QName.createQName(CONTACT_URI, "municipalityCode");
    QName PROP_CONTACT_MAIL_SUBLOCATION_ID = QName.createQName(CONTACT_URI, "mailDeliverySublocationIdentifier");

    QName PROP_CONTACT_REGISTERED = QName.createQName(CONTACT_URI, "registered");
    QName PROP_CONTACT_INTERNAL = QName.createQName(CONTACT_URI, "internal");
    QName PROP_CONTACT_LOCKED_IN_CASES = QName.createQName(CONTACT_URI, "lockedInCases");

    QName PROP_CONTACT_CONTACT = QName.createQName(CONTACT_URI, "contact");
    QName PROP_CONTACT_PARTY_ROLE = QName.createQName(CONTACT_URI, "partyRole");

    QName PROP_NOTE_CONTENT = QName.createQName(NOTE_URI, "content");
    QName PROP_ATTACHMENT_COUNT = QName.createQName(DOC_URI, "attachmentCount");

    /**
     * Version label policy
     */
    String RETAIN_VERSION_LABEL = "retainVersionLabel";

    /**
     * Association Names
     */
    QName PROP_CONTACT_LOGIN_ASSOC = QName.createQName(CONTACT_URI, "contactLogin");

    /**
     * Constraints
     */
    QName CONSTRAINT_CASE_BASE_STATUS = QName.createQName(CASE_URI, "caseStatusConstraint");
    QName CONSTRAINT_CASE_ALLOWED_PARTY_ROLES = QName.createQName(CASE_URI, "allowedPartyRoles");

    //Document constraints
    QName CONSTRAINT_DOC_STATUS = QName.createQName(DOC_URI, "statusConstraint");
    QName CONSTRAINT_DOC_TYPES = QName.createQName(DOC_URI, "typeConstraint");
    QName CONSTRAINT_DOC_CATEGORY = QName.createQName(DOC_URI, "categoryConstraint");
    QName CONSTRAINT_DOC_STATE = QName.createQName(DOC_URI, "stateConstraint");
    QName CONSTRAINT_DOC_RELATION = QName.createQName(DOC_URI, "relationConstraint");
    QName CONSTRAINT_DOC_VARIANT = QName.createQName(DOC_URI, "variantConstraint");

    /**
     * Documents
     */
    String DOCUMENTS_FOLDER_NAME = "documents";
    String DOCUMENT_PROP_NAME = "name";
    String DOCUMENT_PROP_MODIFIED = "modified";
    String DOCUMENT_PROP_MODIFIER = "modifier";

    String DOCUMENT_TYPE_LETTER = "letter";
    String DOCUMENT_TYPE_INVOICE = "invoice";

    String DOCUMENT_CATEGORY_ANNEX = "annex";
    String DOCUMENT_CATEGORY_OTHER = "other";

    String DOCUMENT_STATE_RECEIVED = "received";
    String DOCUMENT_STATE_FINALISED = "finalised";

    /**
     * Various constants
     */
    // currently 7 days in miliseconds - one day is 86400000
    long MYCASES_DAYS_IN_THE_PAST = 604800000L;

    int AUDIT_LOG_MAX = 1000;
    String ADMIN_USER_NAME = "admin";

}
