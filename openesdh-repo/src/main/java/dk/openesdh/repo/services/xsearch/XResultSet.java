package dk.openesdh.repo.services.xsearch;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

public class XResultSet {


    List<NodeRef> nodeRefs;
    int length;
    long numberFound;

    public void setNodeRefs(List<NodeRef> nodeRefs) {
        this.nodeRefs = nodeRefs;
    }

    public List<NodeRef> getNodeRefs() {
        return nodeRefs;
    }

    public long getNumberFound() {
        return numberFound;
    }

    public int getLength() {
        return length;
    }

    XResultSet(List<NodeRef> nodeRefs, int length) {
        this.nodeRefs = nodeRefs;
        this.length = length;
        this.numberFound = length;
    }

    XResultSet(List<NodeRef> nodeRefs, int length, long numberFound) {
        this.nodeRefs = nodeRefs;
        this.length = length;
        this.numberFound = numberFound;
    }
}