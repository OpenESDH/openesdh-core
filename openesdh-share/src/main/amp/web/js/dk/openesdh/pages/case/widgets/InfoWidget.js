define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/InfoWidget.html"
    ],
    function(declare, _Widget, Core, _Templated, template) {
        return declare([_Widget, Core, _Templated], {
            templateString: template,
            buildRendering: function dk_openesdh_pages_case_widgets_InfoWidget__buildRendering() {
                this.greeting = "Hello!";
                this.inherited(arguments);
            }
        });
    });
