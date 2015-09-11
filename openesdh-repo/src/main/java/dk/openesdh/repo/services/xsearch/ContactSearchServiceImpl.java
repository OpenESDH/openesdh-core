package dk.openesdh.repo.services.xsearch;

import dk.openesdh.repo.model.OpenESDHModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ContactSearchServiceImpl extends AbstractXSearchService implements ContactSearchService {

    private static final Logger LOG = Logger.getLogger(ContactSearchService.class.toString());

    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;
    private boolean personSearch;

    @Override
    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        String baseType = params.get("baseType");
        if (baseType == null) {
            throw new AlfrescoRuntimeException("Must specify a baseType parameter");
        }

        String term = params.get("term");
        if (term == null) {
            throw new AlfrescoRuntimeException("Must specify a term paramter");
        }

        QName baseTypeQName = QName.resolveToQName(namespaceService, baseType);

        // Make sure the base type is a Contact
        if (!dictionaryService.isSubClass(baseTypeQName, OpenESDHModel.TYPE_CONTACT_BASE)) {
            throw new AlfrescoRuntimeException(baseTypeQName + " is not a "
                    + "subtype of " + OpenESDHModel.TYPE_CONTACT_BASE);
        }

        // Trim / remove double-quotes
        term = term.trim().replace("\"", "");

        personSearch = false;

        String query;
        if (dictionaryService.isSubClass(baseTypeQName, OpenESDHModel.TYPE_CONTACT_PERSON)) {
            // Person
            query = buildPersonQuery(term);
            sortField = StringUtils.defaultIfEmpty(sortField, "contact:lastName");
        } else if (dictionaryService.isSubClass(baseTypeQName, OpenESDHModel.TYPE_CONTACT_ORGANIZATION)) {
            // Organization
            query = buildOrganizationQuery(term);
            sortField = StringUtils.defaultIfEmpty(sortField, "contact:organizationName");
        } else {
            throw new AlfrescoRuntimeException("Unsupported contact subtype: "
                    + baseTypeQName);
        }

        query = "TYPE:\"" + baseTypeQName + "\" AND (" + query + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(query);
        }
        return executeQuery(query, startIndex, pageSize, sortField, ascending);
    }

    @Override
    protected void setSearchParameters(SearchParameters sp) {
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setNamespace(OpenESDHModel.CONTACT_URI);
        if (this.personSearch) {
            sp.addQueryTemplate("_PERSON", "|%firstName OR |%middleName OR "
                    + "|%lastName");
            sp.setDefaultFieldName("_PERSON");
            sp.setDefaultOperator(SearchParameters.Operator.AND);
        }
    }

    protected String buildPersonQuery(String term) {
        List<String> searchTerms = new ArrayList<>();
        QName[] fields = new QName[]{
            OpenESDHModel.PROP_CONTACT_FIRST_NAME,
            OpenESDHModel.PROP_CONTACT_MIDDLE_NAME,
            OpenESDHModel.PROP_CONTACT_LAST_NAME,
            OpenESDHModel.PROP_CONTACT_EMAIL,
            OpenESDHModel.PROP_CONTACT_CPR_NUMBER
        };

        String[] tokens = term.split("(?<!\\\\) ");

        this.personSearch = true;

        if (tokens.length == 1) {
            if (term.endsWith("*")) {
                term = term.substring(0, term.lastIndexOf("*"));
            }

            term += "*";

            for (QName field : fields) {
                searchTerms.add(buildField(field, term));
            }
        } else {
            StringBuilder multiPartNames = new StringBuilder(tokens.length);
            boolean firstToken = true;
            for (String token : tokens) {
                if (token.endsWith("*")) {
                    token = token.substring(0, token.lastIndexOf("*"));
                }
                multiPartNames.append("\"");
                multiPartNames.append(token);
                multiPartNames.append("*\"");
                if (firstToken) {
                    multiPartNames.append(' ');
                }
                firstToken = false;
            }

            searchTerms.add(OpenESDHModel.PROP_CONTACT_FIRST_NAME.toString() + ":"
                    + multiPartNames.toString());
            searchTerms.add(OpenESDHModel.PROP_CONTACT_MIDDLE_NAME.toString()
                    + ":"
                    + multiPartNames.toString());
            searchTerms.add(OpenESDHModel.PROP_CONTACT_LAST_NAME.toString() + ":"
                    + multiPartNames.toString());
        }
        return StringUtils.join(searchTerms, " OR ");
    }

    private String buildField(QName field, String value) {
        return field.toString() + ":" + quote(value);
    }

    protected String buildOrganizationQuery(String term) {
        List<String> searchTerms = new ArrayList<>();
        QName[] fields = new QName[]{
            OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME,
            OpenESDHModel.PROP_CONTACT_CVR_NUMBER,
            OpenESDHModel.PROP_CONTACT_EMAIL
        };

        term += "*";

        for (QName field : fields) {
            searchTerms.add(buildField(field, term));
        }
        return StringUtils.join(searchTerms, " OR ");
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

}
