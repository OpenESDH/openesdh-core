package dk.openesdh.repo.services.xsearch;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;

public class XResultSet {

    List<NodeRef> nodeRefs;
    long numberFound;
    JSONArray nodes;

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
        return nodeRefs.size();
    }

    public JSONArray getNodes() {
        return nodes;
    }

    public void setNodes(JSONArray nodes) {
        this.nodes = nodes;
    }

    /**
     * Construct an empty result set.
     */
    XResultSet() {
        this.nodeRefs = new LinkedList<>();
        this.numberFound = 0;
    }

    XResultSet(List<NodeRef> nodeRefs) {
        this.nodeRefs = nodeRefs;
        this.numberFound = getLength();
    }

    XResultSet(List<NodeRef> nodeRefs, long numberFound) {
        this.nodeRefs = nodeRefs;
        this.numberFound = numberFound;
    }

    /**
     * Add the result set specified to this result set.
     * @param r
     */
    public void addAll(XResultSet r) {
        this.getNodeRefs().addAll(r.getNodeRefs());
        this.numberFound += r.numberFound;
    }
}