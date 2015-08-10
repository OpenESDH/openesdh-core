/**
 * This is used as the standard search result template used to control the layout of search results.
 * It is more efficient to use a single widget to control the layout than to build a complex model
 * control the layout than to build a complex model out of smaller widgets,
 * out of smaller widgets, however this widget can still be easily replaced by other widgets to
 * provide a completely custom rendering.
 *
 * @module alfresco/search/AlfSearchResult
 * @extends alfresco/lists/views/layouts/Row
 * @author Dave Draper
 * @author David Webster
 */
define(["dojo/_base/declare",
        "dojo/_base/lang",
        "alfresco/renderers/PropertyLink",
        "alfresco/search/SearchThumbnail",
        "alfresco/search/AlfSearchResult"],
    function (declare, lang, PropertyLink, SearchThumbnail, AlfSearchResult) {
        return declare([AlfSearchResult], {

            /**
             * An array of the i18n files to use with this widget.
             *
             * @instance
             * @type {object[]}
             * @default [{i18nFile: "./i18n/AlfSearchResult.properties"}]
             */
            i18nRequirements: [{i18nFile: "./i18n/AlfSearchResult.properties"}],

            /**
             * This function is called to create a [PropertyLink widget]{@link module:alfresco/renderers/PropertyLink}
             * to render the name of the site in which the result can be found (if applicable). It can be overridden to
             * replace the default widget with a reconfigured version.
             *
             * @instance
             */
            createSiteRenderer: function alfresco_search_AlfSearchResult__createSiteRenderer() {
                // Check to see if the result exists within a site and create a renderer to display the site
                // title if appropriate...
                var site = lang.getObject("site.title", false, this.currentItem);
                var caseObj = lang.getObject("case.caseId", false, this.currentItem);
                if (!site && !caseObj)
                {
                    domClass.add(this.siteRow, "hidden");
                }
                else if(caseObj)
                {
                    // jshint nonew:false
                    new PropertyLink({
                        renderedValueClass: "alfresco-renderers-Property pointer",
                        renderSize: "small",
                        pubSubScope: this.pubSubScope,
                        currentItem: this.currentItem,
                        propertyToRender: "case.title",
                        label: this.message("faceted-search.doc-lib.value-prefix.case"),
                        publishTopic: "ALF_NAVIGATE_TO_PAGE",
                        useCurrentItemAsPayload: false,
                        publishPayloadType: "PROCESS",
                        publishPayloadModifiers: ["processCurrentItemTokens"],
                        publishPayload: {
                            url: "oe/case/{case.caseId}/dashboard",
                            type: "PAGE_RELATIVE"
                        }
                    }, this.siteNode);
                }
                else if(site)
                {
                    // jshint nonew:false
                    new PropertyLink({
                        renderedValueClass: "alfresco-renderers-Property pointer",
                        renderSize: "small",
                        pubSubScope: this.pubSubScope,
                        currentItem: this.currentItem,
                        propertyToRender: "site.title",
                        label: this.message("faceted-search.doc-lib.value-prefix.site"),
                        publishTopic: "ALF_NAVIGATE_TO_PAGE",
                        useCurrentItemAsPayload: false,
                        publishPayloadType: "PROCESS",
                        publishPayloadModifiers: ["processCurrentItemTokens"],
                        publishPayload: {
                            url: "site/{site.shortName}/dashboard",
                            type: "PAGE_RELATIVE"
                        }
                    }, this.siteNode);
                }
            },

            /**
             * This function is called to create a [PropertyLink widget]{@link module:alfresco/renderers/PropertyLink}
             * to render the the path to the result. It can be overridden to replace the default widget with a reconfigured
             * version.
             *
             * @instance
             */
            createPathRenderer: function alfresco_search_AlfSearchResult__createPathRenderer() {
                if (!this.currentItem.path) {
                    domClass.add(this.pathRow, "hidden");
                }
                else {
                    var site = lang.getObject("site.title", false, this.currentItem);
                    var caseObj = lang.getObject("case.caseId", false, this.currentItem);
                    // Create processed path as pathLink on this.currentItem
                    var repo = (site || caseObj) ? false : true;

                    this.currentItem.pathLink = repo ?
                        encodeURIComponent("/" + this.currentItem.path.split("/").slice(2).join("/")) :
                        encodeURIComponent("/" + this.currentItem.path);
                    var url = "repository?path={pathLink}";
                    if (caseObj)
                        url = "{path}";
                    else if (site)
                        url = "site/{site.shortName}/documentlibrary?path={pathLink}";

                    // jshint nonew:false
                    new PropertyLink({
                        renderedValueClass: "alfresco-renderers-Property pointer",
                        pubSubScope: this.pubSubScope,
                        currentItem: this.currentItem,
                        propertyToRender: "path",
                        renderSize: "small",
                        label: caseObj ? this.message("faceted-search.doc-lib.value-prefix.case.path"): this.message("faceted-search.doc-lib.value-prefix.path"),
                        publishTopic: "ALF_NAVIGATE_TO_PAGE",
                        useCurrentItemAsPayload: false,
                        publishPayloadType: "PROCESS",
                        publishPayloadModifiers: ["processCurrentItemTokens"],
                        publishPayload: {
                            url: url,
                            type: "PAGE_RELATIVE"
                        }
                    }, this.pathNode);
                }
            }
        });
    });