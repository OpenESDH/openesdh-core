<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">

var caseId = url.templateArgs.caseId;
var caseNodeRef = getCaseNodeRefFromId(caseId);
var isReadOnly = !hasWritePermission(caseId);

var caseWorkflowService= {
    name: "openesdh/common/services/CaseWorkflowService",
    config:{
        caseId: caseId,
        nodeRef: (caseNodeRef != null) ? caseNodeRef : args.destination
    }
};

model.jsonModel = {
	    services: [
	               "alfresco/services/CrudService",
	               "openesdh/common/services/CaseMembersService"],
	           widgets: [{

	               name: "openesdh/common/widgets/layout/BootstrapContainer",
	               config: {
	                   widgets: [{
	                       name: "openesdh/common/widgets/layout/BootstrapGrid",
	                       config: {
	                           widgets: [{
	                               name: "alfresco/layout/VerticalWidgets",
	                               columnSize: 6,
	                               config: {
	                                   widgets: [{
	                                       id: "CASE_INFO_DASHLET",
	                                       name: "openesdh/common/widgets/dashlets/CaseInfoDashlet",
	                                       config: {
	                                           additionalCssClasses: "dashlet-case-info"
	                                       }

	                                   }]
	                               }
	                           }, 
	                           {
	                               name: "alfresco/layout/VerticalWidgets",
	                               columnSize: 6,
	                               config: {
	                                   widgets: [{
	                                       id: "CASE_NOTES_DASHLET",
	                                       name: "openesdh/common/widgets/dashlets/NotesDashlet",
	                                       config: {
	                                           caseId: caseId,
	                                           nodeRef: caseNodeRef,
	                                           isReadOnly: isReadOnly
	                                       }
	                                   }

	                                   ]
	                               }
	                           },
	                           {
	                               id: "CASE_DOCUMENTS_DASHLET",
	                               name: "openesdh/common/widgets/dashlets/CaseDocumentsListDashlet",
	                               config: {
	                                   nodeRef: caseNodeRef
	                               }
	                           }
	                           ]
	                       }

	                   }]
	               }
	           }]
	       };


model.jsonModel.services.push(caseWorkflowService);