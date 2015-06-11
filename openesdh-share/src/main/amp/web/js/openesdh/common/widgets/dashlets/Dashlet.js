define(["dojo/_base/declare",
        "alfresco/dashlets/Dashlet",
        "openesdh/pages/case/widgets/_DocumentGridUploadMixin",
        "dojo/_base/lang"],
    function(declare, Dashlet, _DocumentGridUploadMixin, lang) {

        return declare([Dashlet, _DocumentGridUploadMixin], {
            /**
             * An array of the CSS files to use with this widget.
             *
             * @instance cssRequirements {Array}
             * @type {object[]}
             * @default [{cssFile:"./css/Dashlet.css"}]
             */
            cssRequirements: [{cssFile:"./css/Dashlet.css"}],

            allowDnD: false,
            
            constructor: function (args) {
                lang.mixin(this, args);
                
                if(this.isReadOnly){
                	this.widgetsForTitleBarActions[0].config.visibilityConfig = {initialValue: false};
                	this.widgetsForBody[0].config.isReadOnly = this.isReadOnly;
                }
            },

            postCreate: function(){
                this.inherited(arguments);

                if (!this.allowDnD){
                    this.dndUploadCapable = this.allowDnD;
                }
                
                if(this.allowDnD){
                    this.subscribeToCurrentNodeChanges(this.domNode);
                    this.addUploadDragAndDrop(this.domNode);
                }
            }

        });
    }
);