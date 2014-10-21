/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */


var OpenESDH = OpenESDH || {};

/**
 * CreateContentMgr template.
 *
 * @namespace OpenESDH
 * @class OpenESDH.CreateContentMgr
 */
(function()
{

    /**
     * Alfresco Slingshot aliases
     */
    var $siteURL = Alfresco.util.siteURL;

    /**
     * CreateContentMgr constructor.
     *
     * @param {String} htmlId The HTML id of the parent element
     * @return {OpenESDH.CreateContentMgr} The new CreateContentMgr instance
     * @constructor
     */
    OpenESDH.CreateContentMgr = function CreateContentMgr_constructor(htmlId)
    {
        OpenESDH.CreateContentMgr.superclass.constructor.call(this, "OpenESDH.CreateContentMgr", htmlId);
        return this;
    };

    YAHOO.extend(OpenESDH.CreateContentMgr, Alfresco.CreateContentMgr,
        {
            /**
             * Displays the corresponding details page for the current node
             *
             * @method _navigateForward
             * @private
             * @param nodeRef {Alfresco.util.NodeRef} Optional: NodeRef of just-created content item
             */
            _navigateForward: function CreateContentMgr__navigateForward(nodeRef)
            {
                /* Have we been given a nodeRef from the Forms Service? */
                if (YAHOO.lang.isObject(nodeRef))
                {
                    window.location.href = $siteURL("hdp/ws/dk-openesdh-pages-case-dashboard?nodeRef=" + nodeRef.toString());
                }
                else if (document.referrer)
                {
                    /* Did we come from the document library? If so, then direct the user back there */
                    if (document.referrer.match(/documentlibrary([?]|$)/) || document.referrer.match(/repository([?]|$)/))
                    {
                        // go back to the referrer page
                        history.go(-1);
                    }
                    else
                    {
                        document.location.href = document.referrer;
                    }
                }
                else if (this.options.siteId && this.options.siteId !== "")
                {
                    // In a Site, so go back to the document library root
                    window.location.href = $siteURL("documentlibrary");
                }
                else
                {
                    window.location.href = Alfresco.constants.URL_CONTEXT;
                }
            }
        });
})();
