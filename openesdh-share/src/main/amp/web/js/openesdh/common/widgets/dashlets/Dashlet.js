define(["dojo/_base/declare",
        "alfresco/dashlets/Dashlet",
        "openesdh/pages/case/widgets/_DocumentGridUploadMixin"],
    function(declare, Dashlet, _DocumentGridUploadMixin) {

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