define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/InfoWidget.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "openesdh/pages/_TopicsMixin",
        "openesdh/pages/case/widgets/lib/JSONProcessing",
        "openesdh/common/widgets/renderers/PropertyField"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, array, _TopicsMixin, JSONProcessing, PropertyField) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _TopicsMixin, JSONProcessing], {
            templateString: template,

            i18nRequirements: [
                {i18nFile: "./i18n/InfoWidget.properties"}
            ],
            
            baseClass: "case-info-widget",

            cssRequirements: [{cssFile:"./css/DocInfoWidget.css"}],

            bodyNode: null,
            widgetsForBody: [],

            buildRendering: function dk_openesdh_pages_case_widgets_InfoWidget__buildRendering() {
                this.caseInfoTopicSubscription = this.alfSubscribe(this.CaseInfoTopic, lang.hitch(this, "_onPayloadReceive"));

                this.bodyTitle = '';
                this.inherited(arguments);
            },

            _onPayloadReceive: function (payload) {
                // Unsubscribe from CaseInfoTopic so we don't show the data twice
                this.alfUnsubscribe(this.caseInfoTopicSubscription);

                var currentItem = payload.allProps.properties;
                this.widgetsForBody = [
                    {
                        name: "openesdh/common/widgets/renderers/PropertyField",
                        config:{
                            currentItem: currentItem,
                            propertyToRender: "oe:id",
                            label: this.message("caseId"),
                            renderOnNewLine: true
                        }
                    },
                    {
                        name: "openesdh/common/widgets/renderers/PropertyField",
                        config:{
                            currentItem: currentItem,
                            propertyToRender: "cm:title",
                            label: this.message("caseTitle"),
                            renderOnNewLine: true
                        }
                    },
                    {
                        name: "openesdh/common/widgets/renderers/PropertyField",
                        config:{
                            currentItem: currentItem,
                            propertyToRender: "oe:status",
                            label: this.message("caseStatus"),
                            renderOnNewLine: true
                        }
                    },
                    {
                        name: "openesdh/common/widgets/renderers/UserNameField",
                        config:{
                            currentItem: currentItem,
                            propertyToRender: "cm:creator",
                            label: this.message("createdBy"),
                            renderOnNewLine: true
                        }
                    },
                    {
                        name: "openesdh/common/widgets/renderers/DateField",
                        config:{
                            currentItem: currentItem,
                            propertyToRender: "cm:created",
                            label: this.message("created"),
                            renderOnNewLine: true
                        }
                    },
                    {
                        name: "openesdh/common/widgets/renderers/UserNameField",
                        config:{
                            currentItem: currentItem,
                            propertyToRender: "base:owners",
                            label: this.message("caseOwners"),
                            renderOnNewLine: true
                        }
                    },
                    {
                        name: "openesdh/common/widgets/renderers/DateField",
                        config:{
                            currentItem: currentItem,
                            propertyToRender: "cm:modified",
                            label: this.message("lastModified"),
                            renderOnNewLine: true
                        }
                    },
                    {
                        name: "openesdh/common/widgets/renderers/PropertyField",
                        config:{
                            currentItem: currentItem,
                            propertyToRender: "cm:description",
                            label: this.message("description"),
                            renderOnNewLine: true
                        }
                    }
                ];
                this.processWidgets(this.widgetsForBody, this.bodyNode);
            }


        });
    })
;
