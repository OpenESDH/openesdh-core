/**
 * @module openesdh/common/widgets/renderers/PropertyField
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
            i18nScope: "openesdh.renderers.PropertyField",

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

            /**
             * Determine whether we localise what we're about to display
             */
            localise: false,

            /**
             * Used if the above is true
             */
            i18Value:"",

            /**
             * Set up the attributes to be used when rendering the template.
             *
             * @instance
             */
            postMixInProperties: function alfresco_renderers_UserNameField__postMixInProperties() {

                var property = lang.getObject(this.propertyToRender, false, this.currentItem);
                //Attempt to render the actual property itself (in the case where the propertyToRender isn't an object
                //but a simple string (This was hacked for DocInfoWidget for example)
                this.renderedValue = (property.value == null) ? property: property.value;
                if(this.localise)
                    this.renderedValue = this.i18Value;
                this.renderedValueClass = this.renderedValueClass + " " + this.renderSize + " block";

            }
        });
    });