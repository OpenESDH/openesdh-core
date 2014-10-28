/**
 * Category picker widget.
 */
define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dijit/_KeyNavMixin",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/CategoryPicker.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/on",
        "dojo/dom-class",
        "dojo/dom-style",
        "./CategoryItem"],
    function (declare, _Widget, AlfCore, CoreXhr, _KeyNavMixin, _Templated, template, lang, array, on, domClass, domStyle, CategoryItem) {

        return declare([_Widget, AlfCore, CoreXhr, _KeyNavMixin, _Templated], {

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

                this.loadingNode.innerHTML = this.message("label.loading");

                on(this.backButtonNode, "click", lang.hitch(this, "_onBackClick"));

                this.alfSubscribe("CATEGORY_PICKER_ITEM_SELECT", lang.hitch(this, "_onSelectItem"), true);
                this.alfSubscribe("CATEGORY_PICKER_ITEM_BROWSE", lang.hitch(this, "_onBrowseItem"), true);

                this.browse();
            },

            _onBackClick: function () {
                this.back();
            },

            _onSelectItem: function (payload) {
                this.select(payload.item.getItem());
            },

            _onBrowseItem: function (payload) {
                this.browseTo(payload.item.getItem());
            },

            browseTo: function (item) {
                if (!item.hasChildren) {
                    return;
                }
                this.path.push(item.name);
                this.browse();
            },

            back: function () {
                if (this.path.length > 0) {
                    this.path.pop();
                    this.browse();
                }
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

                domStyle.set(_this.loadingNode, "display", "inline");

                this.getChildCategories(this.path.join("/"), function (response, config) {
                    domStyle.set(_this.loadingNode, "display", "none");

                    this._onPathChanged();

                    array.forEach(this.widgets, function (widget, i) {
                        widget.destroyRecursive();
                    });

                    this.widgets = [];

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

                    this.domNode.setAttribute("tabIndex", "0");

                    if (this.widgets.length > 0) {
                        this.widgets[0].focus();
                    } else {
                        this.domNode.focus();
                    }
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
            },


            // Keyboard navigation

            // Specifies which DOMNode children can be focused
            childSelector: ".category-item .item-label",

            _focusedChildIndex: function(children){
                // summary:
                //      Helper method to return the index of the currently focused child in the array
                return array.indexOf(children, this.focusedChild);
            },


            // Home/End key support
            _getFirst: function(){
                return this.widgets[0];
            },
            _getLast: function(){
                var children = this.widgets;
                return children[children.length - 1];
            },

            _onLeftArrow: function(){
                // Go back
                this.back();
            },
            _onRightArrow: function(){
                // Browse to
                this.browseTo(this.focusedChild.getItem());
            },
            _onDownArrow: function(){
                var children = this.widgets;
                this.focusChild(children[(this._focusedChildIndex(children) + 1) % children.length]);
            },
            _onUpArrow: function(){
                var children = this.widgets;
                this.focusChild(children[(this._focusedChildIndex(children) - 1 + children.length) % children.length]);
            },

            // Letter key navigation support
            _getNext: function(child){
                var children = this.widgets;
                return children[(array.indexOf(children, child) + 1) % children.length];
            }
        });
    });