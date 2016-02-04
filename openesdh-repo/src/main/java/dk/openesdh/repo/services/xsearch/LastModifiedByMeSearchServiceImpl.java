package dk.openesdh.repo.services.xsearch;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
@Service("LastModifiedByMeSearchService")
public class LastModifiedByMeSearchServiceImpl extends AbstractXSearchService implements LastModifiedByMeSearchService {

    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;
    @Autowired
    @Qualifier("auditService")
    private AuditService auditService;

    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        final List<NodeRef> nodeRefs = new LinkedList<>();

        AuditService.AuditQueryCallback auditQueryCallback = new AuditService.AuditQueryCallback() {
            @Override
            public boolean valuesRequired() {
                return true;
            }

            @Override
            public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {
                if (values.get("/esdh/case/value") != null) {
                    NodeRef nodeRef = new NodeRef((String) values.get("/esdh/case/value"));

                    // avoid getting the same case twice as there could be more than one audit entry for a case
                    if (!nodeRefs.contains(nodeRef)) {

                        NodeRef.Status status = nodeService.getNodeStatus(nodeRef);

                        if (!status.isDeleted()) {
                            nodeRefs.add(nodeRef);
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {
                throw new AlfrescoRuntimeException(errorMsg, error);
            }
        };

        // get the different casetypes from the dictionary
        Collection<QName> caseTypes = dictionaryService.getSubTypes(OpenESDHModel.TYPE_CASE_BASE, true);

        Long fromTime = new DateTime().toDate().getTime() - new Long(OpenESDHModel.MYCASES_DAYS_IN_THE_PAST);

        for (QName caseType : caseTypes) {

            // skip the basetype - getSubTypes returns it together with the subtypes
            if (!caseType.getLocalName().equals(OpenESDHModel.TYPE_BASE_NAME)) {

                // have to create the AuditQueryParameters object every time, as there is no way to clear the searchKeys
                AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
                auditQueryParameters.setForward(false);
                auditQueryParameters.setApplicationName("esdh");

                auditQueryParameters.setFromTime(fromTime);
                auditQueryParameters.addSearchKey(null, caseType.getPrefixString());

                // we need to call the auditQuery for every casetype, as auditQueryParameters currently only supports one searchKey at a time
                auditService.auditQuery(auditQueryCallback, auditQueryParameters, OpenESDHModel.AUDIT_LOG_MAX);
            }
        }
        return new XResultSet(nodeRefs, nodeRefs.size());
    }

}
