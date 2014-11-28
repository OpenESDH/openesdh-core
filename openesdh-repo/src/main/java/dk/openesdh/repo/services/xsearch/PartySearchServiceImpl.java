package dk.openesdh.repo.services.xsearch;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;

import java.util.*;

public class PartySearchServiceImpl extends AbstractXSearchService
        implements PartySearchService {

    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;

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

        String query;
        if (dictionaryService.isSubClass(baseTypeQName,
                OpenESDHModel.TYPE_PARTY_PERSON)) {
            // Person
            query = buildPersonQuery(term);
        } else if (dictionaryService.isSubClass(baseTypeQName, OpenESDHModel.TYPE_PARTY_ORGANIZATION)) {
            // Organization
            query = buildOrganizationQuery(term);
        } else {
            throw new AlfrescoRuntimeException("Unsupported party subtype: "
                    + baseTypeQName);
        }

        query = "TYPE:\"" + baseTypeQName + "\" AND (" + buildBaseQuery
                (baseTypeQName, term) + " OR " + query + ")";
        System.out.println(query);
        return executeQuery(query);
    }

    protected String buildPersonQuery(String term) {
        List<String> searchTerms = new ArrayList<>();
        QName[] fields = new QName[]{
                OpenESDHModel.PROP_PARTY_FIRST_NAME,
                OpenESDHModel.PROP_PARTY_MIDDLE_NAME,
                OpenESDHModel.PROP_PARTY_LAST_NAME,
                OpenESDHModel.PROP_PARTY_CPR_NUMBER
        };

        term += "*";

        // TODO: Better searching of names (splitting of multi-part names)
        // See Alfresco's org.alfresco.repo.jscript.People#getPeopleImplSearch
        for (QName field : fields) {
            searchTerms.add(buildField(field, term));
        }
        return StringUtils.join(searchTerms, " OR ");
    }

    private String buildField(QName field, String value) {
        return "@" + QueryParser.escape(field.toString()) + ":" + quote(value);
    }

    protected String buildBaseQuery(QName baseTypeQName, String term) {
        return buildField(OpenESDHModel.PROP_PARTY_EMAIL, term + "*");
    }

    protected String buildOrganizationQuery(String term) {
        List<String> searchTerms = new ArrayList<>();
        QName[] fields = new QName[]{
                OpenESDHModel.PROP_PARTY_ORGANIZATION_NAME,
                OpenESDHModel.PROP_PARTY_CVR_NUMBER
        };
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