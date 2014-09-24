define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/InfoWidget.html",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin",
        "openesdh/pages/case/widgets/lib/JSONProcessing"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, _TopicsMixin, JSONProcessing) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _TopicsMixin, JSONProcessing], {
            templateString: template,
            i18nRequirements: [
                {i18nFile: "./i18n/InfoWidget.properties"}
            ],

            bodyNode: null,

            widgetsForBody: [],

            buildRendering: function dk_openesdh_pages_case_widgets_InfoWidget__buildRendering() {
                this.alfSubscribe(this.CaseInfoTopic, lang.hitch(this, "_onPayloadReceive"));

                this.bodyTitle = this.message('bodyTitle');
                this.inherited(arguments);
            },

            _onPayloadReceive: function (payload) {
                console.log(payload);

                this.widgetsForBody = [];
//                var currentItem = this.unmarshal(payload);
                for (var i in payload) {
                    if (i == "alfTopic") continue;
                    var widget = "";
                    if(payload[i].type == "Date") {
                        widget = "openesdh/common/widgets/renderers/DateField";
                    }
                    else if(payload[i].type == "UserName") {
                        widget = "openesdh/common/widgets/renderers/UserNameField";
                    }
                    else {
                        widget = "openesdh/common/widgets/renderers/PropertyField";
                    }

                    var propertyWidget = {
                        name: widget,
                        config: {
                            currentItem: payload,
                            propertyToRender: i,
                            label: payload[i].label,
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
