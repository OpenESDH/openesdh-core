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
        "dojo/_base/lang"],
    function (declare, Property, TemporalUtils, UrlUtils, lang) {

        return declare([Property, UrlUtils], {

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

            /**
             * Set up the attributes to be used when rendering the template.
             *
             * @instance
             */
            postMixInProperties: function alfresco_renderers_UserNameField__postMixInProperties() {
                var property = lang.getObject(this.propertyToRender, false, this.currentItem);
                var userName = property.value;
                var displayName = property.fullname;
                this.renderedValue = this.userProfileLink(userName, displayName);

                this.renderedValueClass = this.renderedValueClass + " " + this.renderSize + " block";
            }
        });
    });