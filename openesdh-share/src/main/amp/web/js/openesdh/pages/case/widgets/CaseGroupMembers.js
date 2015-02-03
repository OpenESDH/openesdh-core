define(["dojo/_base/declare",
        "alfresco/lists/AlfList",
        "dojo/_base/lang",
        "alfresco/documentlibrary/views/AlfTableView",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin" ],
    function(declare, list, tableView, lang, CaseMembersServiceTopics) {
        return declare([list, CaseMembersServiceTopics], {

            /**
             * The size (or number of items) to be shown on each page.
             *
             * @instance
             * @type {number}
             * @default 25
             */
            currentPageSize: 10,

            /**
             * The initial field to sort results on. For historical reasons the default is the "cm:name"
             * property (because the DocumentLibrary was the first implementation of this capability.
             *
             * @instance
             * @type {string}
             * @default "cm:name"
             */
            sortField: "displayName",

            groupShortName: "",

            loadDataPublishTopic: "ALF_CRUD_GET_ALL",

            itemsProperty: "data",

            postMixInProperties: function openesdh_case_renderers__postMixInProperties() {
                this.loadDataPublishPayload = {
                    url: "api/groups/"+this.groupShortName+"/children?authorityType=USER&maxItems=10&&sortBy=authority"
                };

                //Inject the view widgets required by the list widget inherited from
                //alfresco/lists/AlfList#postCreate ~(270)
                this.widgets = [];
                this.widgets.push({name: "openesdh/common/widgets/dashlets/views/UserInfoTableView"} );
            }

        });
});
            