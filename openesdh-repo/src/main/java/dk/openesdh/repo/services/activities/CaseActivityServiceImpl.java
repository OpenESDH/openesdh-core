package dk.openesdh.repo.services.activities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.transaction.TransactionService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.services.RunInTransactionAsAdmin;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.members.CaseMembersService;

@Service("CaseActivityService")
public class CaseActivityServiceImpl implements CaseActivityService, RunInTransactionAsAdmin {

    private static final String PREFERENCE_FAVOURITE_CASE = "dk_openesdh_cases_favourites";
    private static final String PREFERENCE_LAST_READ_FEED_ID = "dk_openesdh_last_read_feed_id";

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    @Qualifier("CaseMembersService")
    private CaseMembersService caseMembersService;
    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;
    @Autowired
    @Qualifier("activityService")
    private ActivityService activityService;
    @Autowired
    @Qualifier("PreferenceService")
    private PreferenceService preferenceService;
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;

    @Override
    public void postOnCaseUpdate(NodeRef caseNodeRef) {
        postActivity(caseNodeRef, CaseActivityService.ACTIVITY_TYPE_CASE_UPDATE,
                () -> createNewActivity(caseNodeRef));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnCaseMemberRemove(String caseId, NodeRef authority, String role) {
        postActivity(caseId, CaseActivityService.ACTIVITY_TYPE_CASE_MEMBER_REMOVE, (caseNodeRef) -> {
            JSONObject json = createNewActivity(caseId, caseNodeRef);
            json.put("role", role);
            json.put("member", getAuthorityDisplayName(authority));
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnCaseMemberAdd(String caseId, NodeRef authority, String role) {
        postActivity(caseId, CaseActivityService.ACTIVITY_TYPE_CASE_MEMBER_ADD, (caseNodeRef) -> {
            JSONObject json = createNewActivity(caseId, caseNodeRef);
            json.put("role", role);
            json.put("member", getAuthorityDisplayName(authority));
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnCaseWorkflowStart(String caseId, String description) {
        postActivity(caseId, CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_START, (caseNodeRef) -> {
            JSONObject json = createNewActivity(caseId, caseNodeRef);
            json.put("workflowDescription", description);
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnCaseWorkflowCancel(String caseId, String description) {
        postActivity(caseId, CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_CANCEL, (caseNodeRef) -> {
            JSONObject json = createNewActivity(caseId, caseNodeRef);
            json.put("workflowDescription", description);
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnEndCaseWorkflowTask(String caseId, String description, Optional<String> taskOutcome) {
        String activityType = CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_TASK_
                + taskOutcome.map(outcome -> outcome.toLowerCase()).orElse("end");
        postActivity(caseId, activityType, (caseNodeRef) -> {
            JSONObject json = createNewActivity(caseId, caseNodeRef);
            json.put("workflowDescription", description);
            return json;
        });
    }

    @Override
    public void postOnCaseDocumentUpload(NodeRef documentNodeRef) {
        postCaseDocumentActivity(documentNodeRef, CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_UPLOAD);
    }

    @Override
    public void postOnCaseDocumentNewVersionUpload(NodeRef documentNodeRef) {
        postCaseDocumentActivity(documentNodeRef,
                CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_NEW_VERSION_UPLOAD);
    }

    @Override
    public void postOnCaseDocumentAttachmentUpload(NodeRef attachmentNodeRef) {
        postCaseDocumentAttachmentActivity(attachmentNodeRef,
                CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_UPLOAD);
    }

    @Override
    public void postOnCaseDocumentAttachmentNewVersionUpload(NodeRef attachmentNodeRef) {
        postCaseDocumentAttachmentActivity(attachmentNodeRef,
                CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_NEW_VERSION_UPLOAD);
    }

    @Override
    public PagingResults<ActivityFeedEntity> getCurrentUserActivities(PagingRequest paging) {
        return activityService.getPagedUserFeedEntries(AuthenticationUtil.getFullyAuthenticatedUser(), null, false,
                true,
                -1, paging);
    }

    @Override
    public int countCurrentUserNewActivities() {
        int minFeedId = Optional.ofNullable(getCurrentUserLastReadFeedId())
                .map(maxId -> Integer.parseInt(maxId) + 1)
                .orElse(-1);
        return countCurrentUserActivities(minFeedId);
    }

    @Override
    public void setCurrentUserLastReadActivityFeedId(String feedId) {
        Map<String, Serializable> preferences = new HashMap<>();
        preferences.put(PREFERENCE_LAST_READ_FEED_ID, feedId);
        preferenceService.setPreferences(AuthenticationUtil.getFullyAuthenticatedUser(), preferences);
    }

    private int countCurrentUserActivities(int minFeedId) {
        return activityService.getUserFeedEntries(AuthenticationUtil.getFullyAuthenticatedUser(), null, false,
                true, minFeedId).size();
    }

    @SuppressWarnings("unchecked")
    private void postCaseDocumentActivity(NodeRef documentNodeRef, String activityType) {
        NodeRef caseNodeRef = documentService.getCaseNodeRef(documentNodeRef);
        postActivity(caseNodeRef, activityType, () -> {
            JSONObject json = createNewActivity(caseNodeRef);
            json.put("docRecordNodeRef", documentService.getDocRecordNodeRef(documentNodeRef).toString());
            json.put("docTitle", nodeService.getProperty(documentNodeRef, ContentModel.PROP_TITLE));
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    private void postCaseDocumentAttachmentActivity(NodeRef attachmentNodeRef, String activityType) {
        NodeRef caseNodeRef = documentService.getCaseNodeRef(attachmentNodeRef);
        postActivity(caseNodeRef, activityType, () -> {
            NodeRef docRecord = documentService.getDocRecordNodeRef(attachmentNodeRef);
            JSONObject json = createNewActivity(caseNodeRef);
            json.put("docRecordNodeRef", documentService.getDocRecordNodeRef(attachmentNodeRef).toString());
            json.put("attachmentTitle", nodeService.getProperty(attachmentNodeRef, ContentModel.PROP_NAME));
            json.put("docTitle", nodeService.getProperty(docRecord, ContentModel.PROP_TITLE));
            return json;
        });
    }

    private JSONObject createNewActivity(NodeRef caseNodeRef) {
        return createNewActivity(caseService.getCaseId(caseNodeRef), caseNodeRef);
    }

    @SuppressWarnings("unchecked")
    private JSONObject createNewActivity(String caseId, NodeRef caseNodeRef) {
        PersonInfo currentUserInfo = personService.getPerson(personService.getPerson(AuthenticationUtil
                .getFullyAuthenticatedUser()));
        String currentUserDisplayName = currentUserInfo.getFirstName() + " " + currentUserInfo.getLastName();
        JSONObject json = new JSONObject();
        json.put("caseId", caseId);
        json.put("caseTitle", nodeService.getProperty(caseNodeRef, ContentModel.PROP_TITLE));
        json.put("modifier", AuthenticationUtil.getFullyAuthenticatedUser());
        json.put("modifierDisplayName", currentUserDisplayName);
        return json;
    }

    private String getAuthorityDisplayName(NodeRef authority) {
        if (caseMembersService.isAuthorityPerson(authority)) {
            PersonInfo person = personService.getPerson(authority);
            return person.getFirstName() + " " + person.getLastName();
        }
        return caseMembersService.getAuthorityName(authority);
    }

    private void postActivity(String caseId, String activityType, Function<NodeRef, JSONObject> activityJsonFunction) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        postActivity(caseId, caseNodeRef, activityType, () -> activityJsonFunction.apply(caseNodeRef));
    }
    
    private void postActivity(NodeRef caseNodeRef, String activityType, Supplier<JSONObject> activityJsonSupplier) {
        String caseId = caseService.getCaseId(caseNodeRef);
        postActivity(caseId, caseNodeRef, activityType, activityJsonSupplier);
    }

    private void postActivity(String caseId, NodeRef caseNodeRef, String activityType, Supplier<JSONObject> activityJsonSupplier) {
        Set<String> usersToNotify = getUsersToNotify(caseId, caseNodeRef);
        if (usersToNotify.isEmpty()) {
            return;
        }
        String activity = activityJsonSupplier.get().toJSONString();
        usersToNotify.forEach(userId -> notifyUser(activityType, userId, activity));
    }
    
    private Set<String> getUsersToNotify(String caseId, NodeRef caseNodeRef) {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        Set<String> result = caseService.getCaseOwnersUserIds(caseNodeRef);
        result.addAll(getMembersWithFavouriteCase(caseId, caseNodeRef));
        return result.stream()
                .filter(userId -> !userId.equals(currentUser))
                .collect(Collectors.toSet());
    }
    
    private Set<String> getMembersWithFavouriteCase(String caseId, NodeRef caseNodeRef) {
        return runAsAdmin(() -> {
            return caseMembersService.getMembers(caseNodeRef, false, false)
                    .stream()
                    .filter(member -> caseMembersService.isAuthorityPerson(member))
                    .filter(member -> memberHasFavouriteCase(member, caseId))
                    .collect(Collectors.toSet());
        });
    }
    
    private boolean memberHasFavouriteCase(String userName, String caseId){
        return Optional.ofNullable(getFavouriteCasesPreference(userName))
                    .map(caseIds -> caseIds.toString())
                    .filter(caseIds -> caseIds.contains(caseId))
                    .isPresent();
    }

    private void notifyUser(String activityType, String userId, String jsonData) {
        activityService.postActivity(activityType, null, null, jsonData, userId);
    }

    @Override
    public TransactionService getTransactionService() {
        return null;
    }

    /**
     * This is a copy from the PreferenceService to circumvent permissions
     * issue.
     * 
     * @param userName
     * @return
     */
    private String getFavouriteCasesPreference(String userName) {
        return getUserPreferenceValue(userName, PREFERENCE_FAVOURITE_CASE);
    }

    private String getCurrentUserLastReadFeedId() {
        return getUserPreferenceValue(AuthenticationUtil.getFullyAuthenticatedUser(), PREFERENCE_LAST_READ_FEED_ID);
    }

    private String getUserPreferenceValue(String userName, String preferenceName) {
        NodeRef personNodeRef = this.personService.getPerson(userName);
        if (personNodeRef == null || !this.nodeService.hasAspect(personNodeRef, ContentModel.ASPECT_PREFERENCES)) {
            return null;
        }
        ContentReader reader = this.contentService.getReader(personNodeRef, ContentModel.PROP_PREFERENCE_VALUES);
        if (reader == null) {
            return null;
        }
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonPrefs = (JSONObject) parser.parse(reader.getContentString());
            return (String) jsonPrefs.get(preferenceName);
        } catch (ContentIOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
