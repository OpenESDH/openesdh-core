function sortByTitle(workflow1, workflow2) {
    var title1 = (workflow1.title || workflow1.name).toUpperCase(),
        title2 = (workflow2.title || workflow2.name).toUpperCase();
    return (title1 > title2) ? 1 : (title1 < title2) ? -1 : 0;
}
function getCaseWorkflowDefinitions() {
    var connector = remote.connect("alfresco");
    var result = connector.get("/api/openesdh/case/workflow/definitions");
    if (result.status == 200) {
        var workflows = JSON.parse(result).data;
        workflows.sort(sortByTitle);
        return workflows;
    }

    return [];
}

function injectWFItems() {

    model.workflowDefinitions = getCaseWorkflowDefinitions();

    var startWorkflowModule = widgetUtils.findObject(model.widgets, "id", "StartWorkflow");
    startWorkflowModule.options.workflowDefinitions = model.workflowDefinitions;
    startWorkflowModule.options.caseId = (page.url.args.caseId != null) ? page.url.args.caseId : "";
    startWorkflowModule.options.targetCase = (page.url.args.targetCase != null) ? page.url.args.targetCase : "";
    startWorkflowModule.options.targetCasePhase = (page.url.args.targetCasePhase  != null) ? page.url.args.targetCasePhase : "";
}

injectWFItems();