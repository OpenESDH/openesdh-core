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
        "dojo/on"],
    function (declare, _Widget, AlfCore, _Templated, template, lang, array, domConstruct, domClass, on) {

        return declare([_Widget, AlfCore, _Templated], {

            templateString: template,

            cssRequirements: [
                {cssFile: "./css/CategoryItem.css"}
            ],

            i18nRequirements: [
                {i18nFile: "./i18n/CategoryItem.properties"}
            ],

            /**
             * The text label of the item.
             *
             * @instance
             * @default ""
             * @type string
             */
            itemLabel: "",

            /**
             * The ID of the item.
             *
             * @instance
             * @default ""
             * @type string
             */
            itemId: "",

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

            postCreate: function () {
                this.inherited(arguments);

                if (!this.hasChildren) {
                    domConstruct.destroy(this.browseButtonNode);
                }

                on(this.labelNode, "click", lang.hitch(this, "_onLabelClick"));
                on(this.browseButtonNode, "click", lang.hitch(this, "_onBrowseClick"));
            },

            _onLabelClick: function () {
                this.alfPublish("CATEGORY_PICKER_ITEM_SELECT", {itemId: this.itemId});
            },

            _onBrowseClick: function () {
                this.alfPublish("CATEGORY_PICKER_ITEM_BROWSE", {itemId: this.itemId});
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