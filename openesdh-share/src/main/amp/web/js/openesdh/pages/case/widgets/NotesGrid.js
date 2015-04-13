/**
 * A grid to show the list of notes for a node.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang",
        "dojo/on",
        'put-selector/put',
        "alfresco/core/NodeUtils",
        "alfresco/core/ObjectTypeUtils"
    ],
    function(declare, DGrid, lang, on, put, NodeUtils, ObjectTypeUtils) {
        return declare([DGrid], {
            cssRequirements: [
                {cssFile: "./css/NotesGrid.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/NotesGrid.properties"}
            ],

            noDataMessage: "notes.grid.no_data_message",

            showPagination: false,
            showColumnHider: false,
            allowColumnReorder: false,
            showHeader: false,

            additionalCssClasses: 'NotesGrid',

            // TODO: Remove this and use Property widget instead
            nonAmdDependencies: ["/js/yui-common.js",
                                 "/js/alfresco.js"],
            // TODO: Remove this and use Property widget instead
            renderUser: function (property) {
                var value = "";
                if (property == null)
                {
                    // No action required if a property isn't supplied
                }
                else if (ObjectTypeUtils.isString(property))
                {
                    value =  this.encodeHTML(property);
                }
                else if (ObjectTypeUtils.isObject(property)) {
                    if (property.hasOwnProperty("userName") && property.hasOwnProperty("displayName")) {
                        value = Alfresco.util.userProfileLink(property.userName, property.displayName);
                    }
                }
                return value;
            },

            renderRow: function(item, options) {
                var div = put('div');
                // TODO: Use widgets instead to render values
                div.innerHTML = '<div class="note-header"><span class="created">'
                    + this._formatDateTime(item.created) + '</span><span class="author">'
                    + this.renderUser(item.author) + '</span></div>' +
                    '<div class="note-content">' + this.encodeHTML(item.content) + '</div>';
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