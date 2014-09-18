package dk.openesdh.repo.services.xsearch;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

public class XResultSet {
        List<NodeRef> nodeRefs;
        int length;

        public List<NodeRef> getNodeRefs() {
            return nodeRefs;
        }

        public int getLength() {
            return length;
        }

        XResultSet(List<NodeRef> nodeRefs, int length) {
            this.nodeRefs = nodeRefs;
            this.length = length;
        }
    }