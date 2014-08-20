<import resource="classpath:/alfresco/web-extension/utils/case.js">//</import>

var caseTypes = getCaseTypes();

var baseType = "case:base";

model.jsonModel = {
    services: [
        {
            name: "openesdh/search/CaseNavigationService"
        }
    ],
    widgets: [
        {
            id: "SET_PAGE_TITLE",
            name: "alfresco/header/SetTitle",
            config: {
                title: msg.get("openesdh.page.search.label")
            }
        },
        {
            id: "SHARE_VERTICAL_LAYOUT",
            name: "alfresco/layout/VerticalWidgets",
            config: {
                widgets: [
                    {
                        name: 'alfresco/layout/VerticalWidgets',
                        config: {
                            widgets: [
                                {
                                    name: "openesdh/search/CaseFilterPane",
                                    config: {
                                        caseTypes: caseTypes
                                    }
                                },
                                {
                                    name: "openesdh/search/CaseGrid",
                                    config: {
                                        baseType: baseType,
                                        caseTypes: caseTypes
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    ]

};


