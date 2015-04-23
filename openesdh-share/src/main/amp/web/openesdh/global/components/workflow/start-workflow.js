Alfresco.component.StartWorkflow.prototype.options.targetCase = "";
Alfresco.component.StartWorkflow.prototype.options.targetCasePhase = "";

Alfresco.component.StartWorkflow.prototype.onReady = function StartWorkflow_onReady() {
    this.widgets.workflowDefinitionMenuButton = Alfresco.util.createYUIButton(this, "workflow-definition-button",
        this.onWorkflowSelectChange, {
            label: this.msg("label.selectWorkflowDefinition"),
            title: this.msg("title.selectWorkflowDefinition"),
            type: "menu",
            menu: "workflow-definition-menu"
        });
    return Alfresco.component.StartWorkflow.superclass.onReady.call(this);
};

Alfresco.component.StartWorkflow.prototype.onObjectFinderReady= function StartWorkflow_onObjectFinderReady(layer, args) {
    var objectFinder = args[1].eventGroup;
    if (objectFinder.options.field == "assoc_packageItems" && objectFinder.eventGroup.indexOf(this.id) == 0) {
        objectFinder.selectItems(this.options.selectedItems);
    }
    if (objectFinder.options.field == "prop_oewf_caseId" && objectFinder.eventGroup.indexOf(this.id) == 0) {
        objectFinder.selectItems(this.options.targetCase);
    }
    if (objectFinder.options.field == "prop_oewf_phase" && objectFinder.eventGroup.indexOf(this.id) == 0) {
        objectFinder.selectItems(this.options.targetCasePhase);
    }

};

