define(["dojo/_base/declare",
        "alfresco/services/ActionService",
        "dojo/_base/lang",
        "dojo/_base/array"],
    function(declare, ActionService, lang, array) {
        return declare([ActionService], {
            /**
             * Calls a JavaScript function provided (or registered with) the Alfresco.DocLibToolbar widget.
             *
             * @instance
             */
            onActionJournalize: function (payload, nodes) {
                var node = nodes[0].node;
                var nodeRef = node.nodeRef;
                var isCaseDoc = node.aspects.indexOf("oe:caseId") !== -1;
                this.alfPublish("OPENESDH_JOURNALIZE", {
                    nodeRef: nodeRef,
                    isCaseDoc: isCaseDoc
                });
            },

            onActionUnJournalize: function (payload, nodes) {
                var node = nodes[0].node;
                var nodeRef = node.nodeRef;
                var isCaseDoc = node.aspects.indexOf("oe:caseId") !== -1;
                this.alfPublish("OPENESDH_UNJOURNALIZE", {
                    nodeRef: nodeRef,
                    isCaseDoc: isCaseDoc
                });
            }
        });
    });