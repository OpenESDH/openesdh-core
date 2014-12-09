package dk.openesdh.repo.services.xsearch;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;

import java.util.*;

public class PartySearchServiceImpl extends AbstractXSearchService
        implements PartySearchService {

    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;
    private boolean personSearch;

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

        // Make sure the base type is a Party
        if (!dictionaryService.isSubClass(baseTypeQName,
                OpenESDHModel.TYPE_PARTY_BASE)) {
            throw new AlfrescoRuntimeException(baseTypeQName + " is not a " +
                    "subtype of " + OpenESDHModel.TYPE_PARTY_BASE);
        }

        // Trim / remove double-quotes
        term = term.trim().replace("\"", "");

        personSearch = false;

        String query;
        if (dictionaryService.isSubClass(baseTypeQName,
                OpenESDHModel.TYPE_PARTY_PERSON)) {
            // Person
            query = buildPersonQuery(term);
            if (sortField.equals("")) {
                sortField = "party:lastName";
            }
        } else if (dictionaryService.isSubClass(baseTypeQName, OpenESDHModel.TYPE_PARTY_ORGANIZATION)) {
            // Organization
            query = buildOrganizationQuery(term);
            if (sortField.equals("")) {
                sortField = "party:organizationName";
            }
        } else {
            throw new AlfrescoRuntimeException("Unsupported party subtype: "
                    + baseTypeQName);
        }

        query = "TYPE:\"" + baseTypeQName + "\" AND (" + query + ")";
        System.out.println(query);
        return executeQuery(query, startIndex, pageSize, sortField, ascending);
    }

    @Override
    protected void setSearchParameters(SearchParameters sp) {
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setNamespace(OpenESDHModel.PARTY_URI);
        if (this.personSearch) {
            sp.addQueryTemplate("_PERSON", "|%firstName OR |%middleName OR " +
                    "|%lastName");
            sp.setDefaultFieldName("_PERSON");
            sp.setDefaultOperator(SearchParameters.Operator.AND);
        }
    }

    protected String buildPersonQuery(String term) {
        List<String> searchTerms = new ArrayList<>();
        QName[] fields = new QName[]{
                OpenESDHModel.PROP_PARTY_FIRST_NAME,
                OpenESDHModel.PROP_PARTY_MIDDLE_NAME,
                OpenESDHModel.PROP_PARTY_LAST_NAME,
                OpenESDHModel.PROP_PARTY_EMAIL,
                OpenESDHModel.PROP_PARTY_CPR_NUMBER
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

            searchTerms.add(OpenESDHModel.PROP_PARTY_FIRST_NAME.toString() + ":" +
                    multiPartNames.toString());
            searchTerms.add(OpenESDHModel.PROP_PARTY_MIDDLE_NAME.toString() +
                    ":" +
                    multiPartNames.toString());
            searchTerms.add(OpenESDHModel.PROP_PARTY_LAST_NAME.toString() + ":" +
                    multiPartNames.toString());
        }
        return StringUtils.join(searchTerms, " OR ");
    }

    private String buildField(QName field, String value) {
        return field.toString() + ":" + quote(value);
    }

    protected String buildOrganizationQuery(String term) {
        List<String> searchTerms = new ArrayList<>();
        QName[] fields = new QName[]{
                OpenESDHModel.PROP_PARTY_ORGANIZATION_NAME,
                OpenESDHModel.PROP_PARTY_CVR_NUMBER,
                OpenESDHModel.PROP_PARTY_EMAIL
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