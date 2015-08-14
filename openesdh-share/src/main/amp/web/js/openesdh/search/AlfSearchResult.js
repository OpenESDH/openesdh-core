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
        "dojo/dom-class",
        "dojo/text!./templates/AlfSearchResult.html",
        "alfresco/renderers/Property",
        "alfresco/renderers/PropertyLink",
        "alfresco/search/SearchThumbnail",
        "alfresco/search/AlfSearchResult",
        "alfresco/renderers/Date",
        //"openesdh/common/widgets/renderers/Date",
        "openesdh/search/SearchResultPropertyLink"],
    function (declare, lang, domClass, template, Property, PropertyLink, SearchThumbnail, AlfSearchResult, Date, SearchResultPropertyLink) {
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
             * The HTML template to use for the widget.
             *
             * @instance
             * @type {String}
             */
            templateString: template,

            /**
             * Creates the renderers to display for a search result and adds them into the template. Renderers
             * will only be created if there is data for them. This is done to further improve the performance
             * of the search rendering.
             *
             * @instance postCreate
             */
            postCreate: function alfresco_search_AlfSearchResult__postCreate() {
                this.inherited(arguments);
                this.caseStartAndEndDateRenderers();
            },

            /**
             * This function is called to create a
             * [SearchResultPropertyLink]{@link module:alfresco/renderers/SearchResultPropertyLink} widget
             * to render the the displayName of the result. It can be overridden to replace the default widget
             * with a reconfigured version.
             *
             * @instance
             */
            createDisplayNameRenderer: function alfresco_search_AlfSearchResult__createDisplayNameRenderer() {
                // jshint nonew:false
                new SearchResultPropertyLink({
                    currentItem: this.currentItem,
                    pubSubScope: this.pubSubScope,
                    propertyToRender: "displayName",
                    renderSize: "large"
                }, this.nameNode);
            },

            /**
             * This function is called to create a [DateLink widget]{@link module:alfresco/renderers/DateLink}
             * to render the the date that the result was created or last modified. It can be overridden to
             * replace the default widget with a reconfigured version.
             *
             * @instance
             */
            caseStartAndEndDateRenderers: function alfresco_search_AlfSearchResult__caseStartAndEndDateRenderers() {
                var caseObj = this.currentItem.type == "case";
                if (!caseObj)
                    return;
                try {
                    // jshint nonew:false
                    new Date({
                        currentItem: this.currentItem,
                        renderedValueClass: "alfresco-renderers-Property pointer",
                        renderSize: "small",
                        simple: true,
                        propertyToRender: "case.startDate",
                        deemphasized: true,
                        pubSubScope: this.pubSubScope
                    }, this.startDateNode);

                    new Date({
                        currentItem: this.currentItem,
                        renderedValueClass: "alfresco-renderers-Property pointer",
                        renderSize: "small",
                        simple: true,
                        propertyToRender: "case.endDate",
                        deemphasized: true,
                        pubSubScope: this.pubSubScope
                    }, this.endDateNode);
                }
                catch (error){
                    console.log("Error with start/end Date: "+error.message)
                }
            },

            /**
             * This function is called to create a [PropertyLink widget]{@link module:alfresco/renderers/PropertyLink}
             * to render the name of the site in which the result can be found (if applicable). It can be overridden to
             * replace the default widget with a reconfigured version.
             *
             * @instance
             */
            createSiteRenderer: function alfresco_search_AlfSearchResult__createSiteRenderer() {
                if(this.currentItem.type == "case")
                    return;
                // Check to see if the result exists within a site and create a renderer to display the site
                // title if appropriate...
                var site = lang.getObject("site.title", false, this.currentItem);
                var caseObj = lang.getObject("case.caseId", false, this.currentItem);
                if (!site && !caseObj) {
                    domClass.add(this.siteRow, "hidden");
                }
                else if (caseObj) {
                    // jshint nonew:false
                    new PropertyLink({
                        renderedValueClass: "alfresco-renderers-Property pointer",
                        renderSize: "small",
                        pubSubScope: this.pubSubScope,
                        currentItem: this.currentItem,
                        propertyToRender: "case.caseId",
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
                else if (site) {
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
                if (!this.currentItem.path || this.currentItem.type == "case") {
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
                        label: caseObj ? this.message("faceted-search.doc-lib.value-prefix.case.path") : this.message("faceted-search.doc-lib.value-prefix.path"),
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
            },

            /**
             * This function is called to create a [Property widget]{@link module:alfresco/renderers/Property}
             * to render the title of the result (if it has one). It can be overridden to replace the default
             * widget with a reconfigured version.
             *
             * @instance
             */
            createTitleRenderer: function alfresco_search_AlfSearchResult__createTitleRenderer() {
                var isCase = this.currentItem.type == "case";
                // jshint nonew:false
                if (!this.currentItem.title)
                {
                    domClass.add(this.titleNode, "hidden");
                }
                else
                {
                    new Property({
                        currentItem: this.currentItem,
                        pubSubScope: this.pubSubScope,
                        propertyToRender: isCase? "name" : "title",
                        renderSize: "small",
                        renderedValuePrefix: "(",
                        renderedValueSuffix: ")"
                    }, this.titleNode);
                }
            }
        });
    });