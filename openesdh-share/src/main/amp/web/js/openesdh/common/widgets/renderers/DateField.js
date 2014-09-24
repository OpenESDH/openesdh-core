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
                if(this.propertyToRender == "cm:created") {
                    var creatorProperty = lang.getObject("cm:creator", false, this.currentItem);
                    var createdBy = creatorProperty.value;
                    var creatorName = creatorProperty.fullname;

                    var userName = property.value;
                    var displayName = property.fullname;

                    var dateI18N = "details.created-by";
                    this.renderedValue = this.message(dateI18N, {
                        0: TemporalUtils.getRelativeTime(date),
                        1: this.userProfileLink(createdBy, creatorName)
                    });
               }
                else if(this.propertyToRender == "cm:modified") {
                    var modifierProperty = lang.getObject("cm:modifier", false, this.currentItem);
                    var modifiedBy = modifierProperty.value;
                    var modifierName = modifierProperty.fullname;

                    var dateI18N = "details.modified-by";
                    this.renderedValue = this.message(dateI18N, {
                        0: TemporalUtils.getRelativeTime(date),
                        1: this.userProfileLink(modifiedBy, modifierName)
                    });
                }
                else {
                    this.renderedValue = date;
                }

                this.renderedValueClass = this.renderedValueClass + " " + this.renderSize + " block";
            }
        });
    });