define(["dojo/_base/declare",
        "dijit/_WidgetBase",
    "alfresco/core/Core",
    "alfresco/core/CoreWidgetProcessing",
    "dijit/_TemplatedMixin",
    "dojo/text!./templates/InfoWidget.html",
    "dojo/_base/lang",
    "openesdh/pages/_TopicsMixin",
    "openesdh/pages/case/widgets/lib/JSONProcessing",
    "openesdh/common/widgets/renderers/PropertyField"
],
function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, _TopicsMixin, JSONProcessing, PropertyField) {
    return declare([_Widget, Core, CoreWidgetProcessing, _Templated, _TopicsMixin, JSONProcessing], {
        templateString: template,

        i18nRequirements: [
            {i18nFile: "./i18n/DocInfoWidget.properties"}
        ],

        cssRequirements: [{cssFile:"./css/DocInfoWidget.css"}],

        bodyNode: null,

        widgetsForBody: [],

        buildRendering: function pages_case_widgets_DocInfoWidget__buildRendering() {
            this.alfSubscribe(this.CaseRefreshDocInfoTopic, lang.hitch(this, "_onPayloadReceive"));

            this.bodyTitle = this.message('bodyTitle');
            this.inherited(arguments);
        },

        _onPayloadReceive: function (payload) {
            this.bodyNode.innerHTML="";
            this.widgetsForBody = [];
            var properties = payload;
            for (var i in properties) {
                if (i == "alfTopic") continue;
                var widget = "openesdh/common/widgets/renderers/PropertyField";
                var propertyWidget = {
                    name: widget,
                    config: {
                        currentItem: properties,
                        propertyToRender: i,
                        localise: true,
                        i18Value: this.message("document."+i+".constraint.label."+payload[i]),
                        label: this.message("doc.info.label."+i),
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
