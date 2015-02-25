define(["dojo/_base/declare",
        "alfresco/dashlets/Dashlet"],
    function(declare, Dashlet) {

        return declare([Dashlet], {
            /**
             * An array of the CSS files to use with this widget.
             *
             * @instance cssRequirements {Array}
             * @type {object[]}
             * @default [{cssFile:"./css/Dashlet.css"}]
             */
            cssRequirements: [{cssFile:"./css/Dashlet.css"}]
        });
    }
);