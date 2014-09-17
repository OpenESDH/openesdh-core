/**
 * A service to return the user's saved searches.
 * @module openesdh/xsearch/SavedSearchService
 * @extends alfresco/core/Core
 * @mixes alfresco/core/CoreXhr
 * @mixes module:openesdh/xsearch/_SavedSearchTopicsMixin
 */
define(["dojo/_base/declare",
"alfresco/core/Core",
"alfresco/core/CoreXhr",
"dojo/_base/lang",
"openesdh/xsearch/_SavedSearchTopicsMixin",
"alfresco/services/_PreferenceServiceTopicMixin"],
function(declare, Core, CoreXhr, lang, _SavedSearchTopicsMixin, _PreferenceServiceTopicMixin) {
    return declare([Core, CoreXhr, _SavedSearchTopicsMixin, _PreferenceServiceTopicMixin], {
        startup: function () {
            // Subscribe to saving xsearch
            this.alfSubscribe(this.getSavedSearchTopic, lang.hitch(this, "getSavedSearch"));
            this.alfSubscribe(this.setSavedSearchTopic, lang.hitch(this, "setSavedSearch"));
        },

        getSavedSearches: function (payload) {
            if (!lang.exists("callback", payload) ||
                typeof payload.callback !== "function" ||
                !lang.exists("callbackScope", payload))
            {
                this.alfLog("warn", "A request was made to get a saved search, but the callback information was missing", payload);
            }
            this.alfPublish(this.getPreferenceTopic, {
                preference: 'savedSearches',
                callback: payload.callback,
                callbackScope: payload.callbackScope
            });
        },
        
        /**
         * Takes a xsearch object and saves it
         * @param {object} search
         * @param {string} name of the xsearch
         * @returns {undefined}
         */
        saveSearch: function (search, name) {
            var _this = this;
            this.getSavedSearches(function (savedSearches) {
                this.alfLog("debug", "Saved searches", savedSearches);

                savedSearches[name] = search;
                _this.setSavedSearches(savedSearches, function (response) {
                    // Display the success message
                    Alfresco.util.PopupManager.displayMessage(
                    {
                        text: _this.message('message.save_search.success')
                    });
                    
                    // Delay so the user can see the message, before redirecting
                    window.setTimeout(function () {
                        if (name === _this.DEFAULT_SEARCH_NAME) {
                            location.href = _this.scriptURI;
                        } else {
                            location.href = _this.scriptURI + '?search=' + encodeURIComponent(name);
                        }
                    }, 500);
                });
            });
        },
        
        deleteSearch: function (name) {
            var _this = this;
            this.getSavedSearches(function (savedSearches) {
                delete savedSearches[name];
                _this.setSavedSearches(savedSearches,
                    function (response) {
                        // Display the success message
                        Alfresco.util.PopupManager.displayMessage(
                        {
                            text: _this.message('message.delete_search.success')
                        });

                        // Delay so the user can see the message, before reloading
                        window.setTimeout(function () {
                            if (name === _this.searchName) {
                                // Redirect to the default xsearch if the current xsearch was deleted
                                location.href = _this.scriptURI;
                            } else {
                                // Reload
                                window.location.reload();
                            }
                        }, 500);
                    }, _this.message('message.delete_search.failure'));
            });
        }
    });
});