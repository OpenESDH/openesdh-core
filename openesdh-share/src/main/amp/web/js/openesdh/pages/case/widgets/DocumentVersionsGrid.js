/**
 * A grid to show the previous versions of selected document.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin"
    ],
    function(declare, DGrid, lang, _DocumentTopicsMixin) {
        return declare([DGrid, _DocumentTopicsMixin], {
            i18nRequirements: [
                {i18nFile: "./i18n/DocumentVersionsGrid.properties"}
            ],

            /**
             * An array containing the actions which should be available on all
             * result rows.
             *
             * @instance
             * @type {object[]}
             */
            actions: [
                {"callback" : "onPreviewDoc",
                    "id" : "doc-preview",
                    "label" : "grid.actions.preview_doc",
                    "key" : "13"}
            ],

            /**
             * The nodeRef of the document for which we want to retrieve its versions
             */
            nodeRef: "",

            /**
             * The target URI for the store
             */
            targetURI: "api/version",


            onPreviewDoc: function (item) {
                // TODO: Use the nodeRef of the main document
                this.alfPublish("OE_PREVIEW_DOC", {
                    nodeRef: item.nodeRef,
                    displayName: item['cm:title'] ? item['cm:title'] : item['name']
                });
            },
            postMixInProperties: function () {
                this.inherited(arguments);
                this.alfSubscribe(this.GetDocumentVersionsTopic, lang.hitch(this, "_onRefresh"));
            },

            getColumns: function () {
                return [

                    { field: "label", label: this.message("version.label.version"), // TODO: i18n!
                        formatter: lang.hitch(this, "_formatVersion")
                    },
                    { field: "createdDate", label: this.message("version.label.created"),
                        formatter: lang.hitch(this, "_formatDate")
                    },
                    { field: "creator", label: this.message("version.label.addedBy"),
                        formatter: lang.hitch(this, "_getCreator")
                    } // TODO: i18n!

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
             * Return the creator name from the object.
             * @param value
             * @returns {*}
             * @private
             */
            _getCreator: function (value) {
                var name=value.firstName+" "+value.lastName+" ("+value.userName+")";
                return name;
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
            },

            /**
             * Override the refresh method in DGrid.js to allow re-stitching of the nodeRef to the target URI.
             */
            _onRefresh: function (payload) {
                var temp = this.targetURI;// To preserve the original state of the URI
                this.targetURI += "?nodeRef="+ payload.nodeRef; //Stitch the nodeRef to the store target URI
                this.grid.store = this.createStore();
                //console.log("openesdh/pages/case/widgets/DocumentVersionsGrid.js(100) Refresh called.");
                this.grid.refresh();
                this.targetURI = temp; //Revert the URI back to its original state
            }
        });
    });