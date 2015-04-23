/**
 * A grid to show the workflows of a case.
 */
define([
        "dojo/_base/lang",
        "dojo/_base/declare",
        "alfresco/core/NodeUtils",
        "openesdh/common/widgets/grid/DGrid"
    ],
    function(lang, declare, NodeUtils, DGrid) {
        return declare([DGrid], {
            i18nRequirements: [
                {i18nFile: "./i18n/CaseWorkflowGrid.properties"}
            ],

            /**
             * An array containing the actions which should be available on all
             * result rows.
             *
             * @instance
             * @type {object[]}
             */
            actions: [],

            caseId: null,

            /**
             * The nodeRef of the case for which we require the details
             */
            caseNodeRef: null,

            /**
             * The target URI for the store
             */
            targetURI: "api/openesdh/workflowsearch?caseId=",

            getColumns: function () {
                return [
                    { field: "type", label: this.message("grid.header.label.type") },
                    { field: "name", label: this.message("grid.header.label.name") },
                    { field: "status", label: this.message("grid.header.label.status")}

                ];
            },

            constructor: function (args) {
                this.inherited(arguments);
                this.targetURI += args.caseId;
                console.log("WorkflowGrid: "+ this.targetURI);
            }

        });
    });