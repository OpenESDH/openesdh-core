/**
 * A grid to show the cases related to the user.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin"
    ],
    function(declare, DGrid, lang, _DocumentTopicsMixin) {
        return declare([DGrid, _DocumentTopicsMixin], {
            i18nRequirements: [
                {i18nFile: "./i18n/DocumentAttachmentsGrid.properties"}
            ],

            /**
             * An array containing the actions which should be available on all
             * result rows.
             *
             * @instance
             * @type {object[]}
             */
            actions: [
                {
                    "callback" : "onPreviewDoc",
                    "id" : "doc-preview",
                    "label" : "grid.actions.preview_doc",
                    "key" : "13"
                }
            ],
            
            constructor: function (args) {
                lang.mixin(this, args);
                this.initActions();
            },
            
            initActions: function(){
            	if(this.isReadOnly){
            		return;
            	}
            	this.actions.push(
	    			{"href" : "document-details?nodeRef={nodeRef}",
	                    "id" : "doc-details",
	                    "label" : "grid.actions.doc_details",
	                    "key" : "68", // Shift+D
	                    "shift": true}
    			);
            },

            /**
             * The nodeRef of the document for which we want to retrieve its attachments
             */
            nodeRef: "",

            /**
             * The target URI for the store
             */
            targetURI: "api/openesdh/case/documentAttachments?nodeRef=",

            onPreviewDoc: function (item) {
                // TODO: Use the nodeRef of the main document
                this.alfPublish("OE_PREVIEW_DOC", {
                    nodeRef: item.nodeRef,
                    displayName: item['cm:title'] ? item['cm:title'] : item['cm:name']
                });
            },
            postMixInProperties: function () {
                this.inherited(arguments);
                this.alfSubscribe(this.ReloadAttachmentsTopic, lang.hitch(this, "_onRefresh"));
                this.alfSubscribe(this.CaseDocumentMoved, lang.hitch(this, "_onCaseDocumentMoved"));
            },

            getColumns: function () {
                return [
                    { field: "oe:caseId", label: this.message("oe_caseId") },
                    { field: "cm:title", label: this.message("table.header.label.title"),
                        renderCell: lang.hitch(this, '_renderTitleCell')
                    },
                    { field: "cm:versionLabel", label: this.message("table.header.label.version"),
                        formatter: lang.hitch(this, "_formatVersion")
                    },
                    { field: "cm:creator", label: this.message("table.header.label.addedBy") },

                    { field: "cm:created", label: this.message("table.header.label.created"),
                        formatter: lang.hitch(this, "_formatDate")
                    },
                    { field: "cm:modified", label: this.message("table.header.label.modified"),
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
            },

            /**
             * Override the refresh method in DGrid.js to allow re-stitching of the nodeRef to the target URI.
             */
            _onRefresh: function (payload) {
                var temp = this.targetURI;
                this.targetURI += payload.nodeRef;
                this.grid.store = this.createStore();
                //console.log("openesdh/pages/case/widgets/DocumentAttachmentsGrid.js(90) Refresh called.");
                this.grid.refresh();
                this.targetURI = temp;//Revert the targetURI back to its original state
            },
            
            /**
             * Empty attachments grid when document has been moved to another case
             */
            _onCaseDocumentMoved: function(){
            	this.grid.store = this.createStore();
            	this.grid.refresh();
            }
        });
    });