define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/InfoWidget.html",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, _TopicsMixin) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _TopicsMixin], {
            templateString: template,
            i18nRequirements: [
                {i18nFile: "./messages/InfoWidget.properties"}
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
                for (var i in payload) {
                    console.log(i + " " + payload[i]);
                    var propertyWidget = {
                        name: "alfresco/renderers/Property",
                        config: {
                            currentItem: payload,
                            propertyToRender: i,
                            label: i,
                            renderOnNewLine: true
                        }
                    };

                    this.widgetsForBody.push(propertyWidget);
//                console.log(i + " " + payload[i].value + " " + eval("new " + payload[i].type + "(" + payload[i].value + ")"));
                }

                this.processWidgets(this.widgetsForBody, this.bodyNode);
            },



            _applyTypes: function (payload) {
                var result = {};
                for (var i in payload) {
                    if (i == "alfTopic") continue;
                    //    console.log(payload[i].type + " " + payload[i].value)
                    result[i] = new window[payload[i].type](payload[i].value);
                   // console.log(result[i]);
                }
                return result;
            },

            _onPayloadReceive1: function (payload) {
                console.log(payload);

                this.widgetsForBody = [];
                var currentItem = this._applyTypes(payload);
                console.log(currentItem);
                for (var i in currentItem) {
                    console.log(i + " " + currentItem[i]);
                    var widget = "";
                    switch (payload[i].type) {
                        case "Date":
                            widget = "openesdh/common/widgets/renderers/Date";
                            widget = "alfresco/renderers/Property";
                            break;
                        case "String":
                            widget = "alfresco/renderers/Property";
                            break;
                        default:
                            widget = "alfresco/renderers/Property";
                    }
                    var propertyWidget = {
                        name: "alfresco/renderers/Property",
                        config: {
                            currentItem: currentItem,
                            propertyToRender: i,
                            label: i,
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
