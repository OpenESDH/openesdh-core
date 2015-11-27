package dk.openesdh.repo.services.xsearch;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
public abstract class AbstractXSearchService implements XSearchService {
    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;
    @Autowired
    @Qualifier("SearchService")
    protected SearchService searchService;
    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;
    @Autowired
    @Qualifier("NamespaceService")
    protected NamespaceService namespaceService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Override
    public abstract XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending);

    @Override
    public XResultSet getNodesJSON(Map<String, String> params, int startIndex, int pageSize, String sortField,
            boolean ascending) {
        XResultSet result = getNodes(params, startIndex, pageSize, sortField, ascending);
        result.setNodes(getNodesJSON(result.getNodeRefs()));
        return result;
    }

    protected JSONArray getNodesJSON(List<NodeRef> nodeRefs) {
        JSONArray nodes = new JSONArray();
        nodeRefs.stream().map(this::nodeToJSONSafe).forEach(nodes::put);
        return nodes;
    }

    private JSONObject nodeToJSONSafe(NodeRef nodeRef) {
        try {
            return nodeToJSON(nodeRef);
        } catch (JSONException e) {
            throw new PlatformRuntimeException("Unable to serialize JSON", e);
        }
    }

    /**
     * Serializes the node to JSON. The default implementation outputs all
     * properties and associations. This can be overridden to output less (or
     * more) information.
     *
     * @param nodeRef
     * @return
     * @throws JSONException
     */
    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = new JSONObject();
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
            if (entry.getValue() instanceof Date) {
                json.put(entry.getKey().toPrefixString(namespaceService), ((Date) entry.getValue()).getTime());
            } else {
                json.put(entry.getKey().toPrefixString(namespaceService), entry.getValue());
            }
        }
        List<AssociationRef> associations = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        for (AssociationRef association : associations) {
            String assocName = association.getTypeQName().toPrefixString(namespaceService);
            if (!json.has(assocName)) {
                JSONArray refs = new JSONArray();
                addAssocToArray(association, refs);
                json.put(assocName, refs);
            } else {
                JSONArray refs = (JSONArray) json.get(assocName);
                addAssocToArray(association, refs);
                json.put(association.getTypeQName().toPrefixString(namespaceService), refs);
            }
        }
        json.put("TYPE", nodeService.getType(nodeRef).toPrefixString(namespaceService));
        json.put("nodeRef", nodeRef.toString());

        if (nodeService.getType(nodeRef).equals(OpenESDHModel.TYPE_DOC_DIGITAL_FILE)) {
            // also return the filename extension
            String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            String extension = FilenameUtils.getExtension(fileName);
            json.put("fileType", extension);
        }
        return json;
    }
    
    private void addAssocToArray(AssociationRef association, JSONArray refs) throws JSONException {
        QName type = nodeService.getType(association.getTargetRef());
        if (type.isMatch(ContentModel.TYPE_PERSON)) {
            PersonService.PersonInfo info = personService.getPerson(association.getTargetRef());
            JSONObject json = new JSONObject();
            json.put("value", info.getUserName());
            json.put("fullname", info.getFirstName() + " " + info.getLastName());
            refs.put(json);
        } else if (type.isMatch(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
            String groupName = (String) nodeService.getProperty(association.getTargetRef(),
                    ContentModel.PROP_AUTHORITY_NAME);
            JSONObject json = new JSONObject();
            json.put("value", groupName);
            json.put("fullname", authorityService.getAuthorityDisplayName(groupName));
            refs.put(json);
        } else {
            refs.put(association.getTargetRef());
        }
    }

    protected SearchParameters.SortDefinition getSortDefinition(String sortField, boolean ascending) {
        return new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, "@" + sortField, ascending);
    }

    /**
     * Empty. Provided to allow overriding of search parameters.
     * @param sp
     */
    protected void setSearchParameters(SearchParameters sp) {

    }

    protected XResultSet executeQuery(String query, int startIndex, int pageSize, String sortField, boolean ascending) {
        SearchParameters.SortDefinition sortDefinition =
                getSortDefinition(sortField, ascending);

        SearchParameters sp = new SearchParameters();
        sp.addStore(repositoryHelper.getCompanyHome().getStoreRef());
        sp.setSkipCount(startIndex);
        sp.setLimit(pageSize);
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setQuery(query);
        if (sortDefinition != null) {
            sp.addSort(sortDefinition);
        }
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        setSearchParameters(sp);

        ResultSet results = searchService.query(sp);
        if (results != null) {
            List<NodeRef> nodeRefs = results.getNodeRefs();
            results.close();
            return new XResultSet(nodeRefs, results.getNumberFound());
        } else {
            return new XResultSet();
        }
    }

    protected XResultSet executeQuery(String query) {
        return executeQuery(query, 0, -1, "cm:name", true);
    }

    protected static String stripTimeZoneFromDateTime(String str) {
        return str.replaceAll("((\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}))[+-](\\d{2}):(\\d{2})", "$1");
    }


    /**
     * Surrounds a value with quotes, escaping any quotes inside the value.
     * Escapes backslashes, double- and single-quotes.
     * @param value
     * @return
     */
    protected static String quote(String value) {
        return "\"" +
                value.replace("\\", "\\\\"). // Backslash
                        replace("'", "\\'"). // Single quote
                        replace("\"", "\\\"") + // Double quote
                "\"";
    }


    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }


    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public XResultSet getNodes(Map<String, String> params) {
        return getNodes(params, 0, -1, "cm:name", true);
    }
}
