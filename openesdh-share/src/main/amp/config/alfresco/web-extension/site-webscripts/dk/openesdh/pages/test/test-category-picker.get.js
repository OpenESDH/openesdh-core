model.jsonModel = {
    widgets: [
        {
            name: "alfresco/layout/VerticalWidgets",
            config: {
                widgets: [
                    {
                        name: "openesdh/common/widgets/controls/category/CategoryPickerControl",
                        config: {
                            rootNodeRef: page.url.args.nodeRef
                        }
                    },
                    {
                        name: "openesdh/common/widgets/controls/category/CategoryPickerControl",
                        config: {
                            rootNodeRef: page.url.args.nodeRef,
                            multipleSelect: true
                        }
                    }
                ]
            }
        }
    ]
};