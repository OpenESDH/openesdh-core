package dk.openesdh.repo.services.notes;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.model.OpenESDHModel;
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

import java.util.List;

import static org.junit.Assert.*;

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

    NodeRef nodeRef;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Create a node to test with
        nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(),
                ContentModel.ASSOC_CONTAINS, QName.createQName
                        (NamespaceService.CONTENT_MODEL_1_0_URI, "testNode"),
                ContentModel.TYPE_CONTENT).getChildRef();
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        nodeService.deleteNode(nodeRef);
    }

    @Test
    public void testCreateNote() throws Exception {
        assertTrue("Notes list is empty", noteService.getNotes(nodeRef).isEmpty());

        NodeRef noteNodeRef = noteService.createNote(nodeRef, "My note",
                "Author");

        assertTrue("Created note nodeRef exists", nodeService.exists(noteNodeRef));
        assertTrue("Notes list contains newly created note", noteService.getNotes(nodeRef).contains(noteNodeRef));
    }

    @Test
    public void testUpdateNote() throws Exception {
        NodeRef noteNodeRef = noteService.createNote(nodeRef, "My note", "Author");
        noteService.updateNote(noteNodeRef, "My updated note", "Updated author");
        List<NodeRef> notes = noteService.getNotes(nodeRef);
        assertEquals("Updated note contains updated content", "My updated note",
                nodeService.getProperty(
                        notes.get(0), OpenESDHModel.PROP_NOTE_CONTENT));
    }
}
