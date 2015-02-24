/**
 * A grid to show the cases related to the user.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang"
    ],
    function(declare, DGrid, lang) {
        return declare([DGrid], {
            //i18nRequirements: [
            //    {i18nFile: "./i18n/DocumentGrid.properties"}
            //],

            /**
             * An array containing the actions which should be available on all
             * result rows.
             *
             * @instance
             * @type {object[]}
             */
            //actions: [ {"href" : "oe/case/{caseId}/dashboard" ,
            //            "id" : "case-dashboard",
            //            "label" : "grid.actions.goto_case",
            //            "key" : "13"},
            //
            //           {"href" : "edit-metadata?nodeRef={nodeRef}" ,
            //            "id" : "case-edit",
            //            "label" : "grid.actions.edit_case",
            //            "key" : "69",
            //            "shift": true}],

            postCreate: function () {
                this.inherited(arguments);
                this.targetURI = "api/openesdh/documents?nodeRef=" + this.nodeRef;
            },

            getColumns: function () {
                return [
                    { field: "TYPE", label: this.message("oe_type")  },
                    { field: "cm:title", label: this.message("cm_title")  },
                    { field: "sys:versionLabel", label: this.message("sys_versionLabel") },
                    { field: "cm:creator", label: this.message("cm_creator")  },
                    { field: "oe:status", label: this.message("oe_status")},
                    { field: "cm:created", label: this.message("cm_created") },
                    { field: "cm:modified", label: this.message("cm_modified") }
                ];
            }
        });
    });