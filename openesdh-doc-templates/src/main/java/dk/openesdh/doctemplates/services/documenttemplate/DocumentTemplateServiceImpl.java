package dk.openesdh.doctemplates.services.documenttemplate;

import java.io.Serializable;
import java.util.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.doctemplates.model.DocumentTemplateInfo;
import dk.openesdh.doctemplates.model.DocumentTemplateInfoImpl;
import dk.openesdh.doctemplates.model.OpenESDHDocTemplateModel;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;

@Service("DocumentTemplateService")
public class DocumentTemplateServiceImpl implements DocumentTemplateService {

    private static final Log logger = LogFactory.getLog(DocumentTemplateService.class);

    @Autowired
    @Qualifier("OpenESDHFoldersService")
    private OpenESDHFoldersService openESDHFoldersService;
    @Autowired
    @Qualifier("SearchService")
    private SearchService searchService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    /**
     * Find document templates
     *
     * @param filter
     * @param size
     * @return
     */
    @Override
    public List<DocumentTemplateInfo> findTemplates(String filter, int size) {
        List<DocumentTemplateInfo> result;

        NodeRef templatesRoot = openESDHFoldersService.getTemplatesRootNodeRef();
        if (templatesRoot == null) {
            result = Collections.emptyList();
        } else {
            // get the cases that match the specified names
            StringBuilder query = new StringBuilder(128);
            query.append("+ASPECT:\"").append(OpenESDHDocTemplateModel.ASPECT_DOC_TEMPLATE).append('"');

            final boolean filterIsPresent = filter != null && filter.length() > 0;

            if (filterIsPresent) {
                query.append(" AND (");
                // Tokenize the filter and wildcard each token
                String escNameFilter = SearchLanguageConversion.escapeLuceneQuery(filter);
                String[] tokenizedFilter = SearchLanguageConversion.tokenizeString(escNameFilter);
                for (String aTokenizedFilter : tokenizedFilter) {
                    query.append(aTokenizedFilter).append("* ");
                }
                query.append(")");
            }

            SearchParameters sp = new SearchParameters();
            sp.addQueryTemplate("_DOCTEMPLATES", "|%doctmpl:templateType |%title " + "|%description |%doctmpl:assignedCaseTypes");
            sp.setDefaultFieldName("_DOCTEMPLATES");
            sp.addStore(templatesRoot.getStoreRef());
            sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            sp.setQuery(query.toString());
            if (size > 0) {
                sp.setLimit(size);
                sp.setLimitBy(LimitBy.FINAL_SIZE);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Search parameters are: " + sp);
            }

            ResultSet results = null;
            try {
                results = this.searchService.query(sp);
                result = new ArrayList<>(results.length());
                for (NodeRef tmpl : results.getNodeRefs()) {
                    result.add(getTemplateInfo(tmpl));
                }
            } catch (LuceneQueryParserException lqpe) {
                //Log the error but suppress is from the user
                logger.error("LuceneQueryParserException with findCases()", lqpe);
                result = Collections.emptyList();
            } finally {
                if (results != null) {
                    results.close();
                }
            }
        }
        return result;
    }

    @Override
    public DocumentTemplateInfo getTemplateInfo(NodeRef templateNodeRef) {
        DocumentTemplateInfo tmplInfo;

        // Get the properties
        Map<QName, Serializable> properties = this.nodeService.getProperties(templateNodeRef);

        @SuppressWarnings("unchecked")
        List<String> assignedTypes = (List<String>) properties.get(OpenESDHDocTemplateModel.PROP_ASSIGNED_CASE_TYPES);

        String[] assignedCaseTypes = assignedTypes.toArray(new String[assignedTypes.size()]);
        String title = (String) properties.get(ContentModel.PROP_TITLE);

        // Create and return the site information
        tmplInfo = new DocumentTemplateInfoImpl(templateNodeRef, assignedCaseTypes, title, properties);

        tmplInfo.setCreatedDate(DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_CREATED)));
        tmplInfo.setLastModifiedDate(DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_MODIFIED)));
        if (StringUtils.isNotBlank(properties.get(ContentModel.PROP_DESCRIPTION).toString())) {
            tmplInfo.setDescription(properties.get(ContentModel.PROP_DESCRIPTION).toString());
        }

        return tmplInfo;
    }
}
