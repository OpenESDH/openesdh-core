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
                        name: "openesdh/pages/case/members/MembersList"
                    }
                ]
            }
        }
    ],
    services: [
        {
            name: "openesdh/common/services/CaseMembersService",
            config: {
                nodeRef: page.url.args.nodeRef
            }
        }

    ]
};