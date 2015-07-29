package dk.openesdh.repo.services.notes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.Note;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;

/**
 * Created by syastrov on 2/6/15.
 */
public class NoteServiceImpl implements NoteService {

    private NodeService nodeService;

    private PersonService personService;

    private ContactService contactService;

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

    @Override
    public NodeRef createNote(Note note) {
        if (!nodeService.hasAspect(note.getParent(), OpenESDHModel.ASPECT_NOTE_NOTABLE)) {
            nodeService.addAspect(note.getParent(), OpenESDHModel.ASPECT_NOTE_NOTABLE, null);
        }

        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(OpenESDHModel.PROP_NOTE_HEADLINE, note.getHeadline());
        properties.put(OpenESDHModel.PROP_NOTE_CONTENT, note.getContent());
        properties.put(ContentModel.PROP_AUTHOR, note.getAuthor());

        String name = "note-" + System.currentTimeMillis();
        NodeRef noteNodeRef = nodeService.createNode(note.getParent(), OpenESDHModel.ASSOC_NOTE_NOTES,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)),
                OpenESDHModel.TYPE_NOTE_NOTE, properties).getChildRef();
        
        note.getConcernedParties()
                .stream()
                .forEach(
                        partyNodeRef -> nodeService.createAssociation(noteNodeRef, partyNodeRef,
                                OpenESDHModel.ASSOC_NOTE_CONCERNED_PARTIES));
        
        return noteNodeRef;
    }

    @Override
    public List<Note> getObjectNotes(NodeRef parentNodeRef) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs
                (parentNodeRef, OpenESDHModel.ASSOC_NOTE_NOTES, null);
        return childAssocs
                .stream()
                .map(assoc -> getNote(parentNodeRef, assoc.getChildRef()))
                .collect(Collectors.toList());
    }
    
    private Note getNote(NodeRef parentNodeRef, NodeRef noteNodeRef){
        Map<QName, Serializable> props = nodeService.getProperties(noteNodeRef);
        Note note = new Note();
        note.setParent(parentNodeRef);
        note.setNodeRef(noteNodeRef);
        note.setAuthor(props.get(ContentModel.PROP_AUTHOR).toString());
        note.setHeadline(props.get(OpenESDHModel.PROP_NOTE_HEADLINE).toString());
        note.setContent(props.get(OpenESDHModel.PROP_NOTE_CONTENT).toString());
        note.setCreator(props.get(ContentModel.PROP_CREATOR).toString());
        note.setCreated(((Date) props.get(ContentModel.PROP_CREATED)));
        note.setCreated(((Date) props.get(ContentModel.PROP_MODIFIED)));

        NodeRef personNodeRef = personService.getPersonOrNull(note.getAuthor());
        if (personNodeRef != null) {
            note.setAuthorInfo(personService.getPerson(personNodeRef));
        }
        
        List<ContactInfo> concernedPartiesInfo = nodeService.getTargetAssocs(noteNodeRef, RegexQNamePattern.MATCH_ALL)
                .stream()
                .filter(assoc -> OpenESDHModel.ASSOC_NOTE_CONCERNED_PARTIES.equals(assoc.getTypeQName()))
                .map(assoc -> contactService.getContactInfo(assoc.getTargetRef()))
                .collect(Collectors.toList());

        note.setConcernedPartiesInfo(concernedPartiesInfo);

        return note;
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

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }
}
