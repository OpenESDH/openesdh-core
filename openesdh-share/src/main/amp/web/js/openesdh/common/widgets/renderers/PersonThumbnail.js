/**
 * Renders a standard large thumbnail for a node.
 *
 * @module openesdh/common/widgets/renderers/PersonThumbnail
 * @extends alfresco/renderers/Thumbnail
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare", "alfresco/renderers/Thumbnail", "service/constants/Default",
        "alfresco/core/NodeUtils"],
    function (declare, pThumb, AlfConstants, NodeUtils) {

        return declare([pThumb], {

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

                if (this.currentItem != null && this.currentItem.jsNode) {
                    var jsNode = this.currentItem.jsNode;
                    this.thumbnailUrl = this.generateThumbnailUrl();
                    if (this.currentItem.displayName == null) {
                        this.currentItem.displayName = jsNode.properties["cm:name"];
                    }
                    this.setImageTitle();
                }
                else if (this.currentItem != null && this.currentItem.nodeRef != null) {
                    this.imageIdProperty = "nodeRef";
                    this.setImageTitle();

                    // Fallback to just having a nodeRef available... this has been added to handle rendering of
                    // thumbnails in search results where full node information may not be available...
                    var nodeRef = NodeUtils.processNodeRef(this.currentItem.nodeRef);
                    if (this.currentItem.isGroup) {
                        this.thumbnailUrl = AlfConstants.URL_RESCONTEXT + this.currentItem.avatar;
                    }
                    else {
                        if (this.avatar != null) {
                            this.thumbnailUrl = AlfConstants.PROXY_URI + this.currentItem.avatar + "/?c=queue&ph=true";
                        }
                        else
                            this.thumbnailUrl = this.generateFallbackThumbnailUrl();
                    }
                }
            },


            /**
             * If a thumbnail URL cannot be determined then fallback to a standard image.
             *
             * @instance
             * @returns {string} The URL for the thumbnail.
             */
            generateFallbackThumbnailUrl: function alfresco_renderers_Thumbnail__generateFallbackThumbnailUrl() {
                return AlfConstants.URL_RESCONTEXT + "components/images/user-16.png";
            }

        });
    });