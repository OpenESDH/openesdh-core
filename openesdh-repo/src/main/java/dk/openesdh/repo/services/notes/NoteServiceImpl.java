package dk.openesdh.repo.services.notes;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by syastrov on 2/6/15.
 */
public class NoteServiceImpl implements NoteService {

    private NodeService nodeService;

    public NodeRef createNote(NodeRef nodeRef, String content, String author) {
        if (!nodeService.hasAspect(nodeRef, OpenESDHModel.ASPECT_NOTE_NOTABLE)) {
            nodeService.addAspect(nodeRef, OpenESDHModel.ASPECT_NOTE_NOTABLE, null);
        }

        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(OpenESDHModel.PROP_NOTE_CONTENT, content);
        properties.put(ContentModel.PROP_AUTHOR, author);

        String name = "note-" + System.currentTimeMillis();
        return nodeService.createNode(nodeRef,
                OpenESDHModel.ASSOC_NOTE_NOTES,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)),
                OpenESDHModel.TYPE_NOTE_NOTE, properties).getChildRef();
    }

    @Override
    public void updateNote(NodeRef nodeRef, String content, String author) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(OpenESDHModel.PROP_NOTE_CONTENT, content);
        properties.put(ContentModel.PROP_AUTHOR, author);
        nodeService.setProperties(nodeRef, properties);
    }

    @Override
    public List<NodeRef> getNotes(NodeRef nodeRef) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs
                (nodeRef, OpenESDHModel.ASSOC_NOTE_NOTES, null);
        List<NodeRef> nodeRefs = new ArrayList<>();
        for (ChildAssociationRef childAssociationRef : childAssocs) {
            nodeRefs.add(childAssociationRef.getChildRef());
        }
        return nodeRefs;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
