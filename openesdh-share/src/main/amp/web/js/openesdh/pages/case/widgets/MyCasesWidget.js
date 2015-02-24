/**
 * A grid to show the cases related to the user.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang",
        "dojo/on",
        "alfresco/forms/controls/DojoSelect"
    ],
    function(declare, DGrid, lang, on, DojoSelect) {
        return declare([DGrid], {
            i18nRequirements: [
                {i18nFile: "./i18n/MyCasesWidget.properties"}
            ],

            /**
             * An array containing the actions which should be available on all
             * result rows.
             *
             * @instance
             * @type {object[]}
             */
            actions: [ {"href" : "oe/case/{caseId}/dashboard" ,
                        "id" : "case-dashboard",
                        "label" : "grid.actions.goto_case",
                        "key" : "13"},

                       {"href" : "edit-metadata?nodeRef={nodeRef}" ,
                        "id" : "case-edit",
                        "label" : "grid.actions.edit_case",
                        "key" : "69",
                        "shift": true}],


            targetURI: "api/openesdh/userinvolvedsearch",

            showPagination: false,
            showColumnHider: false,
            allowColumnReorder: false,

            getColumns: function () {
                return [
                    { field: "oe:id", label: this.message("mycases.column.id")  },
                    { field: "oe:status", label: this.message("mycases.column.state")},
                    { field: "cm:modified", label: this.message("mycases.column.modified") }
                ];
            }
        });
    });