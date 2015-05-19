/**
 * A grid to show documents.
 * Currently, fetches documents related to a case.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/_base/lang",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin"
    ],
    function(declare, DGrid, lang, _TopicsMixin) {
        return declare([DGrid, _TopicsMixin], {
            cssRequirements: [
                {cssFile: "./css/DocumentGrid.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/DocumentGrid.properties"}
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
                    "key" : "13"},
                    
                // TODO: use widgets!
                {"href" : "edit-metadata?nodeRef={nodeRef}",
                    "id" : "case-edit",
                    "label" : "grid.actions.edit_doc",
                    "key" : "69", // Shift+E
                    "shift": true},
                {"href" : "document-details?nodeRef={mainDocNodeRef}",
                    "id" : "doc-details",
                    "label" : "grid.actions.doc_details",
                    "key" : "68", // Shift+D
                    "shift": true},
                
                {"callback" : "onMoveDoc",
                    "id" : "doc-move",
                    "label" : "grid.actions.move_doc",
                    "key"	: "77", // Shift+M
                    "shift" : true
                },                
                {"callback" : "onCopyDoc",
                    "id" : "doc-copy",
                    "label" : "grid.actions.copy_doc",
                    "key"	: "67", // Shift+C
                    "shift" : true
                },
            ],

            onPreviewDoc: function (item) {
                // TODO: Use the nodeRef of the main document
                this.alfPublish("OE_PREVIEW_DOC", {
                    nodeRef: item.mainDocNodeRef,
                    displayName: item['cm:title'] ? item['cm:title'] : item['cm:name']
                });
            },
            
            onMoveDoc : function (item) {
            	this.alfPublish(this.MoveDocumentTopic, {
                    nodeRef: item.mainDocNodeRef,
                    name: item["cm:name"],
                    caseId: item["oe:caseId"],
                    nodeUuid: item["sys:node-uuid"]
                });
            },
            
            onCopyDoc : function (item) {
            	this.alfPublish(this.CopyDocumentTopic, {
                    nodeRef: item.mainDocNodeRef,
                    name: item["cm:name"],
                    caseId: item["oe:caseId"],
                    nodeUuid: item["sys:node-uuid"]
                });
            },
            
            postMixInProperties: function () {
                this.inherited(arguments);
                this.targetURI = "api/openesdh/casedocumentssearch?nodeRef=" + this.nodeRef;
            },

            getColumns: function () {
                return [
                    { field: "doc:type", label: this.message("table.header.label.type"), renderCell: lang.hitch(this, '_renderDocType') },
                    { field: "doc:category", label: this.message("table.header.label.category"), renderCell: lang.hitch(this, '_renderDocCategory')  },
                    { field: "doc:state", label: this.message("table.header.label.state"), renderCell: lang.hitch(this, '_renderDocState')  },
                    { field: "cm:title", label: this.message("table.header.label.title"), renderCell: lang.hitch(this, '_renderTitleCell') },
                    { field: "mainDocVersion", label: this.message("table.header.label.version"), formatter: lang.hitch(this, "_formatVersion") },
                    { field: "doc:owner", label: this.message("table.header.label.owner") },
                    { field: "cm:created", label: this.message("table.header.label.created"), formatter: lang.hitch(this, "_formatDate") },
                    { field: "cm:modified", label: this.message("table.header.label.modified"), formatter: lang.hitch(this, "_formatDate") }
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
            _renderTitleCell: function (item, value, node, options) {
                node.innerHTML = item['cm:title'] ? item['cm:title'] : item['cm:name'];
            },

            _renderDocType: function (item, value, node, options) {
                if(value && value != undefined && value != "")
                    node.innerHTML = this.message("document.type.constraint.label."+value);
            },

            _renderDocState: function (item, value, node, options) {
                if(value && value != undefined && value != "")
                    node.innerHTML = this.message("document.state.constraint.label."+value);
            },

            _renderDocCategory: function (item, value, node, options) {
                if(value && value != undefined && value != "")
                    node.innerHTML = this.message("document.category.constraint.label."+value);
            }
        });
    });