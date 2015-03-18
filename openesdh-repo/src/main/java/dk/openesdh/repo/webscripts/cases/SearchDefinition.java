package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Created by rasmutor on 3/12/15.
 */
public class SearchDefinition extends DeclarativeWebScript {

    private CaseService caseService;

    private NamespaceService namespaceService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String caseType = req.getServiceMatch().getTemplateVars().get("caseType");

        String url = namespaceService.getNamespaceURI(getPrefix(caseType));
        QName name = QName.createQName("{" + url + "}" + getShortName(caseType));

        return caseService.getSearchDefinition(name);
//        Map<String, Object> model = new HashMap<>();
//        return model;
    }

    private String getPrefix(String caseType) {
        String prefix = null;
        int index = caseType.indexOf("_");
        if (index > 0) {
            prefix = caseType.substring(0, index);
        }
        return prefix;
    }

    private String getShortName(String caseType) {
        String shortname = null;
        int index = caseType.indexOf("_");
        if (index > 0) {
            shortname = caseType.substring(index + 1);
        }
        return shortname;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
}
