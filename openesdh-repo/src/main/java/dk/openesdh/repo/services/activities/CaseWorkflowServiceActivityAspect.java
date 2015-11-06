package dk.openesdh.repo.services.activities;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.audit.CaseWorkflowReviewOutcomeExtractor;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.workflow.CaseWorkflowService;

@Service("CaseWorkflowServiceActivityAspect")
public class CaseWorkflowServiceActivityAspect implements BeanFactoryAware {

    private static final String WORKFLOW_SERVICE = "WorkflowService";

    @Autowired
    @Qualifier(WORKFLOW_SERVICE)
    private WorkflowService workflowService;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    @Qualifier("CaseActivityService")
    private CaseActivityService activityService;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory.containsBean(WORKFLOW_SERVICE)) {
            NameMatchMethodPointcutAdvisor beforeEndTaskAdvisor = new NameMatchMethodPointcutAdvisor(
                    (MethodBeforeAdvice) this::beforeEndWorkflowTask);
            beforeEndTaskAdvisor.addMethodName("endTask");

            NameMatchMethodPointcutAdvisor beforeCancelWorkflowAdvisor = new NameMatchMethodPointcutAdvisor(
                    (MethodBeforeAdvice) this::beforeCancelWorkflow);
            beforeCancelWorkflowAdvisor.addMethodName("cancelWorkflow");

            Advised proxy = (Advised) beanFactory.getBean(WORKFLOW_SERVICE);
            proxy.addAdvisor(beforeEndTaskAdvisor);
            proxy.addAdvisor(beforeCancelWorkflowAdvisor);
        }

        if (beanFactory.containsBean(CaseWorkflowService.NAME)) {
            NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor(
                    (AfterReturningAdvice) this::afterStartWorkflow);
            advisor.addMethodName("startWorkflow");
            Advised proxy = (Advised) beanFactory.getBean(CaseWorkflowService.NAME);
            proxy.addAdvisor(advisor);
        }
    }

    public void beforeCancelWorkflow(Method method, Object[] args, Object target) {
        String workflowId = (String) args[0];
        WorkflowPath path = workflowService.getWorkflowPaths(workflowId).stream().findAny().get();
        String caseId = getCaseId(path);
        if (caseId == null) {
            return;
        }
        String description = path.getInstance().getDescription();
        activityService.postOnCaseWorkflowCancel(caseId, description);
    }

    public void beforeEndWorkflowTask(Method method, Object[] args, Object target) {
        String taskId = (String) args[0];
        if (taskId.contains(CaseWorkflowService.WORKFLOW_START_TASK_ID)) {
            return;
        }
        WorkflowTask task = workflowService.getTaskById(taskId);
        WorkflowPath path = task.getPath();
        String caseId = getCaseId(path);
        if (caseId == null) {
            return;
        }
        String description = path.getInstance().getDescription();
        Optional<String> reviewOutcome = CaseWorkflowReviewOutcomeExtractor.getTaskOutcome(task)
                .map(obj -> obj.toString());
        activityService.postOnEndCaseWorkflowTask(caseId, description, reviewOutcome);
    }

    public void afterStartWorkflow(Object result, Method method, Object[] args, Object target) {
        WorkflowPath path = (WorkflowPath) result;
        String caseId = getCaseId(path);
        if (caseId == null) {
            return;
        }
        String description = path.getInstance().getDescription();
        activityService.postOnCaseWorkflowStart(caseId, description);
    }

    private String getCaseId(WorkflowPath path) {
        Map<QName, Serializable> props = workflowService.getPathProperties(path.getId());
        return (String) props.get(OpenESDHModel.PROP_OE_CASE_ID);
    }
}
