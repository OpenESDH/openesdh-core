/**
 * Category picker.
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
        "dojo/dom-construct",
        "dojo/dom-class",
        "dojo/dom-style",
        "dijit/layout/BorderContainer",
        "dijit/layout/ContentPane",
        "openesdh/common/utils/openesdh",
        "./CategoryItem"],
    function (declare, _Widget, AlfCore, CoreXhr, _KeyNavMixin, _Templated, template, lang, array, on, domConstruct, domClass, domStyle,
              BorderContainer, ContentPane, openesdh,
              CategoryItem) {

        return declare([_Widget, AlfCore, CoreXhr, _KeyNavMixin, _Templated], {

            templateString: template,

            cssRequirements: [
                {cssFile: "./css/CategoryPicker.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/CategoryPicker.properties"}
            ],

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
             * @default true
             * @type boolean
             */
            canPickFirstLevelItems: true,

            /**
             * The initial path to use.
             */
            initialPath: "",

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

                if (this.initialPath) {
                    this.initialPath = this.initialPath.split("/");
                    this.path = lang.clone(this.initialPath);
                } else {
                    this.path = [];
                }

                // Single-select doesn't have any concept of selected items in
                // the picker dialog..
                if (!this.multipleSelect) {
                    this.selectedItems = {};
                    domConstruct.destroy(this.selectedItemsNode);
                }

                this.loadingNode.innerHTML = this.message("label.loading");
                this.emptySelectionNode.innerHTML = this.message("message.empty.selection");

                on(this.backButtonNode, "click", lang.hitch(this, "_onBackClick"));

                this.browse(true);
                this.selectionChanged();
            },

            startup: function () {
                this.inherited(arguments);
                if ("_startup" in this && this._startup) {
                    return;
                }

                this._startup = true;
                var bc = new BorderContainer({
                    style: (this.multipleSelect ?
                        "width: 750px; height: 300px;" :
                        "width: 100%; height: 350px;")
                });

                // create a ContentPane as the center pane in the BorderContainer
                var cp1 = new ContentPane({
                    region: "center",
                    content: this.containerNode,
                    splitter: true
                });
                bc.addChild(cp1);

                if (this.multipleSelect) {
                    // create a ContentPane as the right pane in the BorderContainer
                    var cp2 = new ContentPane({
                        region: "right",
                        style: "width: 45%",
                        splitter: true,
                        content: this.selectedItemsNode
                    });
                    bc.addChild(cp2);
                }

                // put the top level widget into the document, and then call startup()
                bc.placeAt(this.borderContainerNode);
                bc.startup();
                this.bc = bc;
            },

            resize: function () {
                // This is necessary so the BorderContainer resizes
                this.bc.resize(arguments);
                this.inherited(arguments);
            },

            _onBackClick: function () {
                this.back();
            },

            browseTo: function (item) {
                if (!item.hasChildren) {
                    return;
                }
                this.path.push(item.name);
                this.browse();
            },

            back: function () {
                if (this.path.length > 0 && this.path.length - 1 >= this.initialPath.length) {
                    this.path.pop();
                    this.browse();
                }
            },

            select: function (item, select) {
                if (select) {
                    this.selectedItems[item.nodeRef] = item;
                } else {
                    delete this.selectedItems[item.nodeRef];
                }

                if (this.multipleSelect) {
                    this.selectionChanged();
                } else {
                    this.pressOK();
                }
            },

            pressOK: function () {
                this.alfPublish("CATEGORY_PICKER_DIALOG_OK", {selectedItems: this.selectedItems});
            },

            selectionChanged: function () {
                if (!this.multipleSelect) {
                    return;
                }

                var _this = this;

                array.forEach(this.widgets, function (widget, i) {
                    var isSelected = widget.nodeRef in _this.selectedItems;
                    widget.set("selected", isSelected);
                });

                if (!openesdh.isEmpty(this.selectedItems)) {
                    domStyle.set(this.emptySelectionNode, "display", "none");
                }  else {
                    domStyle.set(this.emptySelectionNode, "display", "");
                }

                array.forEach(this.selectedItemWidgets, function (widget, i) {
                    widget.destroyRecursive();
                });
                this.selectedItemWidgets = [];
                for (var nodeRef in this.selectedItems) {
                    if (!this.selectedItems.hasOwnProperty(nodeRef)) continue;
                    var item = this.selectedItems[nodeRef];
                    var itemWidget = new CategoryItem({
                        picker: this,
                        itemName: item.name,
                        nodeRef: item.nodeRef,
                        hasChildren: false,
                        selectable: false,
                        removable: true
                    });
                    this.selectedItemWidgets.push(itemWidget);
                    itemWidget.placeAt(this.selectedItemsNode);
                }
            },

            browse: function (firstTime) {
                var _this = this;

                domStyle.set(_this.loadingNode, "display", "");

                this.getChildCategories(this.path.join("/"), function (response, config) {
                    domStyle.set(_this.loadingNode, "display", "none");

                    this._onPathChanged();

                    array.forEach(this.widgets, function (widget, i) {
                        widget.destroyRecursive();
                    });

                    this.widgets = [];

                    var selectable = this.canPickFirstLevelItems || this.path.toString() != this.initialPath.toString();

                    var items = response.items;

                    this.domNode.setAttribute("tabIndex", "0");

                    array.forEach(items, function (child) {
                        var isSelected = child.nodeRef in _this.selectedItems;
                        var itemWidget = new CategoryItem({
                            picker: _this,
                            itemName: child.name,
                            itemTitle: child.title,
                            itemDescription: child.description,
                            nodeRef: child.nodeRef,
                            hasChildren: child.hasChildren,
                            selected: isSelected,
                            selectable: selectable
                        });
                        _this.widgets.push(itemWidget);
                        itemWidget.placeAt(_this.containerNode);
                        itemWidget.domNode.setAttribute("tabIndex", "-1");
                    });

                    if (!firstTime) {
                        // We should not call focus on the first time,
                        // as something else seems to be doing that for us,
                        // leading to problems focusing the first item.
                        this.focus();
                    }
                });
            },

            _onPathChanged: function () {
                // Update the header
                this.currentItemNode.innerHTML = this.path.join("/");

                if (this.path.length > 0 && this.path.length > this.initialPath.length) {
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
                // Alfresco's webscript ignores the root noderef
                var nodeRef = new Alfresco.util.NodeRef("workspace://SpacesStore/abc"),
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