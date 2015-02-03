/**
 * @module openesdh/pages/documents/views/MainDocumentDetails
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/dom-construct",
        "dojo/text!./templates/OpenESDHDocDetailsWidget.html",
        "dojo/_base/lang"],
    function (declare, _WidgetBase, Core, CoreWidgetProcessing, _TemplatedMixin, domConstruct, template, lang ) {
        return declare([_WidgetBase, Core, CoreWidgetProcessing, _TemplatedMixin], {

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/AlfDocumentListView.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/MainDocumentDetails.properties"}],

            /**
             * An array of the CSS files to use with this widget.
             *
             * @instance cssRequirements {Array}
             * @type {object[]}
             * @default [{cssFile:"./css/AlfDialog.css"}]
             */
            //cssRequirements: [{cssFile:"./css/AlfDocumentListView.css"}],

            /**
             * The HTML template to use for the widget.
             * @instance
             * @type {String}
             */
            templateString: template,

            /**
             * This is the topic that will be subscribed to
             *
             * @instance
             * @type {string}
             * @default "OPENESDH_RETRIEVE_CASE_DOCUMENT_DETAILS"
             */
            subscriptionTopic: "ALF_RETRIEVE_DOCUMENTS_REQUEST_SUCCESS",

            /**
             * The property of the response item from the server
             */
            detailObject: "response.caseDetails",

            bodyNode: null,

            widgets: [],

            /**
             * Disable drag and drop file upload.
             *
             * @instance
             */
            buildRendering: function alfresco_documentlibrary__AlfDndDocumentUploadMixin__constructor() {
                this.inherited(arguments);
                this.alfSubscribe(this.subscriptionTopic, lang.hitch(this, this.onMainDocumentsLoaded));
            },

            onMainDocumentsLoaded : function openesdh_caselibrary_CaseDetails(payload) {

                domConstruct.empty(this.bodyNode);

                var details = lang.getObject(this.detailObject, false, payload);

                this.widgets = [];
                var spacerWidget ={
                    name: "alfresco/html/Spacer",
                    config:{
                        width: "14px;",
                        height:"10px"
                    }
                };

                for(var propertyName in details) {
                    if (details.hasOwnProperty(propertyName)) {
                        var propertyWidget ={};
                        var label = "label."+propertyName;
                            propertyWidget = {
                                name: "alfresco/renderers/Property",
                                config: {
                                    currentItem: details,
                                    renderedValuePrefix: this.message(label)+": ",
                                    propertyToRender: propertyName
                                }
                            };
                        this.widgets.push(spacerWidget);
                        this.widgets.push(propertyWidget);
                    }

                }
                this.processWidgets(this.widgets, this.bodyNode);
            }
        });
    })
;
