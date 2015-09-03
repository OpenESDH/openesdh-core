/**
 * extending the search thumbnail to take cases into consideration.
 *
 * @module openesdh/common/widgets/renderers/SearchThumbnail
 * @extends alfresco/renderers/Thumbnail
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare", "alfresco/renderers/Thumbnail", "service/constants/Default",
        "alfresco/core/NodeUtils"],
    function (declare, pThumb, AlfConstants, NodeUtils) {

        return declare([pThumb], {
            /**
             * The name of the folder image to use. Valid options are: "folder-32.png", "folder-48.png", "folder-64.png"
             * and "folder-256.png". The default is "folder-64.png".
             *
             * @instance
             * @type {string}
             * @default "folder-64.png"
             */
            caseItemImage: "case-64.png",

            /**
             * Set up the attributes to be used when rendering the template.
             *
             * @instance
             */
            postMixInProperties: function alfresco_renderers_Thumbnail__postMixInProperties() {
                this.imgId = "";
                this.thumbnailUrl = "";
                this.imgAltText = "";
                this.imgTitle = "";

                if (this.currentItem && this.thumbnailUrlTemplate) {
                    // If we have an explicitly decared thumbnail URL template then use that initially, this
                    // is used by the avatar thumbnail for example...
                    this.thumbnailUrl = AlfConstants.PROXY_URI + this.processCurrentItemTokens(this.thumbnailUrlTemplate);
                }
                else if (this.currentItem && this.currentItem.jsNode) {
                    var jsNode = this.currentItem.jsNode;
                    this.thumbnailUrl = this.generateThumbnailUrl();

                    var imageTitle = lang.getObject(this.imageTitleProperty, false, this.currentItem);
                    if (!imageTitle) {
                        this.currentItem.displayName = jsNode.properties["cm:name"];
                    }
                }
                else if (this.currentItem && this.currentItem.nodeRef) {
                    this.imageIdProperty = "nodeRef";

                    // Fallback to just having a nodeRef available... this has been added to handle rendering of
                    // thumbnails in search results where full node information may not be available...
                    var nodeRef = NodeUtils.processNodeRef(this.currentItem.nodeRef);
                    if (this.currentItem.type === "folder") {
                        this.thumbnailUrl = require.toUrl("alfresco/renderers") + "/css/images/" + this.folderImage;
                    }
                    if (this.currentItem.type === "case") {
                        this.thumbnailUrl = require.toUrl("openesdh/images")  + this.folderImage;
                    }
                    else if (this.currentItem.type === "document" && nodeRef.uri) {
                        this.thumbnailUrl = this.generateRenditionSpecificThumbnailUrl(nodeRef.uri);
                        if (!this.thumbnailUrl) {
                            this.thumbnailUrl = AlfConstants.PROXY_URI + "api/node/" + nodeRef.uri + "/content/thumbnails/" + this.renditionName + "/?c=queue&ph=true&lastModified=" + this.currentItem.modifiedOn;
                        }
                    }
                    else if (nodeRef && this.assumeRendition) {
                        this.thumbnailUrl = AlfConstants.PROXY_URI + "api/node/" + nodeRef.uri + "/content/thumbnails/" + this.renditionName + "/?c=queue&ph=true";
                    }
                    else {
                        this.thumbnailUrl = this.generateFallbackThumbnailUrl();
                    }
                }
                else {
                    this.thumbnailUrl = this.generateFallbackThumbnailUrl();
                }

                // Ensure that image title attributes, etc are set
                this.setImageTitle();
            }

        });
    });