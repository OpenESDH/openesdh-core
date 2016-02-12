package dk.openesdh.repo.services.notes;

import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.Note;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.model.ResultSet;
import dk.openesdh.repo.services.contacts.ContactService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by syastrov on 2/6/15.
 */
@Service("NoteService")
public class NoteServiceImpl implements NoteService {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("ContactService")
    private ContactService contactService;

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRef createNote(Note note) {
        if (!nodeService.hasAspect(note.getParent(), OpenESDHModel.ASPECT_NOTE_NOTABLE)) {
            nodeService.addAspect(note.getParent(), OpenESDHModel.ASPECT_NOTE_NOTABLE, null);
        }

        Map<QName, Serializable> properties = getNoteProperties(note);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Note> getNotes(NodeRef parentNodeRef) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parentNodeRef, OpenESDHModel.ASSOC_NOTE_NOTES, null);
        return getNotesFromChildAssociations(parentNodeRef, childAssocs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet<Note> getNotes(NodeRef parentNodeRef, int startIndex, int pageSize) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parentNodeRef,
                OpenESDHModel.ASSOC_NOTE_NOTES, null);
        int totalItems = childAssocs.size();
        int resultEnd = startIndex + pageSize;
        if (totalItems < resultEnd) {
            resultEnd = totalItems;
        }
        List<Note> resultList = getNotesFromChildAssociations(parentNodeRef,
                childAssocs.subList(startIndex, resultEnd));
        ResultSet<Note> result = new ResultSet<>();
        result.setTotalItems(totalItems);
        result.setResultList(resultList);
        return result;
    }

    protected List<Note> getNotesFromChildAssociations(NodeRef parentNodeRef, List<ChildAssociationRef> childAssocs) {
        return childAssocs.stream()
                .map(assoc -> getNote(parentNodeRef, assoc.getChildRef()))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNote(Note note) {
        nodeService.setProperties(note.getNodeRef(), getNoteProperties(note));
    }

    private Note getNote(NodeRef parentNodeRef, NodeRef noteNodeRef) {
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

    private Map<QName, Serializable> getNoteProperties(Note note) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(OpenESDHModel.PROP_NOTE_HEADLINE, note.getHeadline());
        properties.put(OpenESDHModel.PROP_NOTE_CONTENT, note.getContent());
        properties.put(ContentModel.PROP_AUTHOR, note.getAuthor());
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNote(NodeRef noteRef) {
        nodeService.deleteNode(noteRef);
    }
}
