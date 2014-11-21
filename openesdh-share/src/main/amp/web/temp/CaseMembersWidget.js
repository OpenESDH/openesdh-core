define(["../../../../../target/openesdh-share-war/js/lib/dojo-1.9.0/dojo/_base/declare",
        "alfresco/lists/AlfSortablePaginatedList",
        "openesdh/pages/_TopicsMixin",
        "alfresco/core/JsNode" ],
    function(declare, list,_TopicsMixin, JsNode) {
        return declare([ list,_TopicsMixin], {

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
            sortField: "role",

            loadDataPublishTopic: "ALF_CRUD_GET_ALL",
            constructor: function (args) {
                this.inherited(arguments);
                var caseNodeRef =  new JsNode(this.caseNodeRef).nodeRef.uri;

                console.log("\n\n"+ caseNodeRef);

                this.loadDataPublishPayload = {
                    url: "api/openesdh/case/memberslist/"+caseNodeRef
                };
            },
            itemsProperty: "members"
    });
});
            