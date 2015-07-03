package dk.openesdh.repo.services.notes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by syastrov on 2/6/15.
 */
public class NoteServiceImpl implements NoteService {

    private NodeService nodeService;

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRef createNote(NodeRef parentNodeRef, String content, String author) {
        if (!nodeService.hasAspect(parentNodeRef, OpenESDHModel.ASPECT_NOTE_NOTABLE)) {
            nodeService.addAspect(parentNodeRef, OpenESDHModel.ASPECT_NOTE_NOTABLE, null);
        }

        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(OpenESDHModel.PROP_NOTE_CONTENT, content);
        properties.put(ContentModel.PROP_AUTHOR, author);

        String name = "note-" + System.currentTimeMillis();
        return nodeService.createNode(parentNodeRef,
                OpenESDHModel.ASSOC_NOTE_NOTES,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)),
                OpenESDHModel.TYPE_NOTE_NOTE, properties).getChildRef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNote(NodeRef noteRef, String content, String author) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(OpenESDHModel.PROP_NOTE_CONTENT, content);
        properties.put(ContentModel.PROP_AUTHOR, author);
        nodeService.setProperties(noteRef, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NodeRef> getNotes(NodeRef parentNodeRef) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs
                (parentNodeRef, OpenESDHModel.ASSOC_NOTE_NOTES, null);
        List<NodeRef> noteRefs = new ArrayList<>();
        for (ChildAssociationRef childAssociationRef : childAssocs) {
            noteRefs.add(childAssociationRef.getChildRef());
        }
        return noteRefs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNote(NodeRef noteRef) {
        nodeService.deleteNode(noteRef);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
