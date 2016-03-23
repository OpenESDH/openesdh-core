package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.function.Predicate;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;

/**
 * Created by flemmingheidepedersen on 18/11/14.
 */
public interface AuditSearchService {

    DateFormat AUDIT_DATE_FORMAT = new SimpleDateFormat("dd. MMM. yyyy");

    public void registerApplication(String name);

    public void registerEntryHandler(String key, AuditEntryHandler handler);

    public void registerIgnoredProperties(QName... prop);

    public void registerIgnoredAspects(QName... aspect);

    public JSONArray getAuditLogByCaseNodeRef(NodeRef nodeRef, int timespan);

    void addTransactionPathEntryHandler(Predicate<Map<String, Serializable>> predicate, IAuditEntryHandler handler);

}
