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
            actions: [
                //{"href" : "#TODO",
                //        "id" : "doc-preview",
                //        "label" : "grid.actions.preview_doc",
                //        "key" : "13"},

                       // TODO: use widgets!
                       {"href" : "edit-metadata?nodeRef={nodeRef}",
                       "id" : "case-edit",
                       "label" : "grid.actions.edit_doc",
                       "key" : "69",
                       "shift": true}
            ],

            postMixInProperties: function () {
                this.inherited(arguments);
                this.targetURI = "api/openesdh/casedocumentssearch?nodeRef=" + this.nodeRef;
            },

            getColumns: function () {
                return [
                    { field: "doc:type", label: this.message("Type") }, // TODO: i18n!
                    { field: "doc:category", label: this.message("Kategori") }, // TODO: i18n!
                    { field: "doc:state", label: this.message("State") }, // TODO: i18n!
                    { field: "cm:title", label: this.message("cm_title"),
                        renderCell: lang.hitch(this, '_renderTitleCell')
                    },
                    { field: "cm:versionLabel", label: this.message("Version"), // TODO: i18n!
                        formatter: lang.hitch(this, "_formatVersion")
                    },
                    { field: "doc:owner", label: this.message("Ejer") }, // TODO: i18n!
                    { field: "cm:created", label: this.message("cm_created"),
                        formatter: lang.hitch(this, "_formatDate")
                    },
                    { field: "cm:modified", label: this.message("cm_modified"),
                        formatter: lang.hitch(this, "_formatDate")
                    }
                ];
            },

            /**
             * Return the version if it is specified, otherwise "1.0".
             * @param value
             * @returns {*}
             * @private
             */
            _formatVersion: function (value) {
                return value ? value : "1.0";
            },

            /**
             * Render the cm:title if set, otherwise, cm:name.
             * @param item
             * @param value
             * @param node
             * @param options
             * @private
             */
            _renderTitleCell:  function (item, value, node, options) {
                node.innerHTML = item['cm:title'] ? item['cm:title'] : item['cm:name'];
            }
        });
    });