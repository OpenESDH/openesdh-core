/**
 * Extend alfresco/search/SearchThumbnail#generateFallbackThumbnailUrl
 * (mixed in from alfresco/search/SearchThumbnailMixin) to include case type item icon
 * 
 * @module openesdh/search/SearchThumbnail
 * @extends alfresco/search/SearchThumbnail
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "alfresco/search/SearchThumbnail"],
        function(declare, SearchThumbnail) {

   return declare([SearchThumbnail], {

       /**
        * Overrides the standard fallback to address specific site item types.
        *
        * @instance
        * @returns {string} The URL for the thumbnail.
        */
       generateFallbackThumbnailUrl: function alfresco_renderers_SearchThumbnailMixin__generateFallbackThumbnailUrl() {
           var url;
           console.log("ping");

           switch (this.currentItem.type)
           {
               case "blogpost":
                   url = require.toUrl("alfresco/search") + "/css/images/blog-post.png";
                   break;

               case "case":
                   url = require.toUrl("openesdh") + "/images/case-64.png";
                   break;

               case "forumpost":
                   url = require.toUrl("alfresco/search") + "/css/images/topic-post.png";
                   break;

               case "calendarevent":
                   url = require.toUrl("alfresco/search") + "/css/images/calendar-event.png";
                   break;

               case "wikipage":
                   url = require.toUrl("alfresco/search") + "/css/images/wiki-page.png";
                   break;

               case "link":
                   url = require.toUrl("alfresco/search") + "/css/images/link.png";
                   break;

               case "datalist":
                   url = require.toUrl("alfresco/search") + "/css/images/datalist.png";
                   break;

               case "datalistitem":
                   url = require.toUrl("alfresco/search") + "/css/images/datalistitem.png";
                   break;

               default:
                   url = require.toUrl("alfresco/search") + "/css/images/generic-result.png";
                   break;
           }
           return url;
       }

   });
});