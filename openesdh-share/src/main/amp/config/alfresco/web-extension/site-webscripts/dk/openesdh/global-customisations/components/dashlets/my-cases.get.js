model.jsonModel = {
    rootNodeId: args.htmlid,
    pubSubScope: instance.object.id,

    widgets: [
        {
            id: "DASHLET_MY_CASES_WIDGET",
            name: "openesdh/common/widgets/dashlets/MyCasesDashlet"
        }
    ]
};