package dk.openesdh;

import org.alfresco.service.namespace.QName;

/**
 * @author Lanre Abiwon.
 */
public interface SimpleCaseModel {
    public static final String SIMPLE_CASE_URI = "http://openesdh.dk/model/case/simple/1.0/";
    public static final String SIMPLE_CASE_PREFIX = "simple";
    public static final String TYPE_CASE = "case";

    /**
     * Models
     */
    public static final QName SIMPLE_CASE_MODEL = QName.createQName(SIMPLE_CASE_URI, "simpleModel");

    /**
     * Types
     */
    public static final QName TYPE_CASE_SIMPLE = QName.createQName(SIMPLE_CASE_URI, TYPE_CASE);
}
