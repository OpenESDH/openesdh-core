/**
 * A grid to show the list of notes for a node.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/on",
        'put-selector/put',
        "alfresco/core/NodeUtils",
        "alfresco/core/ObjectTypeUtils",
        "alfresco/core/I18nUtils"
    ],
    function(declare, DGrid, lang, array, on, put, NodeUtils, ObjectTypeUtils, I18nUtils) {
        return declare([DGrid], {
            cssRequirements: [
                {cssFile: "./css/NotesGrid.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/NotesGrid.properties"}
            ],

            noDataMessage: "notes.grid.no_data_message",

            showPagination: true,
            showColumnHider: false,
            allowColumnReorder: false,
            showHeader: false,
            pageSizeOptions: null,

            additionalCssClasses: 'NotesGrid',

            // TODO: Remove this and use Property widget instead
            nonAmdDependencies: ["/js/yui-common.js",
                                 "/js/alfresco.js"],
                                 
           pagingActions: [
                       {"callback" : "onNewComment",
                           "id" : "comment-new",
                           "label" : "comments.button.label.new",
                       },
                       /*{"callback" : "onPrintAllComments",
                           "id" : "comment-print",
                           "label" : "comments.button.label.print.all",
                       }*/
                       ],
                       
           onNewComment: function(){
               this.alfPublish("OPENESDH_CASE_COMMENTS_NEW");
           },
           
           onPrintAllComments: function(){
               this.alfPublish("OPENESDH_CASE_COMMENTS_PRINT_ALL");
           },
                                 
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
                    if (property.hasOwnProperty("userName") && property.hasOwnProperty("firstName") && property.hasOwnProperty("lastName")) {
                        value = Alfresco.util.userProfileLink(property.userName, property.firstName + " " + property.lastName);
                    }
                }
                return value;
            },
            
            renderParties: function(parties){
                if(parties == null){
                    return "";
                }
                
                if (ObjectTypeUtils.isString(parties))
                {
                    return parties;
                }
                
                if (ObjectTypeUtils.isArray(parties)) {
                    var result = "";
                    var first = true;
                    array.forEach(parties, lang.hitch(this, function(party, i){
                        if(!first){
                            result += ", "
                        }
                        first = false;
                        result += party.name
                    }));
                    
                    return result;
                }
                
                return "";
            },

            renderRow: function(item, options) {
                var div = put('div');
                // TODO: Use widgets instead to render values
                div.innerHTML = '<div class="note-header"><div class="note-headline">'
                    + item.headline + '</div><div class="note-content">' 
                	+ this.encodeHTML(item.content) + '</div><div class="note-meta"><span class="created">'
                    + this._formatDateTime(item.created) + '</span><span class="author">'
                    + this.renderUser(item.authorInfo) + '</span><span class="concerned-parties">'
                    + this.renderParties(item.concernedPartiesInfo) 
                    +'</span></div></div>';
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