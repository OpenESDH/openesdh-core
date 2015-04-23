package dk.openesdh.repo.webscripts.workflows;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
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
    private PersonService personService;

    public void setServiceRegistry(ServiceRegistry registry) {
        this.registry = registry;
        this.workflowService = registry.getWorkflowService();
        this.nodeService = registry.getNodeService();
        this.authorityService = this.registry.getAuthorityService();
        this.personService = this.registry.getPersonService();

    }

    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String caseId = req.getParameter("caseId");
        String status = req.getParameter("status");
        if (status == null) {
            status = "";
        }

        WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();

        Map<QName, Object> processCustomProps = new HashMap<QName, Object>();

//        processCustomProps.put(QName.createQName(
//                "http://www.magenta-aps.dk/model/oeworkflow/1.0", "caseId"), caseId);

//        tasksQuery.setProcessCustomProps(processCustomProps);

        tasksQuery.setWorkflowDefinitionName("activiti$openE-CaseTaskUser");

        List<WorkflowTask> tasks = new ArrayList<>();

		/* If the status is empty, perform two queries so we get both open and closed tasks */
        if (status.isEmpty()) {

            tasksQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
    //        List<WorkflowInstance> openTasks = workflowService.getWorkflows();
            List<WorkflowTask> openTasks = workflowService.queryTasks(tasksQuery, true);

            tasksQuery.setTaskState(WorkflowTaskState.COMPLETED);
            tasksQuery.setActive(false);
            List<WorkflowInstance> closedTasks = workflowService.getWorkflows();

            tasks.addAll(openTasks);
//            tasks.addAll(closedTasks);

        } else {

            if (status.equals("open")) {
                tasksQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            } else if (status.equals("closed")) {
                tasksQuery.setActive(false);
                tasksQuery.setTaskState(WorkflowTaskState.COMPLETED);
            }
//            tasks = workflowService.queryTasks(tasksQuery);
        }

        try {
            // build a json object
            JSONArray obj = new JSONArray();

            QName OWNER = QName.createQName("http://www.alfresco.org/model/content/1.0", "owner");

            QName STATUS = QName.createQName("http://www.alfresco.org/model/bpm/1.0", "status");

            QName DESCRIPTION = QName.createQName("http://www.alfresco.org/model/bpm/1.0", "description");

            QName pooledActors = QName.createQName("http://www.alfresco.org/model/bpm/1.0", "pooledActors");

            QName tmpGroupQName = QName.createQName("http://www.magenta-aps.dk/model/esdhworkflow/1.0", "tmpGroup");


            for (WorkflowTask t : tasks) {
                if (t.getName().equals("esdhwf:completedPhaseTask") == true && t.getProperties().get(STATUS).equals("Completed")) {
                    continue;
                }


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
                NodeRef assigneeNR = (NodeRef) t.getProperties().get(WorkflowModel.ASSOC_ASSIGNEE);
                PersonService.PersonInfo assignee = this.personService.getPerson(assigneeNR);
                PersonService.PersonInfo initiator = this.personService.getPerson(nr);

                JSONObject task = new JSONObject();
//                task.put("name", t.getName());
                task.put("id", t.getId());
                task.put("type", t.getTitle());
                task.put("name", assignee.getFirstName() +" "+ assignee.getLastName()+" ("+assignee.getUserName()+")");
                task.put("owner", t.getProperties().get(OWNER));
                task.put("description", t.getProperties().get(DESCRIPTION));
                task.put("initiator",  initiator.getFirstName() +" "+ initiator.getLastName()+" ("+initiator.getUserName()+")");
                task.put("status", taskStatus);
                obj.put(task);
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
