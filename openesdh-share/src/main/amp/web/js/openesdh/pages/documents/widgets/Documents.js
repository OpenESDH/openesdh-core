define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreWidgetProcessing",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/Documents.html",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin",

        "dojo/json",

        "dojo/store/Memory",
        "dojo/store/JsonRest",
        "dojo/store/util/QueryResults",

        "dgrid/extensions/DijitRegistry",
        "dgrid/Grid",
        "dgrid/Keyboard",
        "dgrid/Selection"


    ],
    function (declare, _Widget, Core, CoreWidgetProcessing, _Templated, template, lang, _TopicsMixin, json,
              Memory, JsonRest, QueryResults, DijitRegistry, Grid, Keyboard, Selection) {
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


            _onPayloadReceive: function (documents) {
                console.log(documents);


                var store = new JsonRest({
                    target: Alfresco.constants.PROXY_URI + "api/openesdh/search",
                    sortParam: "sortBy",
                    idProperty: "nodeRef"
                });

                require(["dgrid/List"], function(List){
                    // attach to a DOM element indicated by its ID
                    var list = new List({}, "list");
                    // render some data
                    list.renderArray([
                        { first: "Bob", last: "Barker", age: 89 },
                        { first: "Vanna", last: "White", age: 55 },
                        { first: "Pat", last: "Sajak", age: 65 }
                    ]);
                });
            },

            _onPayloadReceive1: function (documents) {
                console.log(documents);
                this.widgetsForBody = [];
                for (var i in documents) {
                    var propertyWidget = {
                        name: "openesdh/common/widgets/renderers/DocumentField",
                        config: {
                            currentItem: documents,
                            propertyToRender: i,
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
