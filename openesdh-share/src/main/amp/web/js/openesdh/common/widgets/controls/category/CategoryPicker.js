/**
 * Category picker widget.
 */
define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/CategoryPicker.html",
        "dojo/store/Memory",
        "dojo/store/JsonRest",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/on",
        "dojo/dom-class",
        "./CategoryItem"],
    function (declare, _Widget, AlfCore, _Templated, template, Memory, JsonRest, lang, array, on, domClass, CategoryItem) {

        return declare([_Widget, AlfCore, _Templated], {

            templateString: template,

            cssRequirements: [
                {cssFile: "./css/CategoryPicker.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/CategoryPicker.properties"}
            ],

            /**
             * The ID of the root item, for where to start the picker.
             *
             * @instance
             * @default null
             * @type string
             */
            rootItemId: null,

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
             * The current item that we are viewing children of.
             *
             * @instance
             * @default null
             * @type string
             */
            currentItemId: null,

            /**
             * An array of parent items of the current item.
             *
             * @instance
             * @default null
             * @type string[]
             */
            parentItemIds: null,

            /**
             * The items currently selected in the picker.
             *
             * @instance
             * @default null
             * @type string[]
             */
            selectedItems: null,

            constructor: function() {
                this.inherited(arguments);

                if (this.selectedItems == null) {
                    this.selectedItems = [];
                }

                this.parentItemIds = [];

                this.widgets = [];
            },

            postCreate: function () {
                this.inherited(arguments);

                // Single-select doesn't have any concept of selected items in
                // the picker dialog..
                if (!this.multipleSelect) {
                    this.selectedItems = [];
                }

                if (this.store == null) {
                    // TODO: Use JSON store
//                  this.store = new JsonRest({
//                      target: this.getRestUrl()
//                  });
                    this.store = new Memory({
                        data: [
                            {
                                "name": "US Government",
                                "id": "root",
                                "children": [
                                    {
                                        "name": "Congress",
                                        "id": "congress",
                                        "children": [
                                            "blah1", "blah2"
                                        ]
                                    },
                                    {
                                        "name": "Executive",
                                        "id": "exec"
                                    },
                                    {
                                        "name": "Judicial",
                                        "id": "judicial"
                                    }
                                ]
                            },
                            {
                                "name": "Congress",
                                "id": "congress",
                                "children": [

                                    {
                                        "name": "Blah 1",
                                        "id": "blah1"
                                    },
                                    {
                                        "name": "Blah 2",
                                        "id": "blah2"
                                    }
                                ]
                            }
                        ],
                        getChildren: function (object) {
                            if ("children" in object && object.children) {
                                return object.children;
                            } else {
                                return [];
                            }
                        }
                    });
                }

                // TODO: Change when using JSON store
//                this.store.idProperty = 'id';

                on(this.backButtonNode, "click", lang.hitch(this, "_onBackClick"));

                this.alfSubscribe("CATEGORY_PICKER_ITEM_SELECT", lang.hitch(this, "_onSelectItem"));
                this.alfSubscribe("CATEGORY_PICKER_ITEM_BROWSE", lang.hitch(this, "_onBrowseItem"));

                this.browse(this.rootItemId, false);
            },

            _onBackClick: function () {
                if (this.parentItemIds.length > 0) {
                    this.browse(this.parentItemIds[this.parentItemIds.length - 1], false);
                }
            },

            _onSelectItem: function (payload) {
                this.select(payload.itemId);
            },

            _onBrowseItem: function (payload) {
                this.browse(payload.itemId, true);
            },

            select: function (itemId) {
                if (this.multipleSelect) {
                    this.selectedItems.push(itemId);
                    console.log(this.selectedItems);
                    this.selectionChanged();
                } else {
                    this.alfPublish("CATEGORY_PICKER_DIALOG_OK", {selectedItems: [itemId]});
                }
            },

            selectionChanged: function () {
                var _this = this;
                array.forEach(this.widgets, function (widget, i) {
                    var isSelected = array.indexOf(_this.selectedItems, widget.itemId) >= 0;
                    console.log("is selected?", widget.itemId, widget, isSelected);
                    widget.set("selected", isSelected);
                });
            },

            browse: function (itemId, isChild) {
                var _this = this;

                if (isChild) {
                    this.parentItemIds.push(this.currentItemId);
                } else {
                    this.parentItemIds.pop();
                }

                this.currentItemId = itemId;

                var currentItem = this.store.get(this.currentItemId);
                var children = this.store.getChildren(currentItem);

                // Update the header
                this.currentItemNode.innerHTML = currentItem.name;

                array.forEach(this.widgets, function (widget, i) {
                    widget.destroyRecursive();
                });

                this.widgets = [];

                var selectable = !this.canPickFirstLevelItems && this.currentItemId == this.rootItemId;

                array.forEach(children, function (child) {
                    var isSelected = array.indexOf(_this.selectedItems, child.id) >= 0;
                    var hasChildren = "children" in child && child.children;
                    var itemWidget = new CategoryItem({
                        itemLabel: child.name,
                        itemId: child.id,
                        selected: isSelected,
                        hasChildren: hasChildren,
                        selectable: selectable
                    });
                    _this.widgets.push(itemWidget);
                    itemWidget.placeAt(_this.containerNode);
                });

                this.currentItemChanged();
            },

            currentItemChanged: function () {
                if (this.parentItemIds.length > 0) {
                    domClass.remove(this.backButtonNode, "disabled");
                } else {
                    domClass.add(this.backButtonNode, "disabled");
                }
            },

            getRestUrl: function () {
                return Alfresco.constants.PROXY_URI + "slingshot/doclib/categorynode/node/";
            }
        });
    });