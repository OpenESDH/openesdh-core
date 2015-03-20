/**
 * A grid to show the list of notes for a node.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang",
        "dojo/on",
        "alfresco/core/NodeUtils"
    ],
    function(declare, DGrid, lang, on, NodeUtils) {
        return declare([DGrid], {
            i18nRequirements: [
                {i18nFile: "./i18n/NotesGrid.properties"}
            ],

            showPagination: false,
            showColumnHider: false,
            allowColumnReorder: false,

            postMixInProperties: function () {
                this.inherited(arguments);
                this.targetURI = "api/openesdh/node/" + NodeUtils.processNodeRef(this.nodeRef).uri + "/notes";
            },

            getColumns: function () {
                return [
                    { field: "created", label: this.message("created")  },
                    { field: "author", label: this.message("author")},
                    { field: "content", label: this.message("content")}
                ];
            }
        });
    });