define(["dojo/_base/declare",
        "openesdh/pages/case/widgets/InfoWidget"
    ],
    function (declare, InfoWidget) {
        return declare([InfoWidget], {

            /*i18nRequirements: [
                {i18nFile: "./i18n/InfoWidget.properties"}
            ],*/

            buildRendering: function dk_openesdh_pages_case_widgets_InfoWidget__buildRendering() {
                this.inherited(arguments);

                this.bodyTitle.remove();
            },

            _onPayloadReceive: function (payload) {
                var docProperties = {"sys:node-dbid":"Document Id", "oe:id":"Case-Id", "oe:status":"Status"};
                this.widgetsForBody = [];
                var properties = payload.properties;
                    for(var key in docProperties) {
                        for (var i in properties) {
                            //console.log("-->Key ("+ key +"): "+ docProperties[key]);
                            if (!i.match(key)) continue;
                            var widget = "openesdh/common/widgets/renderers/PropertyField";

                            var propertyWidget = {
                                name: widget,
                                config: {
                                    currentItem: properties,
                                    propertyToRender: i,
                                    label: docProperties[key],
                                    renderOnNewLine: true
                                }
                            };

                            this.widgetsForBody.push(propertyWidget);
                    }
                }
                this.processWidgets(this.widgetsForBody, this.bodyNode);
            }


        });
    })
;
