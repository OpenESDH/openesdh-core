package dk.openesdh.repo.services.audit;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;

/**
 * Created by flemmingheidepedersen on 18/11/14.
 */
public interface AuditSearchService {

    public void registerApplication(String name);

    public void registerEntryHandler(String key, AuditEntryHandler handler);

    public void registerIgnoredProperties(QName... prop);

    public JSONArray getAuditLogByCaseNodeRef(NodeRef nodeRef, int timespan);

}
