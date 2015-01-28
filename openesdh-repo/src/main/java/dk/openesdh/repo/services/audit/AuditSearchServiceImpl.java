package dk.openesdh.repo.services.audit;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by flemmingheidepedersen on 18/11/14.
 */
public class AuditSearchServiceImpl implements AuditSearchService {

    protected JSONObject result;

    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    public AuditService auditService;


    @Override
    public JSONArray getAuditLogByCaseNodeRef(NodeRef nodeRef, Long timespan) {

        AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
        auditQueryParameters.setForward(false);
        auditQueryParameters.setApplicationName("esdh");


        //auditQueryParameters.setFromTime((new Date(+1).getTime()));
        auditQueryParameters.addSearchKey(null, nodeRef.toString());

        // create auditQueryCallback inside this method, putting it outside, will make it a singleton as the class is a service.
        OpenESDHAuditQueryCallBack auditQueryCallback = new OpenESDHAuditQueryCallBack();
        auditService.auditQuery(auditQueryCallback, auditQueryParameters, OpenESDHModel.auditlog_max);

        // test comment

        return auditQueryCallback.getResult();

    };



}
