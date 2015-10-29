package dk.openesdh.repo.audit;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.workflow.WorkflowTaskService;

@Service("audit.dk.openesdh.CaseIDExtractor")
public final class CaseNodeRefExtractor extends AbstractAnnotatedDataExtractor {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private CaseService caseService;
    @Autowired
    private WorkflowTaskService workflowTaskService;

    @SuppressWarnings("unchecked")
    public Serializable extractData(Serializable value) throws Throwable {
        String result = null;
        // TODO Ole,do we ever get an instance of a nodeRef?
        if (value instanceof NodeRef) {
            NodeRef nodeRef = (NodeRef) value;
            QName type = nodeService.getType(nodeRef);
            if (type.isMatch(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
                Serializable name = nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_NAME);
                result = getNodeRefFromString((String) name);
            } else {
                // received a NodeRef object, we know this is a permission change
                // therefore we always have a path
                nodeService.getPath(nodeRef);//throws error
                result = nodeRef.toString();
            }
        } else if (value instanceof String) {
            result = getNodeRefFromString((String) value);
        } else if (value instanceof Map) {
            result = getNodeRefFromMap((Map<QName, Serializable>) value);
        }
        // TODO: check that what is returned is actually a case, return null otherwise
        return result;
    }

    private String getNodeRefFromMap(Map<QName, Serializable> params) {
        return Optional.ofNullable(params.get(OpenESDHModel.PROP_OE_CASE_ID))
                .map(param -> getNodeRefFromCaseID(param.toString()))
                .orElse(null);
    }

    private String getNodeRefFromString(String str) {
        if (str.startsWith("GROUP_case_")) {
            String[] parts = str.split("_");
            if (parts.length < 3) {
                return null;
            }
            return getNodeRefFromCaseID(parts[2]);
        } else if (str.contains("GROUP_PARTY")) {
            String[] parts = str.split("_");
            if (parts.length < 3) {
                return null;
            }
            return getNodeRefFromCaseDbID(parts[2]);
        } else if (str.startsWith("activiti$") && !str.contains("start")) {
            return workflowTaskService.getWorkflowCaseId(str)
                    .map(caseId -> getNodeRefFromCaseID(caseId))
                    .orElse(null);
        }
        return getNodeRefFromPath(str);
    }

    protected String getNodeRefFromPath(String path) {
        String prefix = CaseService.OPENESDH_ROOT_CONTEXT_PATH;
        if (!path.startsWith(prefix)) {
            return null;
        }

        String caseId = getCaseIdFromPath(path);
        if (StringUtils.isEmpty(caseId)) {
            return null;
        }

        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        if (caseNodeRef != null) {
            return caseNodeRef.toString();
        }
        return null;
    }

    /**
     * Retrieves case id from the provided path of the following format:
     * /app:company_home/oe:OpenESDH/oe:cases/case:2015/case:6/case:15/case:20150615-916
     *
     * @param path the path to retrieve a case id from
     * @return case id
     */
    protected String getCaseIdFromPath(String path) {
        Matcher m = CaseService.CASE_ID_PATTERN.matcher(path);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    private String getNodeRefFromCaseID(String caseID) {
        int dashIndex = caseID.lastIndexOf('-');
        if (dashIndex != -1) {
            return getNodeRefFromCaseDbID(caseID.substring(dashIndex + 1));
        } else {
            return null;
        }
    }

    private String getNodeRefFromCaseDbID(String caseDbID) {
        NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(caseDbID));
        if (nodeRef != null) {
            return nodeRef.toString();
        } else {
            return null;
        }
    }
}
