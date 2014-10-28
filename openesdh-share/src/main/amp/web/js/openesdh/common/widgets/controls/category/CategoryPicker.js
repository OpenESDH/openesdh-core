/**
 * Category picker widget.
 */
define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/CategoryPicker.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/on",
        "dojo/dom-class",
        "./CategoryItem"],
    function (declare, _Widget, AlfCore, CoreXhr, _Templated, template, lang, array, on, domClass, CategoryItem) {

        return declare([_Widget, AlfCore, CoreXhr, _Templated], {

            templateString: template,

            cssRequirements: [
                {cssFile: "./css/CategoryPicker.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/CategoryPicker.properties"}
            ],

            /**
             * The NodeRef of the root category
             *
             * @instance
             * @default null
             * @type string
             */
            rootNodeRef: null,

            /**
             * Whether the picker should allow multiple selections.
             *
             * @instance
             * @default false
             * @type boolean
             */
            multipleSelect: false,

            /**
             * Whether or not the first level items in the picker can be chosen.
             *
             * @instance
             * @default false
             * @type boolean
             */
            canPickFirstLevelItems: false,

            /**
             * The current path as an array of strings.
             *
             * @instance
             * @default null
             * @type string[]
             */
            path: null,

            /**
             * The items currently selected in the picker. An object of the
             * form: {nodeRef: {name, nodeRef}}.
             *
             * @instance
             * @default null
             * @type {object}
             */
            selectedItems: null,

            postCreate: function () {
                this.inherited(arguments);

                this.widgets = [];

                if (this.selectedItems == null) {
                    this.selectedItems = {};
                }

                if (this.path != null) {
                    this.initialPath = this.path;
                } else {
                    this.initialPath = [];
                }

                this.path = [];

                // Single-select doesn't have any concept of selected items in
                // the picker dialog..
                if (!this.multipleSelect) {
                    this.selectedItems = {};
                }

                on(this.backButtonNode, "click", lang.hitch(this, "_onBackClick"));

                this.alfSubscribe("CATEGORY_PICKER_ITEM_SELECT", lang.hitch(this, "_onSelectItem"), true);
                this.alfSubscribe("CATEGORY_PICKER_ITEM_BROWSE", lang.hitch(this, "_onBrowseItem"), true);

                this.browse();
            },

            _onBackClick: function () {
                if (this.path.length > 0) {
                    this.path.pop();
                    this.browse();
                }
            },

            _onSelectItem: function (payload) {
                this.select(payload.item.getItem());
            },

            _onBrowseItem: function (payload) {
                this.path.push(payload.item.itemName);
                this.browse();
            },

            select: function (item) {
                this.selectedItems[item.nodeRef] = item;
                if (this.multipleSelect) {
                    this.selectionChanged();
                } else {
                    this.alfPublish("CATEGORY_PICKER_DIALOG_OK", {selectedItems: this.selectedItems});
                }
            },

            selectionChanged: function () {
                var _this = this;
                array.forEach(this.widgets, function (widget, i) {
                    var isSelected = widget.nodeRef in _this.selectedItems;
                    widget.set("selected", isSelected);
                });
            },

            browse: function () {
                var _this = this;

                this._onPathChanged();

                this.getChildCategories(this.path.join("/"), function (response, config) {
                    array.forEach(this.widgets, function (widget, i) {
                        widget.destroyRecursive();
                    });

                    this.widgets = [];

                    // TODO: Test
                    var selectable = this.canPickFirstLevelItems || this.path.toString() != this.initialPath.toString();

                    var items = response.items;

                    array.forEach(items, function (child) {
                        var isSelected = child.nodeRef in _this.selectedItems;
                        var itemWidget = new CategoryItem({
                            itemName: child.name,
                            nodeRef: child.nodeRef,
                            hasChildren: child.hasChildren,
                            selected: isSelected,
                            selectable: selectable
                        });
                        _this.widgets.push(itemWidget);
                        itemWidget.placeAt(_this.containerNode);
                    });
                });
            },

            _onPathChanged: function () {
                // Update the header
                var currentPathElem = this.path[this.path.length - 1];
                this.currentItemNode.innerHTML = currentPathElem || "";

                if (this.path.length > 0) {
                    domClass.remove(this.backButtonNode, "disabled");
                } else {
                    domClass.add(this.backButtonNode, "disabled");
                }
            },

            getChildCategories: function (path, callback) {
                var url = this.getChildCategoriesUrl(path);
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    handleAs: "json",
                    successCallback: callback,
                    callbackScope: this
                });
            },

            getChildCategoriesUrl: function (path) {
                var nodeRef = new Alfresco.util.NodeRef(this.rootNodeRef),
                    uriTemplate = "slingshot/doclib/categorynode/node/" + encodeURI(nodeRef.uri) + "/" + Alfresco.util.encodeURIPath(path);
                return Alfresco.constants.PROXY_URI + uriTemplate + "?perms=false&children=true";
            }
        });
    });