package dk.openesdh.doctemplates.model;

import org.alfresco.service.namespace.QName;

public interface OpenESDHDocTemplateModel {

    String DOC_TMPL_URI = "http://openesdh.dk/model/document/templates/1.0";
    String DOC_TMPL_PREFIX = "doctmpl";

    QName ASPECT_DOC_TEMPLATE = QName.createQName(DOC_TMPL_URI, "template");

    //For document templates
    QName PROP_TEMPLATE_TYPE = QName.createQName(DOC_TMPL_URI, "templateType");
    QName PROP_ASSIGNED_CASE_TYPES = QName.createQName(DOC_TMPL_URI, "assignedCaseTypes");

}
