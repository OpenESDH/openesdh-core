define(["dojo/_base/declare",
        "dojo/dom-class",
        "alfresco/forms/controls/MultiSelectInput",
        "openesdh/common/widgets/controls/form/BaseFormControl",
        "dojo/text!./templates/MultiSelect.html"
        ],
    function(declare, domClass, MultiSelectInput, BaseFormControl, template) {

        return declare([MultiSelectInput, BaseFormControl], {
            
            constructor: function(){
                this.inherited(arguments);
            },
            
            getWidgetConfig: function alfresco_forms_controls_MultiSelectInput__getWidgetConfig() {

                // Setup the widget config
                var widgetConfig = {
                   id: this.id + "_CONTROL",
                   name: this.name,
                   width: this.width,
                   value: this.value,
                   placeHolder: this.placeHolder,
                   choiceCanWrap: this.optionsConfig.choiceCanWrap,
                   choiceMaxWidth: this.optionsConfig.choiceMaxWidth,
                   labelFormat: this.optionsConfig.labelFormat,
                   templateString: template
                };
                

                // Don't want to pass through undefined values (will override defaults)
                if (!widgetConfig.choiceCanWrap && widgetConfig.choiceCanWrap !== false) {
                   delete widgetConfig.choiceCanWrap;
                }
                if (!widgetConfig.choiceMaxWidth) {
                   delete widgetConfig.choiceMaxWidth;
                }

                // Pass back the config
                return widgetConfig;
            },
            
            _onBlur: function(e){
            	if(this.disabled || this.getValue().length){ return; }
    			this.inherited(arguments);
            	
            	domClass.remove(this.domNode, "active");
    		},
            
            _onFocus: function(/*String*/ by ){
            	if(this.disabled || this.readOnly){ return; }
    			this.inherited(arguments);
    			
            	domClass.add(this.domNode, "active");
            }
            
        });
    });