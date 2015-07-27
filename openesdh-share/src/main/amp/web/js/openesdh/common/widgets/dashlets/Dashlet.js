define(["dojo/_base/declare",
        "dojo/text!./templates/Dashlet.html",
        "alfresco/dashlets/Dashlet",
        "openesdh/pages/case/widgets/_DocumentGridUploadMixin",
        "dojo/_base/lang"],
    function(declare, template, Dashlet, _DocumentGridUploadMixin, lang) {

        return declare([Dashlet, _DocumentGridUploadMixin], {
            /**
             * An array of the CSS files to use with this widget.
             *
             * @instance cssRequirements {Array}
             * @type {object[]}
             * @default [{cssFile:"./css/Dashlet.css"}]
             */
            cssRequirements: [{cssFile:"./css/Dashlet.css"}],
            
            templateString: template,

            allowDnD: false,
            
            /**
             * The container that holds the footerbar widgets.
             * Will be populated by dojo.
             *
             * @instance
             * @type {HTMLElement}
             */
            footerBarActionsNode: null,
            
            /**
             * Widgets to place as footer bar actions.
             *
             * @instance
             * @type {object[]}
             */
            widgetsForFooterBarActions: null,
            
            constructor: function (args) {
                lang.mixin(this, args);
                
                if(this.isReadOnly){
                	this.widgetsForTitleBarActions[0].config.visibilityConfig = {initialValue: false};
                	this.widgetsForBody[0].config.isReadOnly = this.isReadOnly;
                }
            },

            postCreate: function(){
                this.inherited(arguments);
                
                this.processContainer(this.widgetsForFooterBarActions, this.footerBarActionsNode);

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