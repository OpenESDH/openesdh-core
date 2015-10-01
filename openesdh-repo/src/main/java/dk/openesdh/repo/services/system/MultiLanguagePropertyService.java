package dk.openesdh.repo.services.system;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.NativeObject;

public interface MultiLanguagePropertyService {

    public MultiLanguageValue getMLValues(NodeRef nodeRef, QName propertyName);

    public void setMLValues(NodeRef nodeRef, QName propertyQName, NativeObject values);
}
