define(["dojo/_base/declare",
        "openesdh/common/widgets/dashlets/views/UserInfoTableView",
        "alfresco/lists/AlfSortablePaginatedList",
        "alfresco/core/NodeUtils" ],
    function(declare, UserInfoTableView, list, NodeUtils) {
        return declare([list], {

            /**
             * The size (or number of items) to be shown on each page.
             *
             * @instance
             * @type {number}
             * @default 25
             */
            currentPageSize: 25,

            /**
             * The initial field to sort results on. For historical reasons the default is the "cm:name"
             * property (because the DocumentLibrary was the first implementation of this capability.
             *
             * @instance
             * @type {string}
             * @default "cm:name"
             */
            sortField: "",

            caseNodeRef: null,

            loadDataPublishTopic: "ALF_CRUD_GET_ALL",

            postMixInProperties : function () {
                this.inherited(arguments);
                //this.caseNodeRef = args.caseNodeRef ;
                var nodeRefUri =  NodeUtils.processNodeRef(this.caseNodeRef).uri;

                this.loadDataPublishPayload = {url: "api/openesdh/case/members/"+nodeRefUri };

                //Inject the view widgets required by the list widget inherited from
                //alfresco/lists/AlfList#postCreate ~(270)
                this.widgets = [];
                this.widgets.push({name: "openesdh/common/widgets/dashlets/views/UserInfoTableView"} );
            },

            itemsProperty: "members"


    });
});
            