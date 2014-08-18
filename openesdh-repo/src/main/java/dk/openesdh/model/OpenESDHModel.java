package dk.openesdh.model;

import org.alfresco.service.namespace.*;

/**
 * Created by torben on 15/08/14.
 */
public interface OpenESDHModel {

    public static final String CASE_URI = "http://openesdh.dk/model/case/1.0/";
    public static final String DOC_URI = "http://openesdh.dk/model/document/1.0/";
    public static final String OE_URI = "http://openesdh.dk/model/openesdh/1.0/";

    /**
     * Types
     */
    public static final QName TYPE_OE_BASE = QName.createQName(OE_URI, "base");

    public static final QName TYPE_CASE_BASE = QName.createQName(CASE_URI, "base");
    public static final QName TYPE_CASE_SIMPLE = QName.createQName(CASE_URI, "simple");

    public static final QName TYPE_DOC_DOCUMENT = QName.createQName(DOC_URI, "document");

    /**
     * Aspects
     */
    public static final QName ASPECT_OE_READONLY = QName.createQName(OE_URI, "readOnly");
    public static final QName ASPECT_OE_JOURNALIZABLE = QName.createQName(OE_URI, "journalizable");
    public static final QName ASPECT_OE_JOURNALKEY = QName.createQName(OE_URI, "journalKey");

    public static final QName ASPECT_CASE_JOURNALIZED = QName.createQName(CASE_URI, "journalized");
    public static final QName ASPECT_CASE_JOURNALKEY = QName.createQName(CASE_URI, "journalKey");

    /**
     * Associations
     */
    public static final QName ASSOC_DOC_RESPONSIBLE_PERSON = QName.createQName(DOC_URI, "owner");
    public static final QName ASSOC_DOC_MAIN = QName.createQName(DOC_URI, "main");
    public static final QName ASSOC_DOC_ATTACHMENTS = QName.createQName(DOC_URI, "attachments");

    /**
     * Properties
     */
    public static final QName PROP_OE_ID = QName.createQName(OE_URI, "id");
    public static final QName PROP_OE_TITLE = QName.createQName(OE_URI, "title");
    public static final QName PROP_OE_DESCRIPTION = QName.createQName(OE_URI, "description");
    public static final QName PROP_OE_STATUS = QName.createQName(OE_URI, "status");

    public static final QName PROP_CASE_OWNERS = QName.createQName(CASE_URI, "owners");
    public static final QName PROP_CASE_STARTDATE = QName.createQName(CASE_URI, "startDate");
    public static final QName PROP_CASE_ENDDATE = QName.createQName(CASE_URI, "endDate");
    public static final QName PROP_CASE_JOURNALIZED_BY = QName.createQName(CASE_URI, "jounalizedBy");
    public static final QName PROP_CASE_JOURNALIZED_DATE = QName.createQName(CASE_URI, "journalizedDate");
    public static final QName PROP_CASE_JOURNAL_KEY = QName.createQName(CASE_URI, "journalKey");


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
