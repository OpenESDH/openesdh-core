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

//                console.log(payload);

                this.widgetsForBody = [];
//                var currentItem = this.unmarshal(payload);
                var properties = payload.properties;
                for (var i in properties) {
                    if (i == "alfTopic") continue;
                    var widget = "";
                    if (properties[i].type == "Date") {
                        widget = "openesdh/common/widgets/renderers/DateField";
                    }
                    else if (properties[i].type == "UserName") {
                        widget = "openesdh/common/widgets/renderers/UserNameField";
                    }
                    else {
                        widget = "openesdh/common/widgets/renderers/PropertyField";
                    }

                    var propertyWidget = {
                        name: widget,
                        config: {
                            currentItem: properties,
                            propertyToRender: i,
                            label: properties[i].label,
                            renderOnNewLine: true
                        }
                    };

                    this.widgetsForBody.push(propertyWidget);
                }
                this.processWidgets(this.widgetsForBody, this.bodyNode);
            }


        });
    })
;
