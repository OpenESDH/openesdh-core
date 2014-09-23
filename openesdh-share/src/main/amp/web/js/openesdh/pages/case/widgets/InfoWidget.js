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
                var currentItem = this.unmarshal(payload);
                console.log(currentItem);
                for (var i in currentItem) {
                    console.log(i + " " + currentItem[i]);
                    var widget = "";
                    if(currentItem[i] instanceof Date) {
                        widget = "openesdh/common/widgets/renderers/DateField";
                    }
                    else {
                        widget = "alfresco/renderers/Property";
                    }

                    var propertyWidget = {
                        name: widget,
                        config: {
                            currentItem: currentItem,
                            propertyToRender: i,
                            label: this.message(i),
                            renderOnNewLine: true
                        }
                    };

                    this.widgetsForBody.push(propertyWidget);
//                console.log(i + " " + payload[i].value + " " + eval("new " + payload[i].type + "(" + payload[i].value + ")"));
                }

                this.processWidgets(this.widgetsForBody, this.bodyNode);
            }
        });
    })
;
