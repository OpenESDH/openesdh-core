package dk.openesdh.repo.services.notes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by syastrov on 2/6/15.
 */
@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class NoteServiceImplIT {
    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("NoteService")
    protected NoteService noteService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    NodeRef parentNodeRef;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Create a parent node to test notes with
        parentNodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(),
                ContentModel.ASSOC_CONTAINS, QName.createQName
                        (NamespaceService.CONTENT_MODEL_1_0_URI, "testNode"),
                ContentModel.TYPE_CONTENT).getChildRef();
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        nodeService.deleteNode(parentNodeRef);
    }

    @Test
    public void testCreateNote() throws Exception {
        assertTrue("Notes list is empty", noteService.getNotes(parentNodeRef).isEmpty());

        NodeRef noteNodeRef = createNote();

        assertTrue("A nodeRef of the created note should exist, but it doesn't", nodeService.exists(noteNodeRef));
        assertTrue("Notes list should contain newly created note",
                noteService.getNotes(parentNodeRef).contains(noteNodeRef));
    }

    @Test
    public void testUpdateNote() throws Exception {
        NodeRef noteNodeRef = createNote();
        noteService.updateNote(noteNodeRef, "My updated note", "Updated author");
        List<NodeRef> notes = noteService.getNotes(parentNodeRef);

        assertEquals("Updated note should contain updated content", "My updated note",
                nodeService.getProperty(notes.get(0), OpenESDHModel.PROP_NOTE_CONTENT));

        assertEquals("Updated note should contain updated author", "Updated author",
                nodeService.getProperty(notes.get(0), ContentModel.PROP_AUTHOR));
    }

    @Test
    public void testDeleteNote() throws Exception {
        NodeRef noteNodeRef = createNote();
        assertTrue("A nodeRef of the created note should exist, but it doesn't", nodeService.exists(noteNodeRef));

        noteService.deleteNote(noteNodeRef);
        assertFalse("A nodeRef of the deleted note shouldn't exist", nodeService.exists(noteNodeRef));
    }

    private NodeRef createNote() {
        return noteService.createNote(parentNodeRef, "My note", "Author");
    }
}
