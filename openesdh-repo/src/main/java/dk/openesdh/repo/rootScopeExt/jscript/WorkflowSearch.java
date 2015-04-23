package dk.openesdh.repo.rootScopeExt.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorkflowSearch extends BaseScopableProcessorExtension {

    private ServiceRegistry registry;
    private WorkflowService workflowService;
    private ValueConverter converter;

    public final static int BOTH = 0;
    public final static int OPEN = 1;
    public final static int CLOSED = 2;

    public void setServiceRegistry(ServiceRegistry registry) {
        this.registry = registry;
        this.workflowService = registry.getWorkflowService();
        this.converter = new ValueConverter();
    }

    private ArrayList<EsdhWorkflowTask> getTasksFromQuery(WorkflowTaskQuery query) {
        ArrayList<EsdhWorkflowTask> result = new ArrayList<EsdhWorkflowTask>();
        List<WorkflowTask> tasks = workflowService.queryTasks(query, true);

        for (WorkflowTask task : tasks) {
            result.add(new EsdhWorkflowTask(task));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Scriptable search(Object customProps, int state) {
        Map<String, String> customProperties = (Map<String, String>) converter.convertValueForJava(customProps);
        Map<QName, Object> processCustomProps = new HashMap<QName, Object>();

        for (Map.Entry<String, String> entry : customProperties.entrySet()) {
            processCustomProps.put(QName.createQName(entry.getKey()), entry.getValue());
        }

        ArrayList<EsdhWorkflowTask> result = new ArrayList<EsdhWorkflowTask>();

        WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
        taskQuery.setProcessCustomProps(processCustomProps);

        if (state == CLOSED) {
            taskQuery.setTaskState(WorkflowTaskState.COMPLETED);
            taskQuery.setActive(false);
            result.addAll(getTasksFromQuery(taskQuery));
        } else if (state == OPEN) {
            taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            result.addAll(getTasksFromQuery(taskQuery));
        } else if (state == BOTH) {
            taskQuery.setTaskState(WorkflowTaskState.COMPLETED);
            taskQuery.setActive(false);
            result.addAll(getTasksFromQuery(taskQuery));

            taskQuery.setActive(true);
            taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            result.addAll(getTasksFromQuery(taskQuery));
        }

        return (Scriptable) converter.convertValueForScript(this.registry, getScope(), null, result);
    }

    public Scriptable search(Object customProps) {
        return search(customProps, BOTH);
    }

}
