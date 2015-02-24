package dk.openesdh.repo.services.audit;

import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.json.simple.JSONArray;

/**
 * Created by flemmingheidepedersen on 18/11/14.
 */
public interface AuditSearchService {

    public JSONArray getAuditLogByCaseNodeRef(NodeRef nodeRef, int timespan);

}
