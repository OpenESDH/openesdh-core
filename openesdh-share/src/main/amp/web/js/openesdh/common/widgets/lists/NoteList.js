define(["dojo/_base/declare",
        "alfresco/lists/AlfList",
        "dojo/_base/lang",
        "alfresco/core/NodeUtils"],
    function (declare, AlfList, lang, NodeUtils) {
        return declare([AlfList], {

            /**
             * The nodeRef to load comments for.
             * @instance
             * @type {string}
             * @default null
             */
            nodeRef: null,

            loadDataPublishTopic: "ALF_CRUD_GET_ALL",

            itemsProperty: "",

            i18nRequirements: [
                {i18nFile: "./i18n/NoteList.properties"}
            ],

            waitForPageWidgets: false,

            widgets: [
                {
                    name: "openesdh/common/widgets/lists/views/NotesView"
                }
            ],

            updateLoadDataPayload: function (payload) {
                this.inherited(arguments);
                payload.url = "api/openesdh/node/" + NodeUtils.processNodeRef(this.nodeRef).uri + "/notes";
            }
        });
    });