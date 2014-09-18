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
        "dojo/_base/lang"],
    function(declare, Property, TemporalUtils, UrlUtils, lang) {

        return declare([Property, UrlUtils], {

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/Date.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/Date.properties"}],

            /**
             * Set up the attributes to be used when rendering the template.
             *
             * @instance
             */
            postMixInProperties: function alfresco_renderers_Date__postMixInProperties() {

                var modifiedDate = lang.getObject("cm:modified", false, this.currentItem);

                var modifiedBy = lang.getObject("cm:modifier", false, this.currentItem);

                var dateI18N = "details.modified-by";

                this.renderedValue = this.message(dateI18N, {
                    0: TemporalUtils.getRelativeTime(modifiedDate),
                    1: modifiedBy
                });

                this.renderedValueClass = this.renderedValueClass + " " + this.renderSize + " block";
                console.log(this.renderedValueClass);
            }
        });
    });