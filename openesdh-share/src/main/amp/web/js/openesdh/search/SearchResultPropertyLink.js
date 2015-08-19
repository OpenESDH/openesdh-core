/**
 * Extends the Property renderer ({@link module:alfresco/search/SearchResultPropertyLink}) to consider cases.
 *
 * @module openesdh/search/SearchResultPropertyLink
 * @extends alfresco/search/SearchResultPropertyLink
 * @mixes alfresco/renderers/_SearchResultLinkMixin
 * @author Dave Draper
 */
define(["dojo/_base/declare",
        "dojo/_base/lang",
        "alfresco/search/SearchResultPropertyLink",
        "alfresco/renderers/_SearchResultLinkMixin"],
    function (declare, lang, SearchResultPropertyLink, _SearchResultLinkMixin) {

        return declare([SearchResultPropertyLink, _SearchResultLinkMixin], {

            /**
             * This function generates a payload for the [NavigationService]{@link module:alfresco/services/NavigationService}
             * that varies depending upon the type of search result (e.g. a document or folder, in a site or in
             * the repository, etc) which can also be used to extrapolate an HTML anchor for
             * [_HtmlAnchorMixin]{@link module:alfresco/navigation/_HtmlAnchorMixin}.
             *
             * @instance
             * @return {object} The generated payload
             */
            generateSearchLinkPayload: function alfresco_renderers__SearchResultLinkMixin__generateSearchLinkPayload() {
                // jshint maxcomplexity:false
                var payload = {
                    type: "PAGE_RELATIVE",
                    target: "CURRENT",
                    url: null
                };
                var type = lang.getObject("type", false, this.currentItem),
                    site = lang.getObject("site.shortName", false, this.currentItem),
                    caseObj = lang.getObject("case.caseId", false, this.currentItem);

                switch (type) {
                    case "folder":
                        var path = lang.getObject("path", false, this.currentItem),
                            name = lang.getObject("name", false, this.currentItem);
                        if (site) {
                            payload.url = "site/" + site + "/documentlibrary?path=" + encodeURIComponent(path) + "%2F" + encodeURIComponent(name);
                        }
                        else if (path) {
                            path = "/" + path.split("/").slice(2).join("/");
                            payload.url = "repository?path=" + encodeURIComponent(path) + "%2F" + encodeURIComponent(name);
                        }
                        break;

                    case "case":
                        var caseId = lang.getObject("case.caseId", false, this.currentItem);
                        payload.url = "oe/case/" + caseId + "/dashboard";
                        break;

                    case "wikipage":
                        var title = lang.getObject("name", false, this.currentItem);
                        if (site) {
                            payload.url = "site/" + site + "/wiki-page?title=" + title;
                        }
                        break;

                    case "blogpost":
                        var postid = lang.getObject("name", false, this.currentItem);
                        if (site) {
                            payload.url = "site/" + site + "/blog-postview?postId=" + postid;
                        }
                        break;

                    case "forumpost":
                        var topicid = lang.getObject("name", false, this.currentItem);
                        if (site) {
                            payload.url = "site/" + site + "/discussions-topicview?topicId=" + topicid;
                        }
                        break;

                    case "link":
                        var linkid = lang.getObject("name", false, this.currentItem);
                        if (site) {
                            payload.url = "site/" + site + "/links-view?linkId=" + linkid;
                        }
                        break;

                    case "datalist":
                        var listid = lang.getObject("name", false, this.currentItem);
                        if (site) {
                            payload.url = "site/" + site + "/data-lists?list=" + listid;
                        }
                        break;

                    case "calendarevent":
                        var dateProperty = lang.getObject("fromDate", false, this.currentItem),
                            eventDate = dateProperty && this.fromISO8601(dateProperty),
                            formattedDate = eventDate && this.formatDate(eventDate, "yyyy-mm-dd");
                        if (site) {
                            payload.url = "site/" + site + "/calendar?date=" + formattedDate;
                        }
                        break;

                    default:
                        var nodeRef = lang.getObject("nodeRef", false, this.currentItem);
                        if (site) {
                            payload.url = "site/" + site + "/document-details?nodeRef=" + nodeRef;
                        }
                        if(caseObj){
                            var caseId = lang.getObject("case.caseId", false, this.currentItem);
                            payload.url = "oe/case/"+caseId+"/documents";
                        }
                        else {
                            payload.url = "document-details?nodeRef=" + nodeRef;
                        }
                }

                return payload;
            }
        });
    });