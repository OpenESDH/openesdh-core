package dk.openesdh.repo.webscripts.search;

import com.github.dynamicextensionsalfresco.webscripts.annotations.*;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;
import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.model.DocumentTemplateInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.documents.DocumentTemplateService;
import dk.openesdh.repo.utils.Utils;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * IMPORTANT
 * Please note that this isn't the function/method responsible for returning the results on the search page.
 * For that refer to openesdh-repo/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/slingshot/search/search.lib.js#L139
 * or on github https://github.com/OpenESDH/openesdh-core/blob/develop/openesdh-repo/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/slingshot/search/search.lib.js#L139
 */
@Component
@WebScript(families = {"OpenESDH search"}, defaultFormat = "json", description = "Contextual Live Search Webscripts")
public class LiveSearch {

    //<editor-fold desc="injected services and initialised properties">
    @Autowired
    private CaseService caseService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private DocumentTemplateService documentTemplateService;
    //</editor-fold>
    private static final Logger logger = Logger.getLogger(LiveSearch.class);

    public void init() {
        PropertyCheck.mandatory(this, "DocumentService", documentService);
        PropertyCheck.mandatory(this, "DocTemplateService", documentTemplateService);
        PropertyCheck.mandatory(this, "CaseService", caseService);
        PropertyCheck.mandatory(this, "NodeService", nodeService);
    }

    @Uri(value = "/api/openesdh/live-search/{context}?t={term}", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution execute(WebScriptRequest req, @UriVariable final String context, @RequestParam(required = false) final String filter ) throws JSONException {
        Map<String, String> params = Utils.parseParameters(req.getURL());
        int maxResults = 3;
        try {
            maxResults = Integer.parseInt(params.get("maxResults"));
        }
        catch (NumberFormatException nfe){
            if(logger.isDebugEnabled())
                logger.warn("\n\n-----> Max results parameter was unreadable from the webscript request parameter:\n\t\t\t"+ nfe.getLocalizedMessage());
        }
        JSONObject response = new JSONObject();

        switch (context){
            case "cases": {

                try {
                    List<CaseInfo> foundCases = this.caseService.findCases(params.get("t"), maxResults );
                    JSONArray jsonArray = buildCasesJSON(foundCases);
                    response.put("cases",jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;

            case "caseDocs": {

                try {
                    List<NodeRef> foundDocuments = this.documentService.findCaseDocuments(params.get("t"), maxResults);;
                    JSONArray jsonArray = buildDocsJSON(foundDocuments);
                    response.put("documents", jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;

            case "templates": {
                try {
                    List<DocumentTemplateInfo> foundTemplates = this.documentTemplateService.findTemplates(params.get("t"), maxResults);;
                    JSONArray jsonArray = buildDocTemplateJSON(foundTemplates);
                    response.put("templates", jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
        }

        return WebScriptUtils.jsonResolution(response);
    }

    JSONArray buildDocTemplateJSON(List<DocumentTemplateInfo> templates) throws JSONException {
        JSONArray result = new JSONArray();
        for(DocumentTemplateInfo template : templates){
            JSONObject templateObj = new JSONObject();

            templateObj.put("title", template.getTitle() );
            templateObj.put("name", template.getName() );
            templateObj.put("nodeRef", template.getNodeRef() );
            templateObj.put("version", template.getCustomProperty(ContentModel.PROP_VERSION_LABEL) );
            templateObj.put("templateType", template.getTemplateType() );
            result.put(templateObj);
        }
        return result;
    }

    JSONArray buildDocsJSON(List<NodeRef> documents) throws JSONException {
        JSONArray result = new JSONArray();
        for(NodeRef document : documents){
            JSONObject documentObj = new JSONObject();
            JSONObject caseObj = new JSONObject();
            Map<QName, Serializable> docProps = nodeService.getProperties(document);
            //The case to which the document belongs
            NodeRef docCase = documentService.getCaseNodeRef(document);
            //The actual docRecord (Folder) representing the document itself. This contains the "main document" we're interested in
            NodeRef docRecord = nodeService.getPrimaryParent(document).getParentRef();

            CaseInfo caseItem = caseService.getCaseInfo(docCase);
            //Create the case object which we'll stuff into the document object
            caseObj.put("caseNodeRef", caseItem.getNodeRef());
            caseObj.put("caseId", caseItem.getCaseId());
            caseObj.put("caseTitle", caseItem.getTitle());
            //Needed to get the mimetype
            ContentData docData = (ContentData) docProps.get(ContentModel.PROP_CONTENT);

            documentObj.put("name", docProps.get(ContentModel.PROP_NAME));
            documentObj.put("title", docProps.get(ContentModel.PROP_TITLE));
            documentObj.put("nodeRef", document);
            documentObj.put("docRecordNodeRef", docRecord);
            documentObj.put("docStatus", nodeService.getProperty(docRecord, OpenESDHModel.PROP_OE_STATUS));
            documentObj.put("version", docProps.get(ContentModel.PROP_VERSION_LABEL));
            documentObj.put("fileMimeType", docData.getMimetype());
            documentObj.put("case", caseObj); //This one isn't optional at the moment
            result.put(documentObj);
        }
        return result;
    }

    JSONArray buildCasesJSON(List<CaseInfo> cases) throws JSONException {
        JSONArray result = new JSONArray();
        for(CaseInfo caseItem : cases){
            JSONObject caseObj = new JSONObject();
            caseObj.put("caseNodeRef", caseItem.getNodeRef());
            caseObj.put("caseId", caseItem.getCaseId());
            caseObj.put("caseTitle",caseItem.getTitle());
            caseObj.put("caseEndDate",caseItem.getEndDate());
            caseObj.put("caseStartDate",caseItem.getStartDate());
            caseObj.put("caseCreatedDate",caseItem.getCreatedDate());
            caseObj.put("caseDescription",caseItem.getDescription());
            result.put(caseObj);
        }
        return result;
    }
}
