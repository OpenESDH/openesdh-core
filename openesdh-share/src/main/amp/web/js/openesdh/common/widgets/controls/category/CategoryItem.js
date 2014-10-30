/**
 * Category picker widget.
 */
define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "alfresco/core/Core",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/CategoryItem.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/dom-construct",
        "dojo/dom-class",
        "dojo/dom-attr",
        "dojo/on"],
    function (declare, _Widget, AlfCore, _Templated, template, lang, array, domConstruct, domClass, domAttr, on) {

        return declare([_Widget, AlfCore, _Templated], {

            templateString: template,

            cssRequirements: [
                {cssFile: "./css/CategoryItem.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/CategoryItem.properties"}
            ],

            /**
             * The name of the item.
             *
             * @instance
             * @default ""
             * @type string
             */
            itemName: "",

            /**
             * The nodeRef of the item.
             *
             * @instance
             * @default ""
             * @type string
             */
            nodeRef: "",

            /**
             * Whether the item has children.
             *
             * @instance
             * @default false
             * @type boolean
             */
            hasChildren: false,

            /**
             * Whether the item is currently selected.
             *
             * @instance
             * @default false
             * @type boolean
             */
            selected: false,

            /**
             * Whether the item can be selected.
             *
             * @instance
             * @default true
             * @type boolean
             */
            selectable: true,

            /**
             * Whether the item can be removed.
             *
             * @instance
             * @default false
             * @type boolean
             */
            removable: false,

            postCreate: function () {
                this.inherited(arguments);

                if (!this.removable) {
                    domConstruct.destroy(this.removeButtonNode);
                }

                if (!this.hasChildren) {
                    domConstruct.destroy(this.browseButtonNode);
                }

                var iconUrl = Alfresco.constants.URL_CONTEXT + "res/components/images/filetypes/generic-category-32.png";
                domAttr.set(this.iconImgNode, "src", iconUrl);

                if (this.selectable) {
                    on(this.labelNode, "click", lang.hitch(this, "_onLabelClick"));
                    // Clicking on the item should be like clicking on the label.
                    on(this.domNode, "click", lang.hitch(this, "_onLabelClick"));
                } else {
                    domAttr.remove(this.labelNode, "href");
                }

                on(this.removeButtonNode, "click", lang.hitch(this, "_onRemoveClick"));
                on(this.browseButtonNode, "click", lang.hitch(this, "_onBrowseClick"));
            },

            focus: function(){
                this.labelNode.focus();
            },

            getItem: function () {
                return {name: this.itemName, nodeRef: this.nodeRef, hasChildren: this.hasChildren};
            },

            _onLabelClick: function (e) {
                if (!this.selected) {
                    this.alfPublish("CATEGORY_PICKER_ITEM_SELECT", {item: this}, true);
                } else {
                    this.alfPublish("CATEGORY_PICKER_ITEM_DESELECT", {item: this}, true);
                }
                e.stopPropagation();
            },

            _onBrowseClick: function (e) {
                this.alfPublish("CATEGORY_PICKER_ITEM_BROWSE", {item: this}, true);
                e.stopPropagation();
            },

            _onRemoveClick: function (e) {
                this.alfPublish("CATEGORY_PICKER_ITEM_DESELECT", {item: this}, true);
                console.log("Remove", this);
                e.stopPropagation();
            },

            _setSelectedAttr: function (selected) {
                this._set("selected", selected);
                if (selected) {
                    domClass.add(this.itemNode, "item-selected");
                } else {
                    domClass.remove(this.itemNode, "item-selected");
                }
            }
        });
    });