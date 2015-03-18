/**
 * A grid to show the previous versions of selected document.
 */
define(["dojo/_base/declare",
        "openesdh/common/widgets/grid/DGrid",
        "dojo/on",
        "dijit/registry",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/dom-construct",
        "openesdh/common/widgets/dashlets/_DocumentTopicsMixin"
    ],
    function(declare, DGrid, on, dijitRegistry, lang, array,  domConstruct, _DocumentTopicsMixin) {
        return declare([DGrid, _DocumentTopicsMixin], {
            /**
             * Array of css requirements for the widget
             */
            cssRequirements: [
                {cssFile: "./css/DocumentVersionsGrid.css"}
            ],

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
                {
                    "callback" : "onPreviewDoc",
                    "id" : "doc-preview",
                    "label" : "grid.actions.preview_doc",
                    "key" : "13"
                },
                {
                    "download" : true,
                    "id" : "version-download",
                    "label" : "grid.actions.version.download",
                    "key" : "68", // Shift+D
                    "shift": true
                },
                {
                    "callback" : "onRevert",
                    "id" : "version-revert",
                    "label" : "grid.actions.version.revert"
                }
            ],

            /**
             * The nodeRef of the document for which we want to retrieve its versions
             */
            nodeRef: "",

            /**
             * The target URI for the store
             */
            targetURI: "api/version",


            onRevert: function (item) {
                this.alfPublish(this.DocumentVersionRevertDialog, {
                    documentNodeRef : this.nodeRef,
                    nodeRef : item.nodeRef,
                    version : item.label,
                    revert: true
                });
            },

            onPreviewDoc: function (item) {
                this.alfPublish("OE_PREVIEW_DOC", {
                    nodeRef: item.nodeRef,
                    displayName: item['cm:title'] ? item['cm:title'] : item['name']
                });
            },

            /**
             * Complete override to add the ability to download the doc version
             * if there's a need to use this in more than one place then the DGrid method should be modified
             */
            _renderActionsCell: function (item, value, node, options) {

                var div = domConstruct.toDom('<div style="white-space: nowrap;"></div>');

                array.forEach(this.actions, lang.hitch(this, function (action, i) {
                    var actionElem;
                    var label = this.message(action.label);
                    var href='';
                    if (action.href != null) {
                        href = this.getActionUrl(action, item);
                        actionElem = domConstruct.toDom("<span><a class='magenta ui-icon grid-action action-" + action.id + "' href='" + href + "' title='" + label + "'>" + label + "</a></span>");
                    }
                    else if (action.callback != null && typeof this[action.callback] === "function") {
                        actionElem = domConstruct.toDom("<span><a class='magenta ui-icon grid-action action-" + action.id + "' href='#' title='" + label + "'>" + label + "</a></span>");
                        on(actionElem, "click", lang.hitch(this, function () {
                            this[action.callback].call(this, item);
                        }));
                    }
                    else if (action.download) {
                        console.log("DocumentVersionGrid(87) ping");
                        href = Alfresco.constants.PROXY_URI + 'api/node/content/' + item.nodeRef.replace(":/", "") + '/' + item['name'] + '?a=true';
                        actionElem = domConstruct.toDom("<span><a class='magenta ui-icon grid-action action-" + action.id + "' href='" + href + "' title='" + label + "'>" + label + "</a></span>");
                    }

                    domConstruct.place(actionElem, div);

                }));
                domConstruct.place(div, node);
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
                if(payload.nodeRef != null) {
                    this.targetURI += "?nodeRef=" + payload.nodeRef; //Stitch the nodeRef to the store target URI
                    this.grid.store = this.createStore();
                }
                console.log("openesdh/pages/case/widgets/DocumentVersionsGrid.js(160) Refresh called. "+ this.nodeRef);
                this.grid.refresh();
                this.targetURI = temp; //Revert the URI back to its original state
            }
        });
    });