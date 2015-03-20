/**
 * A grid to show the list of notes for a node.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang",
        "dojo/on",
        'put-selector/put',
        "alfresco/core/NodeUtils"
    ],
    function(declare, DGrid, lang, on, put, NodeUtils) {
        return declare([DGrid], {
            i18nRequirements: [
                {i18nFile: "./i18n/NotesGrid.properties"}
            ],

            showPagination: false,
            showColumnHider: false,
            allowColumnReorder: false,
            showHeader: false,

            renderRow: function(item, options) {
                var div = put('div');
                div.innerHTML = '<div class="name">' + item.created + ': ' + item.author + '</div><div class="summary">' + item.content + '</div>';
                return div;
            },

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