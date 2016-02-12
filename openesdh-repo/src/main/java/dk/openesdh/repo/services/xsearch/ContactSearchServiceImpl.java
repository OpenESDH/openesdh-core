package dk.openesdh.repo.services.xsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

@Service("ContactSearchService")
public class ContactSearchServiceImpl extends AbstractXSearchService implements ContactSearchService {

    private final Logger logger = LoggerFactory.getLogger(ContactSearchServiceImpl.class);

    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;

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
        logger.debug(query);
        return executeQuery(query, startIndex, pageSize, sortField, ascending);
    }

    @Override
    protected void setSearchParameters(SearchParameters sp) {
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setNamespace(OpenESDHModel.CONTACT_URI);
        if (isSearchForPerson(sp)) {
            sp.addQueryTemplate("_PERSON", "|%firstName OR |%middleName OR "
                    + "|%lastName");
            sp.setDefaultFieldName("_PERSON");
            sp.setDefaultOperator(SearchParameters.Operator.AND);
        }
    }

    private boolean isSearchForPerson(SearchParameters sp) {
        return sp.getQuery().contains("TYPE:\"" + OpenESDHModel.TYPE_CONTACT_PERSON + "\"");
    }

    private String buildPersonQuery(String term) {
        List<String> searchTerms = new ArrayList<>();
        QName[] fields = new QName[]{
            OpenESDHModel.PROP_CONTACT_FIRST_NAME,
            OpenESDHModel.PROP_CONTACT_MIDDLE_NAME,
            OpenESDHModel.PROP_CONTACT_LAST_NAME,
            OpenESDHModel.PROP_CONTACT_EMAIL,
            OpenESDHModel.PROP_CONTACT_CPR_NUMBER
        };

        String[] tokens = term.split("(?<!\\\\) ");

        if (tokens.length == 1) {
            if (term.endsWith("*")) {
                term = term.substring(0, term.lastIndexOf('*'));
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
                    token = token.substring(0, token.lastIndexOf('*'));
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

    private String buildOrganizationQuery(String term) {
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
}
