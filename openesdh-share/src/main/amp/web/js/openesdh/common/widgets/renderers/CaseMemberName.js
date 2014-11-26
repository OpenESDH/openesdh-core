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
        "alfresco/dialogs/AlfDialog",
        "alfresco/buttons/AlfButton",
        "dojo/on",
        "dojo/_base/lang"],
    function (declare, Property, ObjectTypeUtils, UrlUtils, AlfDialog, AlfButton, on, lang) {

        return declare([Property, UrlUtils], {


            statusDialog: null,

            /**
             * Set up the attributes to be used when rendering the template.
             *
             * @instance
             */
            postMixInProperties: function openesdh_case_dashlet_name_renderers__postMixInProperties() {
                if (this.label != null) {
                    this.label = this.message(this.label) + ": ";
                }
                else {
                    this.label = "";
                }

                if (ObjectTypeUtils.isString(this.propertyToRender) &&
                    ObjectTypeUtils.isObject(this.currentItem) &&
                    lang.exists(this.propertyToRender, this.currentItem)) {
                    this.renderPropertyNotFound = false;
                    this.originalRenderedValue = this.getRenderedProperty(this.currentItem);
                    this.renderedValue = this.mapValueToDisplayValue(this.originalRenderedValue);
                }
                else {
                    this.alfLog("log", "Property does not exist:", this);
                }

                this.renderedValueClass = this.renderedValueClass + " " + this.renderSize;

                if (this.renderOnNewLine == true) {
                    this.renderedValueClass = this.renderedValueClass + " block";
                }

                // If the renderedValue is not set then display a warning message if requested...
                if ((this.renderedValue == null || this.renderedValue == "") && this.warnIfNotAvailable) {
                    // Get appropriate message
                    // Check message based on propertyToRender otherwise default to sensible alternative
                    var warningKey = this.warnIfNoteAvailableMessage,
                        warningMessage = "";
                    if (warningKey == null) {
                        warningKey = "no." + this.propertyToRender + ".message";
                        warningMessage = this.message(warningKey);
                        if (warningMessage == warningKey) {
                            warningMessage = this.message("no.property.message", {0: this.propertyToRender});
                        }
                    }
                    else {
                        warningMessage = this.message(warningKey);
                    }
                    this.renderedValue = warningMessage;
                    this.renderedValueClass += " faded";
                }
                else if ((this.renderedValue == null || this.renderedValue == "") && !this.warnIfNotAvailable) {
                    // Reset the prefix and suffix if there's no data to display
                    this.requestedValuePrefix = this.renderedValuePrefix;
                    this.requestedValueSuffix = this.renderedValueSuffix;
                    this.renderedValuePrefix = "";
                    this.renderedValueSuffix = "";
                }
            },

            postCreate: function alfresco_header_CurrentUserStatus__postCreate() {
                this.inherited(arguments);
                //this.alfSubscribe("OPENESDH_SHOW_CASE_GROUP_MEMBERS", lang.hitch(this, "showGroupMembersDialog"));
                if(this.currentItem.isGroup)
                    on(this.renderedValueNode, "click", lang.hitch(this, "showGroupMembersDialog"));
            },


            /**
             * @instance
             * @param {string} property The name of the property to render
             */
            getRenderedProperty: function alfresco_renderers_Property__getRenderedProperty(property) {
                var value = "";
                if (property == null) {
                    // No action required if a property isn't supplied
                    console.log("=====> Property is NULL <=====");
                }

                else if (ObjectTypeUtils.isObject(property)) {
                    if ( !property.isGroup)
                        value = Alfresco.util.userProfileLink(property.authority, property.displayName);
                    else
                        value = this.groupMembersLink(property.displayName);

                }
                /*
                else if (property.hasOwnProperty("displayName")) {
                    value = this.encodeHTML(property.displayName || "");
                }
                else if (property.hasOwnProperty("title")) {
                    value = this.encodeHTML(property.title || "");
                }
                else if (property.hasOwnProperty("name")) {
                        value = this.encodeHTML(property.name || "");
                }
                */

                return value;
            },

            showGroupMembersDialog : function  openesdh_generate_group_members_link(payload) {
                if (this.statusDialog == null) {

                    var gShortName = this.currentItem.authority.substring(6, this.currentItem.authority.length);

                    //TODO perhaps hitch this to a topic that provides a function to teardown the dialog and remove it on close
                    //this.alfSubscribe(this.postNewUserStatusTopic, lang.hitch(this, "postStatus"));
                    this.statusDialog = new AlfDialog({
                        title: this.message("Group Members"),
                        widgetsContent: [ {
                                name: "openesdh/pages/case/widgets/CaseGroupMembers",
                                config: {
                                    groupShortName: gShortName
                                }
                            } ],
                        widgetsButtons: [
                            {
                                name: "alfresco/buttons/AlfButton",
                                config: {
                                    label: this.message("Ok")
                                }
                            }
                            /*{
                                name: "alfresco/buttons/AlfButton",
                                config: {
                                    label: this.message("cancel.button.label"),
                                    publishTopic: this.cancelUserStatusUpdateTopic,
                                    publishPayload: payload
                                }
                            }*/
                        ]
                    });
                }
                this.statusDialog.show();
            },

            groupMembersLink: function openesdh_generate_group_members_link(groupName) {
                var topSpan, enclosingSpan, link;
                topSpan = '<span>';
                enclosingSpan = '</span>';
                link = '<a>' + groupName + '</a>';

                return topSpan+link+enclosingSpan;
                /*return {
                    name: "alfresco/navigation/Link",
                    config: {
                        label: groupName,
                        publishTopic: "OPENESDH_SHOW_CASE_GROUP_MEMBERS",
                        publishPayload: gShortName
                    }
                }*/
            }
        });
    });