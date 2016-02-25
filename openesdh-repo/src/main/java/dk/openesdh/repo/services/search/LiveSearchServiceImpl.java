package dk.openesdh.repo.services.search;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;

@Service("LiveSearchService")
public class LiveSearchServiceImpl implements LiveSearchService {

    private final Logger logger = LoggerFactory.getLogger(LiveSearchServiceImpl.class);
    private final Map<String, LiveSearchComponent> searchComponents = new HashMap<>();

    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @PostConstruct
    public void registerDefaultComponents() {
        searchComponents.put("case", createCaseSearchComponent());
        searchComponents.put("caseDocs", createCaseDocsSearchComponent());
    }

    public void registerComponent(String name, LiveSearchComponent component) {
        searchComponents.put(name, component);
    }

    public JSONObject search(String name, String query, int size) throws JSONException {
        JSONObject resultJson = new JSONObject();
        if (!searchComponents.containsKey(name)) {
            logger.warn("Search component \"{}\" not found. Returning empty list", name);
            resultJson.put(name, Collections.emptyList());
            return resultJson;
        }
        JSONArray searchResults = searchComponents.get(name).search(query, size);
        resultJson.put(name, searchResults);
        return resultJson;
    }

    private LiveSearchComponent createCaseSearchComponent() {
        return new LiveSearchComponent() {
            @Override
            public JSONArray search(String query, int size) throws JSONException {
                List<CaseInfo> foundCases = caseService.findCases(query, size);
                return buildCasesJSON(foundCases);
            }

            JSONArray buildCasesJSON(List<CaseInfo> cases) throws JSONException {
                JSONArray result = new JSONArray();
                for (CaseInfo caseItem : cases) {
                    JSONObject caseObj = new JSONObject();
                    caseObj.put("caseNodeRef", caseItem.getNodeRef());
                    caseObj.put("caseId", caseItem.getCaseId());
                    caseObj.put("caseTitle", caseItem.getTitle());
                    caseObj.put("caseEndDate", caseItem.getEndDate());
                    caseObj.put("caseStartDate", caseItem.getStartDate());
                    caseObj.put("caseCreatedDate", caseItem.getCreatedDate());
                    caseObj.put("caseDescription", caseItem.getDescription());
                    result.put(caseObj);
                }
                return result;
            }
        };
    }

    private LiveSearchComponent createCaseDocsSearchComponent() {
        return new LiveSearchComponent() {
            @Override
            public JSONArray search(String query, int size) throws JSONException {
                List<NodeRef> foundDocuments = documentService.findCaseDocuments(query, size);
                return buildDocsJSON(foundDocuments);
            }

            JSONArray buildDocsJSON(List<NodeRef> documents) throws JSONException {
                JSONArray result = new JSONArray();
                for (NodeRef document : documents) {
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
        };
    }
}
