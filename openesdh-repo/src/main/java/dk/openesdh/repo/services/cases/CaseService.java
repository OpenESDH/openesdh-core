package dk.openesdh.repo.services.cases;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;

import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.services.HasStatus;

/**
 * Created by torben on 19/08/14.
 */
public interface CaseService extends HasStatus {
    String DATE_FORMAT = "yyyyMMdd";

    //Folder root contexts (i.e. the next 3 variables)
    String CASES_ROOT = "cases";
    String CASES_TYPES_ROOT = "types";
    String OPENESDH_ROOT_CONTEXT_PATH = "/app:company_home/oe:OpenESDH/oe:cases/";

    Pattern CASE_ID_PATTERN = Pattern.compile("\\d+-(\\d+)");

    String CASE = "Case";

    String CREATOR = "Creator";

    String WRITER = "Writer";

    String READER = "Reader";

    /**
     * Get the root folder for storing cases
     *
     * @return NodeRef for the root folder
     */
    NodeRef getCasesRootNodeRef();

    /**
     * Get the roles that are possible to set for the given case.
     *
     * @param caseNodeRef
     * @return Set containing the role names
     */
    Set<String> getRoles(NodeRef caseNodeRef);

    /**
     * Get all roles for the given case (including owners role).
     *
     * @param caseNodeRef
     * @return Set containing the role names
     */
    Set<String> getAllRoles(NodeRef caseNodeRef);

    /**
     * Get the ID number of the case.
     *
     * @param caseNodeRef
     * @return
     */
    String getCaseId(NodeRef caseNodeRef);

    /**
     * Returns a case given the ID number of the case.
     *
     * @param caseId
     * @return the nodeRef for a case corresponding with the ID supplied or null
     */
    NodeRef getCaseById(String caseId);

    /**
     * returns case info data structure given a nodeRef
     *
     * @param caseNodeRef
     * @return
     */
    CaseInfo getCaseInfo(NodeRef caseNodeRef);

    /**
     * Returns a case info data structure given a case id string
     *
     * @param caseId
     * @return
     */
    CaseInfo getCaseInfo(String caseId);

    /**
     * Return whether a user can update case roles.
     *
     * @param user
     * @param caseNodeRef
     * @return
     */
    boolean canUpdateCaseRoles(String user, NodeRef caseNodeRef);

    /**
     * Create a case
     *
     * @param childAssociationRef
     * @return NodeRef to the case
     */
    void createCase(ChildAssociationRef childAssociationRef);

    /**
     * Find or create a folder for a new case
     *
     * @return NodeRef to folder
     */
    NodeRef getCaseFolderNodeRef(NodeRef casesFolderNodeRef);


    /**
     * Return whether a case is locked or not.
     * <p/>
     * This can be due to either the case being closed or archived.
     *
     * @param nodeRef
     * @return
     */
    boolean isLocked(NodeRef nodeRef);

    /**
     * Return whether or not the node is a case node.
     *
     * @param nodeRef
     * @return
     */
    boolean isCaseNode(NodeRef nodeRef);

    /**
     * Return whether or not the node is a doc which exists within a case.
     *
     * @param nodeRef
     * @return
     */
    boolean isCaseDocNode(NodeRef nodeRef);

    /**
     * Get the parent case of the given node, or null if the node does not
     * have a parent which is a case. The parent does not have to be immediate.
     *
     * @param nodeRef
     * @return
     */
    NodeRef getParentCase(NodeRef nodeRef);

    /**
     * Get the documents folder of the given case.
     *
     * @param caseNodeRef
     * @return
     */
    NodeRef getDocumentsFolder(NodeRef caseNodeRef);

    /**
     * Find cases
     *
     * @param filter
     * @param size
     * @return
     */
    public List<CaseInfo> findCases(String filter, int size);

    Map<String, Object> getSearchDefinition(QName caseType);

    public JSONArray buildConstraintsJSON(ConstraintDefinition constraint) throws JSONException;

    public void checkCaseCreatorPermissions(QName caseTypeQName);

    /**
     * Get current user permissions for the case
     * @param caseId
     * @return
     */
    public List<String> getCaseUserPermissions(String caseId);

    /**
     *
     * @param caseNodeRef
     * @throws AccessDeniedException
     */
    public void checkCanUpdateCaseRoles(NodeRef caseNodeRef) throws AccessDeniedException;

    public JSONObject getCaseInfoJson(NodeRef caseNodeRef) throws JSONException;

    public Set<String> getCaseOwnersUserIds(NodeRef caseNodeRef);

    public String getCaseRoleGroupName(String caseId, String role);
}
