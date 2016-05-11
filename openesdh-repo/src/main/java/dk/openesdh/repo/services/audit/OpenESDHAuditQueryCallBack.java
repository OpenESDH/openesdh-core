package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.security.PersonService;

import com.google.common.base.Joiner;

public class OpenESDHAuditQueryCallBack implements AuditService.AuditQueryCallback {

    private final Map<String, AuditEntryHandler> auditEntryHandlers;
    private final PersonService personService;
    private final AuthenticationContext authenticationContext;
    private final Map<String, String> userFullNames = new HashMap<>();

    private final List<AuditEntry> result = new ArrayList<>();

    public OpenESDHAuditQueryCallBack(Map<String, AuditEntryHandler> auditEntryHandlers, PersonService personService,
            AuthenticationContext authenticationContext) {
        this.auditEntryHandlers = auditEntryHandlers;
        this.personService = personService;
        this.authenticationContext = authenticationContext;
    }

    public List<AuditEntry> getResult() {
        return result;
    }

    @Override
    public boolean valuesRequired() {
        return true;
    }

    @Override
    public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {
        getAuditEntryHandler(values.keySet())
                .flatMap(handler -> handler.createAuditEntry(user, time, values))
                .ifPresent(entry -> {
                    entry.setFullName(getUserFullName(entry.getUser()));
                    result.add(entry);
                });
        return true;
    }

    private Optional<AuditEntryHandler> getAuditEntryHandler(Set<String> auditValuesEntryKeys) {
        return auditEntryHandlers.entrySet()
                .stream()
                .filter(handler -> auditValuesEntryKeys.contains(handler.getKey()))
                .findFirst()
                .map(handler -> handler.getValue());
    }

    @Override
    public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {
        throw new AlfrescoRuntimeException(errorMsg, error);
    }

    private String getUserFullName(String username) {
        if (userFullNames.containsKey(username)) {
            return userFullNames.get(username);
        }
        //if not cached:
        if (authenticationContext.isSystemUserName(username)) {
            userFullNames.put(username, username);
            return username;
        }
        PersonService.PersonInfo person = personService.getPerson(personService.getPerson(username));
        String fullName = Joiner.on(" ").skipNulls().join(person.getFirstName(), person.getLastName()).trim();
        userFullNames.put(username, fullName);
        return fullName;
    }
}
