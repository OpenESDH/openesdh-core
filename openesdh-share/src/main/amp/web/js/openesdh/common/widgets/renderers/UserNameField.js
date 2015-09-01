/**
 * @module openesdh/common/widgets/renderers/UserName
 * @extends module:alfresco/renderers/Property
 * @mixes module:alfresco/core/TemporalUtils
 * @mixes module:alfresco/core/UrlUtils
 * @author Torben Lauritzen
 */
define(["dojo/_base/declare",
        "alfresco/renderers/Property",
        "alfresco/core/TemporalUtils",
        "alfresco/core/UrlUtils",
        "dojo/_base/lang",
        "dojo/text!./templates/Property.html"],
    function (declare, Property, TemporalUtils, UrlUtils, lang, template) {

        return declare([Property, UrlUtils], {
        	/**
             * An array of the CSS files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{cssFile:"./css/Property"}]
             */
            cssRequirements: [{cssFile:"./css/Property.css"}],

            /**
             * The HTML template to use for the widget.
             * @instance
             * @type {string}
             */
            templateString: template,

            /**
             * The i18n scope to use for this widget.
             *
             * @instance
             */
            i18nScope: "openesdh.renderers.UserNameField",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/UserNameField.properties"}]
             */
            i18nRequirements: [
                {i18nFile: "./i18n/UserNameField.properties"}
            ],
            
            valueBlockClass: false,

            /**
             * Set up the attributes to be used when rendering the template.
             *
             * @instance
             */
            postMixInProperties: function alfresco_renderers_UserNameField__postMixInProperties() {
                var property = lang.getObject(this.propertyToRender, false, this.currentItem);
                this.renderedValue = "";
                                
                if(!lang.isArray(property)){
                    var userName = property.value;
                    var displayName = property.fullname;
                    this.renderedValue = this.userProfileLink(userName, displayName);
                }else{
                    var notFirst = false;
                    for(var i in property){
                        var user = property[i];
                        if(notFirst){
                            this.renderedValue += " ";
                        }
                        this.renderedValue += this.userProfileLink(user.value, user.fullname);
                        notFirst = true;
                    }
                }

                this.renderedValueClass = this.renderedValueClass + " " + this.renderSize;
                if(this.valueBlockClass){
                    this.renderedValueClass += " block"; 
                }
            }
        });
    });