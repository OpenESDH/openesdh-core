package dk.openesdh.repo.model;

import org.alfresco.service.namespace.*;

/**
 * Created by torben on 15/08/14.
 */
public interface OpenESDHModel {

    public static final String CASE_URI = "http://openesdh.dk/model/case/1.0/";
    public static final String CASE_PREFIX = "case";
    public static final String DOC_URI = "http://openesdh.dk/model/document/1.0/";
    public static final String DOC_PREFIX = "doc";
    public static final String OE_URI = "http://openesdh.dk/model/openesdh/1.0/";
    public static final String OE_PREFIX = "oe";
    public static final String TYPE_SIMPLE_NAME = "simple";
    public static final String TYPE_BASE_NAME = "base";



    /**
     * Types
     */
    public static final QName TYPE_OE_BASE = QName.createQName(OE_URI, TYPE_BASE_NAME);

    public static final QName TYPE_CASE_BASE = QName.createQName(CASE_URI, TYPE_BASE_NAME);
    public static final QName TYPE_CASE_SIMPLE = QName.createQName(CASE_URI, TYPE_SIMPLE_NAME);

    public static final QName TYPE_DOC_BASE = QName.createQName(DOC_URI,
            TYPE_BASE_NAME);
    public static final QName TYPE_DOC_SIMPLE = QName.createQName(DOC_URI,
            TYPE_SIMPLE_NAME);

    /**
     * Aspects
     */
    public static final QName ASPECT_OE_READONLY = QName.createQName(OE_URI, "readOnly");
    public static final QName ASPECT_OE_JOURNALIZED = QName.createQName(OE_URI, "journalized");

    public static final QName ASPECT_CASE_COUNTER = QName.createQName(CASE_URI, "counter");

    /**
     * Associations
     */
    public static final QName ASSOC_DOC_RESPONSIBLE_PERSON = QName.createQName(DOC_URI, "owner");
    public static final QName ASSOC_DOC_MAIN = QName.createQName(DOC_URI, "main");
    public static final QName ASSOC_DOC_ATTACHMENTS = QName.createQName(DOC_URI, "attachments");
    public static final QName ASSOC_CASE_OWNERS = QName.createQName(CASE_URI, "owners");

    /**
     * Properties
     */
    public static final QName PROP_OE_ID = QName.createQName(OE_URI, "id");
    public static final QName PROP_OE_TITLE = QName.createQName(OE_URI, "title");
    public static final QName PROP_OE_DESCRIPTION = QName.createQName(OE_URI, "description");
    public static final QName PROP_OE_STATUS = QName.createQName(OE_URI, "status");
    public static final QName PROP_OE_JOURNALIZED_BY = QName.createQName(OE_URI, "journalizedBy");
    public static final QName PROP_OE_JOURNALIZED_DATE = QName.createQName(OE_URI, "journalizedDate");
    public static final QName PROP_OE_JOURNALKEY = QName.createQName(OE_URI, "journalKey");
    public static final QName PROP_OE_IS_JOURNALIZED = QName.createQName(OE_URI, "isJournalized");

    public static final QName PROP_CASE_STARTDATE = QName.createQName(CASE_URI, "startDate");
    public static final QName PROP_CASE_ENDDATE = QName.createQName(CASE_URI, "endDate");

    public static final QName PROP_CASE_UNIQUE_NUMBER = QName.createQName(CASE_URI, "uniqueNumber");


    public static final QName PROP_DOC_ARRIVAL_DATE = QName.createQName(DOC_URI, "arrivalDate");
    public static final QName PROP_DOC_CATEGORY = QName.createQName(DOC_URI, "category");
    public static final QName PROP_DOC_IS_MAIN_ENTRY = QName.createQName(DOC_URI, "isMainEntry");

    /**
     * Constraints
     */
    public static final QName CONSTRAINT_CASE_SIMPLE_STATUS = QName.createQName(CASE_URI, "simpleStatusConstraint");
    public static final QName CONSTRAINT_DOC_STATUS = QName.createQName(DOC_URI, "statusConstraint");
    public static final QName CONSTRAINT_DOC_CATEGORY = QName.createQName(DOC_URI, "categoryConstraint");
}
