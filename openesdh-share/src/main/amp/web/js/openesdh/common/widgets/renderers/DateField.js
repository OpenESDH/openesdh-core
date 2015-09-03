/**
 * @module openesdh/common/widgets/renderers/Date
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
    function(declare, Property, TemporalUtils, UrlUtils, lang, template) {

        return declare([Property, UrlUtils, TemporalUtils], {
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
            i18nScope: "openesdh.renderers.DateField",

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/DateField.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/DateField.properties"}],

            /**
             * Set up the attributes to be used when rendering the template.
             *
             * @instance
             */
            postMixInProperties: function alfresco_renderers_Date__postMixInProperties() {
                var property = lang.getObject(this.propertyToRender, false, this.currentItem);
                var date = new Date(property.value);
                this.renderedValue = this.getRelativeTime(date);
                this.renderedValueClass = this.renderedValueClass + " " + this.renderSize + " block";
            }
        });
    });