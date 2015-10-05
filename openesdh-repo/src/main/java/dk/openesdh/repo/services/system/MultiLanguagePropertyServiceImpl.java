package dk.openesdh.repo.services.system;

import java.io.Serializable;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.NativeObject;
import static org.springframework.extensions.surf.util.I18NUtil.parseLocale;

public class MultiLanguagePropertyServiceImpl implements MultiLanguagePropertyService {

    private NodeService nodeService;

    public MultiLanguageValue getMLValues(NodeRef nodeRef, QName propertyQName) {
        MultiLanguageValue values = new MultiLanguageValue();
        boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
        try {
            MLText property = (MLText) nodeService.getProperty(nodeRef, propertyQName);
            if (property != null) {
                ((MLText) property).entrySet().stream().forEach((e) -> {
                    values.defineProperty(e.getKey().getLanguage(), e.getValue(), NativeObject.PERMANENT);
                });
            }
        } finally {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }
        return values;
    }

    public void setMLValues(NodeRef nodeRef, QName propertyQName, NativeObject values) {
        if (values != null) {
            boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
            try {
                Serializable property = nodeService.getProperty(nodeRef, propertyQName);
                MLText mlText = property == null ? new MLText() : (MLText) property;
                mlText.clear();
                for (Object id : NativeObject.getPropertyIds(values)) {
                    String propertyName = id.toString();
                    mlText.addValue(parseLocale(propertyName), NativeObject.getProperty(values, propertyName).toString());
                }
                nodeService.setProperty(nodeRef, propertyQName, mlText);
            } finally {
                MLPropertyInterceptor.setMLAware(wasMLAware);
            }
        } else {
            nodeService.removeProperty(nodeRef, propertyQName);
        }

    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
