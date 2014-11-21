package dk.openesdh.repo.services.Audit;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by flemmingheidepedersen on 18/11/14.
 */
public class AuditSearchServiceImpl implements AuditService {

    protected JSONObject result;
    private org.alfresco.service.cmr.audit.AuditService auditService;

    protected org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback auditQueryCallback = new OpenESDHAuditQueryCallBack();

    protected JSONObject getAuditLogByCaseNodeRef(NodeRef nodeRef, Long timespan) {


        AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
        auditQueryParameters.setForward(false);
        auditQueryParameters.setApplicationName("esdh");

        auditQueryParameters.setFromTime(fromTime);
        auditQueryParameters.addSearchKey(null, caseType.getPrefixString());

        // we need to call the auditQuery for every casetype, as auditQueryParameters currently only supports one searchKey at a time
        auditService.auditQuery(auditQueryCallback, auditQueryParameters, OpenESDHModel.auditlog_max);

        return null;

    };



}
