define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/Documents.html",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin"
    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, _TopicsMixin) {
        return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _TopicsMixin], {
            templateString: template,
            i18nRequirements: [
                {i18nFile: "./i18n/Documents.properties"}
            ],

            bodyNode: null,

            widgetsForBody: [],

            buildRendering: function dk_openesdh_pages_case_widgets_InfoWidget__buildRendering() {
                this.alfSubscribe(this.DocumentsTopic, lang.hitch(this, "_onPayloadReceive"));

                this.bodyTitle = this.message('bodyTitle');
                this.inherited(arguments);
            },

            _onPayloadReceive: function (payload) {
                console.log(payload);

                this.widgetsForBody = [];
                for (var i in payload.documents) {
                    if (i == "alfTopic") continue;

                    var propertyWidget = {
                        name: "openesdh/common/widgets/renderers/DocumentField",
                        config: {
                            currentItem: payload,
                            propertyToRender: i,
                            label: "En label",
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
