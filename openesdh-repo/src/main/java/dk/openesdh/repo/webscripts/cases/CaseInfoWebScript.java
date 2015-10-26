package dk.openesdh.repo.webscripts.cases;

import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

/**
 * Created by torben on 11/09/14.
 */
@Component
@WebScript(description = "Retrieves case info either by caseId or case nodeRef", defaultFormat = "json", baseUri = "/api/openesdh/caseinfo", families = "Case Tools")
public class CaseInfoWebScript {

    @Autowired
    private NodeInfoService nodeInfoService;
    @Autowired
    private CaseService caseService;

    @Authentication(AuthenticationType.USER)
    @Uri(value = "/{caseId}", method = HttpMethod.GET)
    public Resolution getCaseInfoById(@UriVariable(WebScriptUtils.CASE_ID) final String caseId) throws JSONException {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        if (caseNodeRef == null) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "CASE_NOT_FOUND");
        }
        return getCaseInfo(caseNodeRef);
    }

    @Authentication(AuthenticationType.USER)
    @Uri(method = HttpMethod.GET)
    public Resolution getCaseInfoByNodeRef(@RequestParam(value = WebScriptUtils.NODE_REF) NodeRef caseNodeRef) throws JSONException {
        return getCaseInfo(caseNodeRef);
    }

    private Resolution getCaseInfo(NodeRef caseNodeRef) throws JSONException {
        NodeInfoService.NodeInfo nodeInfo = nodeInfoService.getNodeInfo(caseNodeRef);
        List<QName> requiredProps = Arrays.asList(OpenESDHModel.PROP_OE_ID, ContentModel.PROP_TITLE,
                OpenESDHModel.PROP_OE_STATUS, ContentModel.PROP_CREATOR,
                ContentModel.PROP_CREATED, ContentModel.PROP_MODIFIED, ContentModel.PROP_MODIFIER,
                ContentModel.PROP_DESCRIPTION, OpenESDHModel.PROP_OE_JOURNALKEY,
                OpenESDHModel.PROP_OE_JOURNALFACET, OpenESDHModel.PROP_OE_LOCKED_BY,
                OpenESDHModel.PROP_OE_LOCKED_DATE, OpenESDHModel.PROP_CASE_STARTDATE);
        JSONObject json = nodeInfoService.getSelectedProperties(nodeInfo, requiredProps);
        json.getJSONObject("properties")
                .put(OpenESDHModel.ASSOC_CASE_OWNERS.getLocalName(), caseService.getCaseOwners(caseNodeRef));
        try {
            JSONObject allProps = nodeInfoService.buildJSON(nodeInfo);
            json.put("allProps", allProps);
            json.put("isLocked", caseService.isLocked(caseNodeRef));
            json.put("statusChoices", caseService.getValidNextStatuses(caseNodeRef));

            JSONObject properties = (JSONObject) json.get("properties");
            properties.put("nodeRef", caseNodeRef.toString());
            properties.put("type", allProps.get(NodeInfoService.NODE_TYPE_PROPERTY));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return WebScriptUtils.jsonResolution(json);
    }

}
