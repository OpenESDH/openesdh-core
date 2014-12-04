/**
 * Extend alfresco/renderers/PublishAction to use a different CSS file, which
 * uses a hand cursor when over the action.
 */
define(["dojo/_base/declare",
        "alfresco/renderers/PublishAction"],
    function(declare, PublishAction) {

        return declare([PublishAction], {

            /**
             * An array of the CSS files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{cssFile:"./css/PublishAction.css"}]
             */
            cssRequirements: [{cssFile:"./css/PublishAction.css"}],

            /**
             * Convert nodeRefs from store_type://store_id/id to
             * store_type/store_id/id for use in REST API URLs.
             * @param v
             * @returns {string}
             */
            convertNodeRefToUrl: function (v) {
                return v.replace(":/", "");
            }
        });
    });