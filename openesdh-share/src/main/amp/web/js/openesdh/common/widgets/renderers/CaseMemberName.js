/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

/**
 * Renders the value of a property found in the configured [currentItem]{@link module:alfresco/renderers/Property#currentItem}
 * attribute. The property that will be rendered is determined by the [propertyToRender]{@link module:alfresco/renderers/Property#propertyToRender}
 * attribute which should be defined in "dot-notation" format (e.g. "node.properties.cm:title"). This widget accepts a number
 * of different configuration options that control how the property is ultimately displayed.
 *
 * @module openesdh/common/widgets/renderers/CaseMemberName
 * @extends alfresco/renderers/Property
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare",
        "alfresco/renderers/Property",
        "alfresco/core/ObjectTypeUtils",
        "alfresco/core/UrlUtils",
        "dojo/_base/lang",
        "dijit/_OnDijitClickMixin",
        "alfresco/navigation/Link","dojo/dom-construct",
        "openesdh/common/services/_CaseMembersServiceTopicsMixin"],
    function (declare, Property, ObjectTypeUtils, UrlUtils, lang, _OnDijitClickMixin, Link, domConstruct, CaseMembersServiceTopics) {

        return declare([Property, UrlUtils, CaseMembersServiceTopics], {

            statusDialog: null,

            /**
             * Set up the attributes to be used when rendering the template.
             *
             * @instance
             */
            postMixInProperties: function openesdh_case_dashlet_name_renderers__postMixInProperties() {
               //Intentionally do nothing
            },

            postCreate: function alfresco_header_CurrentUserStatus__postCreate() {
                this.inherited(arguments);
                if(this.currentItem.isGroup || this.currentItem.authorityType == "GROUP") {
                    var groupShortName = this.currentItem.isGroup ? this._getGroupShortName(this.currentItem.authority) : this.currentItem.shortName; //strip the "GROUP_" from the auth name
                    var groupLink = this.groupMembersLink(groupShortName, this.currentItem.displayName);
                    groupLink.placeAt(this.renderedValueNode);
                }
                else{
                    var userName = this.currentItem.authority ? this.currentItem.authority : this.currentItem.shortName ;
                    var profileLink = this.getUserProfileLink(userName, this.currentItem.displayName);
                    profileLink.placeAt(this.renderedValueNode);
                }
            },

            /**
             * @instance
             * @param {string} property The name of the property to render
             */
            getRenderedProperty: function alfresco_renderers_Property__getRenderedProperty(property) {
                //Intentionally do nothing to prevent double posting of displayed property
            },

            /**
             * create an ink object for the group dialog
             * @param groupName {String}
             * @param displayName {String}
             * @returns {alfresco/navigation/Link}
             */
            groupMembersLink: function openesdh_generate_group_members_link(groupName, displayName) {
                return new Link({
                    label: displayName,
                    title: displayName,
                    publishTopic: this.CaseMembersGroupGet,
                    publishPayload: {
                        groupShortName: groupName
                    }
                });
            },

            /**
             * create a link object for the userprofile link
             * @param userName {String}
             * @param displayName {String}
             * @returns {alfresco/navigation/Link}
             */
            getUserProfileLink: function openesdh_generate_group_members_link(userName, displayName) {
                return new Link({
                    label: displayName,
                    title: displayName,
                    publishTopic: this.CaseNavigateToUserProfilePage,
                    publishPayload: {
                        userName: userName
                    }
                });
            },

            /**
             * trim "GROUP_" from the fullAuthorityName to return the group short name
             * @param fullAuthorityName {String}
             * @returns {string}
             */
            _getGroupShortName: function openesdh_extract_group_shortName(fullAuthorityName){
                return fullAuthorityName.substring(6, fullAuthorityName.length);
            }

        });
    });