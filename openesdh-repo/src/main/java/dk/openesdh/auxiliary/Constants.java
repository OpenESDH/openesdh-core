package dk.openesdh.auxiliary;

import org.alfresco.service.namespace.QName;

/**
 * Created by torben on 15/08/14.
 */
public class Constants {

  public static final String CASE_URI = "http://openesdh.dk/model/case/1.0/";
  public static final String DOC_URI = "http://openesdh.dk/model/document/1.0/";

  public static final QName CASE_SIMPLE = org.alfresco.service.namespace.QName.createQName(CASE_URI, "simple");
  public static final QName DOC_DOCUMENT = org.alfresco.service.namespace.QName.createQName(DOC_URI, "document");

}
