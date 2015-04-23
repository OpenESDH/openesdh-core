package dk.openesdh.repo.webscripts.workflows;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowSearch extends AbstractWebScript {
    private ServiceRegistry registry;
    private WorkflowService workflowService;
    private NodeService nodeService;
    private AuthorityService authorityService;

    public void setServiceRegistry(ServiceRegistry registry) {
        this.registry = registry;
        this.workflowService = registry.getWorkflowService();
        this.nodeService = registry.getNodeService();
        this.authorityService = this.registry.getAuthorityService();

    }

    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String caseId = req.getParameter("caseId");
        String phase = req.getParameter("phase");
        String status = req.getParameter("status");
        if (status == null) {
            status = "";
        }

        WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();

        Map<QName, Object> processCustomProps = new HashMap<QName, Object>();

        processCustomProps.put(QName.createQName(
                "http://www.alfresco.org/model/bpm/1.0", "caseId"), caseId);

        if (phase != null) {
            processCustomProps.put(QName.createQName("http://www.alfresco.org/model/bpm/1.0", "casePhase"), phase);
        }

        tasksQuery.setProcessCustomProps(processCustomProps);


        List<WorkflowTask> tasks = new ArrayList<WorkflowTask>();

		/* If the status is empty, perform two queries so we get both open and closed tasks */
        if (status.isEmpty()) {

            tasksQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            List<WorkflowTask> openTasks = workflowService.queryTasks(tasksQuery);

            tasksQuery.setTaskState(WorkflowTaskState.COMPLETED);
            tasksQuery.setActive(false);
            List<WorkflowTask> closedTasks = workflowService.queryTasks(tasksQuery);

            tasks.addAll(openTasks);
            tasks.addAll(closedTasks);

        } else {

            if (status.equals("open")) {
                tasksQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            } else if (status.equals("closed")) {
                tasksQuery.setActive(false);
                tasksQuery.setTaskState(WorkflowTaskState.COMPLETED);
            }
            tasks = workflowService.queryTasks(tasksQuery);
        }

        try {
            // build a json object
            JSONObject obj = new JSONObject();

            QName OWNER = QName.createQName("http://www.alfresco.org/model/content/1.0", "owner");

            QName STATUS = QName.createQName("http://www.alfresco.org/model/bpm/1.0", "status");

            QName DESCRIPTION = QName.createQName("http://www.alfresco.org/model/bpm/1.0", "description");

            QName phaseQName = QName.createQName("http://www.alfresco.org/model/bpm/1.0", "casePhase");

            QName pooledActors = QName.createQName("http://www.alfresco.org/model/bpm/1.0", "pooledActors");

            QName tmpGroupQName = QName.createQName("http://www.magenta-aps.dk/model/esdhworkflow/1.0", "tmpGroup");

            for (WorkflowTask t : tasks) {
                if (t.getName().equals("esdhwf:completedPhaseTask") == true && t.getProperties().get(STATUS).equals("Completed")) {
                    continue;
                }

                Object tmpp = t.getProperties().get(phaseQName);


                if (tmpp == null) {
                    continue;
                }
                String tmp = tmpp.toString();

                NodeRef phaseNodeRef = new NodeRef(tmp);

                String phaseName = this.nodeService.getProperty(phaseNodeRef, ContentModel.PROP_NAME).toString();

                NodeRef pooledActorsNodeRef;
                String groupName = "";
                String groupDisplayName = "";
                String taskStatus = (String) t.getProperties().get(STATUS);

                try {
                        ArrayList<NodeRef> pool = (ArrayList<NodeRef>) t.getProperties().get(pooledActors);
                        if (pool.size() > 0) {
                            pooledActorsNodeRef = pool.get(0);
                            groupName = (String) this.nodeService.getProperty(pooledActorsNodeRef, ContentModel.PROP_AUTHORITY_NAME);
                            groupDisplayName = (String) this.nodeService.getProperty(pooledActorsNodeRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
                        }

                        if (t.getName().equals("esdhwf:completedPhaseTask") == true) {

                            taskStatus = "In Progress";
                            pooledActorsNodeRef = (NodeRef) t.getProperties().get(tmpGroupQName);

                            groupName = (String) this.nodeService.getProperty(pooledActorsNodeRef, ContentModel.PROP_AUTHORITY_NAME);
                            groupDisplayName = (String) this.nodeService.getProperty(pooledActorsNodeRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);

                        }

                } catch (Exception e) {
                    System.out.println("Got exception!");
                    e.printStackTrace();
                }


                NodeRef nr = t.getPath().getInstance().getInitiator();

                JSONObject task = new JSONObject();
                task.put("tmp", t.getProperties().get(pooledActors));
                task.put("name", t.getName());
                task.put("phaseName", phaseName);
                task.put("phase", tmp);
                task.put("id", t.getId());
                task.put("owner", t.getProperties().get(OWNER));
                task.put("groupName", groupName);
                task.put("groupDisplayName", groupDisplayName);
                task.put("description", t.getProperties().get(DESCRIPTION));
                task.put("initiator", nr.toString());
                task.put("status", taskStatus);
                obj.append("tasks", task);
            }

            // build a JSON string and send it back
            String jsonString = obj.toString();
            res.setHeader("Content-type", "application/json");
            res.getWriter().write(jsonString);
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }

}
