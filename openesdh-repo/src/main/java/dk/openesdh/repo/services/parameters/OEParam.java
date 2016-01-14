package dk.openesdh.repo.services.parameters;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Enumerated all available OpenE configuration parameters with default values. Name and order are important. QName
 * represents value type
 */
public enum OEParam {

    can_create_contacts(OpenESDHModel.PROP_OE_PARAMETER_BOOL_VALUE, false);

    public final QName qname;
    public final Serializable defaultVal;

    private OEParam(QName qname, Serializable defaultVal) {
        this.qname = qname;
        this.defaultVal = defaultVal;
    }

}
