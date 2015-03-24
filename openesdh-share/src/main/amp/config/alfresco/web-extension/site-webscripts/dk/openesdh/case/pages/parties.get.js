<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">

var args = page.url.args;

var caseId = url.templateArgs.caseId;
var caseNodeRef = getCaseNodeRefFromId(caseId);

var roleTypes = getPermittedRoleTypes();

var addAuthorityToRoleDropdownItems = [];
for (var i = 0; i < roleTypes.length; i++) {
    var roleType = roleTypes[i];
    addAuthorityToRoleDropdownItems.push({
        name: "alfresco/menus/AlfMenuBarItem",
        id: "CASE_PARTY_ADD_CONTACT_" + roleType.toUpperCase(),
        config: {
            label: msg.get("roles." + roleType.toLowerCase()),
            publishTopic: "CASE_ADD_CONTACT_TO_ROLE_CLICK",
            publishPayload: {
                role: roleType,
                caseId: caseId
            }
        }
    });
}

model.jsonModel = {
    widgets: [
        {
            id: "SET_PAGE_TITLE",
            name: "alfresco/header/SetTitle",
            config: {
                title: msg.get("header.title")
            }
        },
        {
            name: "alfresco/layout/VerticalWidgets",
            config: {
                widgets: [
                    {
                        name: "alfresco/layout/LeftAndRight",
                        config: {
                            widgets: [
                                {
                                    name: "alfresco/html/Label",
                                    id:"CASE_GROUP_MEMBERS_MENU_LABEL",
                                    align: "left",
                                    config: {
                                        label: msg.get("case-parties.heading")
                                    }
                                },
                                {
                                    name: "alfresco/menus/AlfMenuBar",
                                    id: "ADD_USERS_GROUPS_BUTTON",
                                    align: "right",
                                    config: {
                                        widgets: [
                                            {
                                                name: "alfresco/menus/AlfMenuBarPopup",
                                                id: "CASE_MEMBERS_ADD_AUTHORITIES",
                                                config: {
                                                    label: msg.get("case-parties.invite-people"),
                                                    widgets: addAuthorityToRoleDropdownItems,
                                                    visibilityConfig: {
                                                        initialValue: true, //TODO setting this property to false hides the buttons need to fix
                                                        rules: [
                                                            {
                                                                topic: "CASE_INFO",
                                                                attribute: "isJournalized",
                                                                isNot: [true]
                                                            }
                                                        ]
                                                    }
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    },
                    {
                        name: "openesdh/pages/case/members/ContactList",
                        config: {
                            roleTypes: roleTypes,
                            caseId: caseId
                        }
                    }
                ]
            }
        }
    ],
    services: [
        {
            name: "openesdh/common/services/CaseMembersService",
            config: {
                nodeRef: caseNodeRef,
                caseId: caseId
            }
        }

    ]
};