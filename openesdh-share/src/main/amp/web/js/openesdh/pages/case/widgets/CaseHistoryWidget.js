/**
 * A grid to show the history of a case
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin"
    ],
    function(declare, DGrid, lang, _TopicsMixin) {
        return declare([DGrid, _TopicsMixin], {
            i18nRequirements: [
                {i18nFile: "./i18n/CaseHistoryWidget.properties"}
            ],

            cssRequirements: [
                {cssFile:"./css/CaseHistoryWidget.css"}
            ],

            allowRowSelection: false,
            allowColumnReorder: false,
            showColumnHider: false,

            additionalCssClasses: "CaseHistoryWidget",
            
            buildRendering: function dk_openesdh_pages_case_widgets_CaseHistoryWidget__buildRendering() {
            	this.alfSubscribe(this.NotesTopicsScope + this.NoteCreatedTopic, lang.hitch(this, "_onCaseNoteCreated"));
            	this.inherited(arguments);
            },
            
            _onCaseNoteCreated: function(){
            	this.onRefresh();
            },

            postMixInProperties: function () {
                this.inherited(arguments);
                this.targetURI = "api/openesdh/casehistory?nodeRef=" + this.nodeRef;
            },

            getColumns: function () {
                return [
                    { field: "action", label: this.message("casehistory.column.action"), sortable: false},
                    { field: "type", label: this.message("casehistory.column.type"), sortable: false},
                    { field: "user", label: this.message("casehistory.column.user"), sortable: false},
                    {
                        field: "time", label: this.message("casehistory.column.time"),
                        formatter: lang.hitch(this, '_formatDateTime'),
                        sortable: false
                    }
                ];
            }
        });
    });