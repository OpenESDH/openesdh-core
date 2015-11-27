package dk.openesdh.repo.services.cases;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;

public interface CaseOwnersService {

    Set<String> getCaseOwnersUserIds(NodeRef caseNodeRef);

    Set<String> getCaseOwnersAuthorityNames(NodeRef caseNodeRef);

    JSONArray getCaseOwners(NodeRef nodeRef) throws JSONException;

}
