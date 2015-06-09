/**
 * Get the nodeRef of the folder to store new cases in.
 * @returns {string}
 */
function getNewCaseFolderNodeRef () {

    var connector = remote.connect("alfresco");
    var companyHome = connector.get("/api/nodelocator/userhome");

    companyHome = eval('(' + companyHome + ')');
    companyHome = companyHome["data"];

    return companyHome["nodeRef"];
}

/**
 * Get the case types
 */
function getCaseTypes () {
	return retrieveCaseTypes("/api/openesdh/casetypes");
}

/**
 * Get case types for current case creator 
 */
function getCaseTypesForCaseCreator(){
	return retrieveCaseTypes("/api/openesdh/casetypes/casecreator");
}

function retrieveCaseTypes(url){
	var connector = remote.connect("alfresco");
    var caseTypes = connector.get(url);

    caseTypes = eval('(' + caseTypes + ')');
    var casesArr = new Array();

    for (var i in caseTypes) {

        var c = caseTypes[i];
        var cObject = {};
        cObject["type"] = c.Prefix;
        cObject["label"] = c.Title;
        casesArr.push(cObject);
    }
    return casesArr;
}

/**
 * Get the role types for a case
 */
function getCaseRoleTypes (nodeRef, caseId) {

    var connector = remote.connect("alfresco");
    var roleTypes = connector.get("/api/openesdh/"+caseId+"/caseroles?nodeRef=" + encodeURIComponent(nodeRef));
    roleTypes = eval('(' + roleTypes + ')');
    return roleTypes;
}

/**
 * Get reader and writer role types for a case
 */
function getCaseReadWriteRoleTypes(nodeRef, caseId){
	var connector = remote.connect("alfresco");
    var roleTypes = connector.get("/api/openesdh/"+caseId+"/casereadwriteroles?nodeRef=" + encodeURIComponent(nodeRef));
    roleTypes = eval('(' + roleTypes + ')');
    return roleTypes;
}

/**
 * Get the permitted role types for cases. Used for the contact roles
 */
function getPermittedRoleTypes () {

    var connector = remote.connect("alfresco");
    var roleTypes = connector.get("/api/openesdh/case/party/permittedRoles");
    roleTypes = eval('(' + roleTypes + ')');
    return roleTypes;
}

/**
 * Get the permitted status types for cases. Used for the case status select control input(s)
 */
//TODO Don't think we need this any longer
function getCaseStatusTypes () {
    var connector = remote.connect("alfresco");
    var states = connector.get("/api/openesdh/case/party/permittedStates");
    states = eval('(' + states + ')');
    return states;
}

/**
 * Get the case nodeRef from caseId
 */
function getCaseNodeRefFromId(caseId){
    //When we're not within the case context this will always be null as CaseService is included in the
    if (caseId == null || caseId == undefined)
        return null;
    var connector = remote.connect("alfresco");
    var caseInfo = connector.get("/api/openesdh/case/noderef/"+caseId);
    caseInfo = eval('(' + caseInfo + ')');

    return caseInfo.caseNodeRef;
}

/**
 * Get the case id from nodeRef
 */
function getCaseIdFromNodeRef(nodeRef){
    if (nodeRef == null || nodeRef == undefined)
        return null;
    var caseId = nodeRef.replace(":/","");
    var connector = remote.connect("alfresco");
    var caseInfo = connector.get("/api/openesdh/documents/isCaseDoc/"+caseId);
    caseInfo = eval('(' + caseInfo + ')');

    return caseInfo.caseId;
}

/*
function getCreateCaseWidgets(){
    var caseContainerNodeRef = getNewCaseFolderNodeRef();
    return [
        {
            name: "alfresco/forms/ControlRow",
            config: {
                description: "",
                title: "",
                fieldId: "33ed6de4-3a60-46bb-8389-40b04aeddd37",
                widgets: [
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            fieldId: "edb22ed0-ch9a-48f4-8f30-c5atjd748ffb",
                            name: "alf_destination",
                            value: caseContainerNodeRef,
                            label: "",
                            unitsLabel: "",
                            description: "",
                            postWhenHiddenOrDisabled: true,
                            noValueUpdateWhenHiddenOrDisabled: false,
                            validationConfig: {
                                regex: ".*"
                            },
                            placeHolder: "",
                            widgets: []
                        },
                        widthPc: "1"
                    },
                    {
                        name: "alfresco/forms/controls/HiddenValue",
                        config: {
                            fieldId: "edb31ed0-c74a-48f4-8f30-c5atbd748ffb",
                            value: "",
                            label: "",
                            unitsLabel: "",
                            description: "",
                            postWhenHiddenOrDisabled: true,
                            noValueUpdateWhenHiddenOrDisabled: false,
                            validationConfig: {
                                regex: ".*"
                            },
                            placeHolder: "",
                            widgets: []
                        },
                        widthPc: "1"
                    }
                ]
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            config: {
                description: "",
                title: "",
                fieldId: "33ed6de4-3a60-46bb-8389-40b04aeddd37",
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "edb19ed0-c74a-48f4-8f30-c5aabd74fffb",
                            name: "prop_cm_title",
                            value: "",
                            label: msg.get("create-case.label.title"),
                            unitsLabel: "",
                            description: "",
                            visibilityConfig: {
                                initialValue: true,
                                rules: []
                            },
                            requirementConfig: {
                                initialValue: false,
                                rules: []
                            },
                            disablementConfig: {
                                initialValue: false,
                                rules: []
                            },
                            postWhenHiddenOrDisabled: true,
                            noValueUpdateWhenHiddenOrDisabled: false,
                            validationConfig: {
                                regex: ".*"
                            },
                            placeHolder: "",
                            widgets: []
                        },
                        widthPc: "50"
                    },
                    {
                        name: "alfresco/forms/controls/DojoSelect",
                        config: {
                            id: "prop_oe_status",
                            label: msg.get("create-case.label.button.case-status"),
                            optionsConfig: {
                                fixed: getStatusLabels()
                            },
                            unitsLabel: "",
                            description: "",
                            name: "prop_oe_status",
                            fieldId: "ebf9b987-9744-47ad-8823-ee9d9aa783f4",
                            widgets: []
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            config: {
                description: "",
                title: "",
                fieldId: "88ba8d88-b562-4954-81b9-d34ac564d5ff",
                widgets: [
                    {
                        name: "alfresco/forms/ControlRow",
                        config: {
                            description: "",
                            title: "",
                            fieldId: "31ed6de4-3a60-46bb-83e9-40b04ae0dd37",
                            widgets: [
                                {
                                    name: "openesdh/common/widgets/controls/AuthorityPicker",
                                    config: {
                                        label: "Owner",
                                        name: "assoc_case_owners_added",
                                        itemKey: "nodeRef",
                                        singleItemMode: false
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            config: {
                description: "",
                title: "",
                fieldId: "b0632dac-002e-4860-884b-b9237246075c",
                widgets: [
                    {
                        name: "alfresco/forms/controls/DateTextBox",
                        config: {
                            id: "prop_case_startDate",
                            unitsLabel: "mm/dd/yy",
                            description: "",
                            label: msg.get("create-case.label.button.start-date"),
                            name: "prop_case_startDate",
                            fieldId: "b4bd606f-66ae-4f06-847d-dfdc77f5abc2",
                            widgets: []
                        }
                    },
                    {
                        name: "openesdh/common/widgets/controls/DojoDateExt",
                        config: {
                            id: "prop_case_endDate",
                            unitsLabel: "mm/dd/yy",
                            description: "",
                            label: msg.get("create-case.label.button.end-date"),
                            name: "prop_case_endDate",
                            fieldId: "69707d94-0f8c-4966-832a-a1adbc53b74f",
                            widgets: []
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            config: {
                description: "",
                title: "",
                fieldId: "0b4ab71a-26ce-4df9-839f-c26b12fffecb",
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoTextarea",
                        config: {
                            fieldId: "63854d9e-295a-454d-8c0d-685de6f68d71",
                            name: "prop_cm_description",
                            value: "",
                            label: "Description",
                            unitsLabel: "",
                            description: "",
                            visibilityConfig: {
                                initialValue: true,
                                rules: []
                            },
                            requirementConfig: {
                                initialValue: false,
                                rules: []
                            },
                            disablementConfig: {
                                initialValue: false,
                                rules: []
                            },
                            postWhenHiddenOrDisabled: true,
                            noValueUpdateWhenHiddenOrDisabled: false,
                            widgets: []
                        },
                        widthPc: "98"
                    }
                ]
            }
        }
    ]
}*/
