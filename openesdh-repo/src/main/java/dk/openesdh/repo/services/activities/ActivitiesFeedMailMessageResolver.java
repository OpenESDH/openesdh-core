package dk.openesdh.repo.services.activities;

import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_NEW_VERSION_UPLOAD;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_UPLOAD;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_NEW_VERSION_UPLOAD;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_UPLOAD;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_MEMBER_ADD;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_MEMBER_REMOVE;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_UPDATE;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_CANCEL;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_START;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_TASK_APPROVE;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_TASK_END;
import static dk.openesdh.repo.services.activities.CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_TASK_REJECT;
import static dk.openesdh.repo.services.activities.CaseActivityService.ATTACHMENT_TITLE;
import static dk.openesdh.repo.services.activities.CaseActivityService.CASE_ID;
import static dk.openesdh.repo.services.activities.CaseActivityService.CASE_TITLE;
import static dk.openesdh.repo.services.activities.CaseActivityService.DOC_TITLE;
import static dk.openesdh.repo.services.activities.CaseActivityService.MEMBER;
import static dk.openesdh.repo.services.activities.CaseActivityService.MODIFIER_DISPLAY_NAME;
import static dk.openesdh.repo.services.activities.CaseActivityService.ROLE;
import static dk.openesdh.repo.services.activities.CaseActivityService.WORKFLOW_DESCRIPTION;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.alfresco.processor.Processor;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.smtp.MailFreeMarkerProcessorProxyFactory;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@Component(ActivitiesFeedMailMessageResolver.BEAN_ID)
public class ActivitiesFeedMailMessageResolver extends BaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    public static final String BEAN_ID = "ActivitiesFeedMailMessageResolver";

    private static final Logger logger = LoggerFactory.getLogger(ActivitiesFeedMailMessageResolver.class);

    private Map<String, MessageResolver> resolvers = new HashMap<>();

    @Autowired
    @Qualifier(MailFreeMarkerProcessorProxyFactory.BEAN_ID)
    @Override
    public void setProcessor(Processor processor) {
        super.setProcessor(processor);
    }

    @PostConstruct
    @Override
    public void register() {
        setExtensionName("activitymessage");
        initResolvers();
        super.register();
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        try {
            if (arguments.isEmpty()) {
                return "";
            }
            Map<String, Object> activity = (Map<String, Object>) ((SimpleHash) arguments.get(0)).toMap();
            return getResolver(activity)
                    .map(resolver -> resolver.resolve(activity))
                    .orElse("No resolver for activity type " + activityType(activity));
        } catch (Exception ex) {
            logger.error("Error resolving activity message: ", ex);
            throw ex;
        }
    }

    public void registerMesageResolver(String activityType, MessageResolver resolver) {
        resolvers.put(activityType, resolver);
    }

    private Optional<MessageResolver> getResolver(Map<String, Object> activity) {
        return Optional.ofNullable(resolvers.get(activityType(activity)));
    }

    private void initResolvers() {
        resolvers.put(ACTIVITY_TYPE_CASE_UPDATE, new MessageResolver(MODIFIER_DISPLAY_NAME, CASE_TITLE, CASE_ID));
        resolvers.put(ACTIVITY_TYPE_CASE_MEMBER_ADD, new MessageResolver(MODIFIER_DISPLAY_NAME, MEMBER, ROLE, CASE_TITLE, CASE_ID));
        resolvers.put(ACTIVITY_TYPE_CASE_MEMBER_REMOVE, new MessageResolver(MODIFIER_DISPLAY_NAME, MEMBER, ROLE, CASE_TITLE, CASE_ID));
        resolvers.put(ACTIVITY_TYPE_CASE_DOCUMENT_UPLOAD, new MessageResolver(MODIFIER_DISPLAY_NAME, DOC_TITLE, CASE_TITLE, CASE_ID));
        resolvers.put(ACTIVITY_TYPE_CASE_DOCUMENT_NEW_VERSION_UPLOAD, new MessageResolver(MODIFIER_DISPLAY_NAME, DOC_TITLE, CASE_TITLE, CASE_ID));
        resolvers.put(ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_UPLOAD, new MessageResolver(MODIFIER_DISPLAY_NAME, ATTACHMENT_TITLE, DOC_TITLE, CASE_TITLE, CASE_ID));
        resolvers.put(ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_NEW_VERSION_UPLOAD, new MessageResolver(MODIFIER_DISPLAY_NAME, ATTACHMENT_TITLE, DOC_TITLE, CASE_TITLE, CASE_ID));
        
        MessageResolver workflowMessageResolver = new MessageResolver(MODIFIER_DISPLAY_NAME, WORKFLOW_DESCRIPTION, CASE_TITLE, CASE_ID);
        resolvers.put(ACTIVITY_TYPE_CASE_WORKFLOW_START, workflowMessageResolver);
        resolvers.put(ACTIVITY_TYPE_CASE_WORKFLOW_CANCEL, workflowMessageResolver);
        resolvers.put(ACTIVITY_TYPE_CASE_WORKFLOW_TASK_END, workflowMessageResolver);
        resolvers.put(ACTIVITY_TYPE_CASE_WORKFLOW_TASK_APPROVE, workflowMessageResolver);
        resolvers.put(ACTIVITY_TYPE_CASE_WORKFLOW_TASK_REJECT, workflowMessageResolver);
    }

    private static String activityType(Map<String, Object> activity) {
        return (String) activity.get(ACTIVITY_TYPE);
    }

    public static class MessageResolver {

        private String[] paramNames;

        public MessageResolver(String... paramNames) {
            this.paramNames = paramNames;
        }

        public String resolve(Map<String, Object> activity) {
            Map<String, String> summary = (Map<String, String>) activity.get("activitySummary");
            Object[] params = Arrays.stream(this.paramNames)
                    .map(summary::get)
                    .toArray();

            return I18NUtil.getMessage(activityType(activity), params);
        }
    }
}
