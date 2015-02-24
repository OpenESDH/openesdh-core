/**
 * A grid to show the history of a case
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang"
    ],
    function(declare, DGrid, lang) {
        return declare([DGrid], {
            i18nRequirements: [
                {i18nFile: "./i18n/CaseHistoryWidget.properties"}
            ],

            allowRowSelection: false,
            allowColumnReorder: false,
            showColumnHider: false,

            postMixInProperties: function () {
                this.inherited(arguments);
                this.targetURI = "api/openesdh/casehistory?nodeRef=" + this.nodeRef;
            },

            getColumns: function () {
                return [
                    {
                        field: "time", label: this.message("casehistory.column.time"),
                        formatter: lang.hitch(this, '_formatDate'),
                        sortable: false
                    },
                    { field: "user", label: this.message("casehistory.column.user"), sortable: false},
                    { field: "action", label: this.message("casehistory.column.action"), sortable: false}
                ];
            }
        });
    });