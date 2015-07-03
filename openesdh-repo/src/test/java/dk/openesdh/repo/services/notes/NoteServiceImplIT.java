package dk.openesdh.repo.services.notes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by syastrov on 2/6/15.
 */
@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class NoteServiceImplIT {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("NoteService")
    protected NoteService noteService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    @Autowired
    @Qualifier("AuthorityService")
    protected AuthorityService authorityService;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    protected CaseDocumentTestHelper docTestHelper;

    @Autowired
    @Qualifier("TestCaseHelper")
    protected CaseHelper caseHelper;

    private static final String TEST_CASE_NAME = "Test_case";
    private static final String TEST_FOLDER_NAME = "Test_folder";
    private static final String CASE_SIMPLE_WRITER_GROUP_NAME = AuthorityType.GROUP.getPrefixString()
            + OpenESDHModel.PERMISSION_NAME_CASE_SIMPLE_WRITER;

    private static final String CASE_SIMPLE_READER_GROUP_NAME = AuthorityType.GROUP.getPrefixString()
            + OpenESDHModel.PERMISSION_NAME_CASE_SIMPLE_READER;

    private static final String CASE_READER_USER_NAME = "caseReaderUser";
    private static final String NON_CASE_READER_USER_NAME = "nonCaseReaderUser";

    private static final String TEST_NOTE_CONTENT = "My note";

    private NodeRef parentNodeRef;
    private NodeRef caseNodeRef;
    private NodeRef noteNodeRef;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Create a parent node to test notes with
        parentNodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(),
                ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, TEST_FOLDER_NAME),
                ContentModel.TYPE_FOLDER).getChildRef();

        caseNodeRef = docTestHelper
                .createCaseBehaviourOn(TEST_CASE_NAME, parentNodeRef, CaseHelper.ADMIN_USER_NAME);

        caseHelper.createDummyUser(NON_CASE_READER_USER_NAME);

        caseHelper.createDummyUser();
        authorityService.addAuthority(CASE_SIMPLE_WRITER_GROUP_NAME, CaseHelper.DEFAULT_USERNAME);

        caseHelper.createDummyUser(CASE_READER_USER_NAME);
        authorityService.addAuthority(CASE_SIMPLE_READER_GROUP_NAME, CASE_READER_USER_NAME);
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        List<NodeRef> nodes = new ArrayList<NodeRef>();
        if (noteNodeRef != null) {
            nodes.add(noteNodeRef);
        }
        nodes.add(parentNodeRef);
        docTestHelper.removeNodesAndDeleteUsersInTransaction(nodes, Arrays.asList(caseNodeRef),
                Arrays.asList(NON_CASE_READER_USER_NAME, CaseHelper.DEFAULT_USERNAME, CASE_READER_USER_NAME));
    }

    @Test
    public void testCreateNote() throws Exception {
        assertTrue("Notes list is empty", noteService.getNotes(parentNodeRef).isEmpty());

        noteNodeRef = createNoteForNonCaseNode();

        assertTrue("A node of the created note should exist, but it doesn't", nodeService.exists(noteNodeRef));
        assertTrue("Notes list should contain newly created note",
                noteService.getNotes(parentNodeRef).contains(noteNodeRef));
    }

    @Test
    public void testUpdateNote() throws Exception {
        noteNodeRef = createNoteForNonCaseNode();
        noteService.updateNote(noteNodeRef, "My updated note", "Updated author");
        List<NodeRef> notes = noteService.getNotes(parentNodeRef);

        assertEquals("Updated note should contain updated content", "My updated note",
                nodeService.getProperty(notes.get(0), OpenESDHModel.PROP_NOTE_CONTENT));

        assertEquals("Updated note should contain updated author", "Updated author",
                nodeService.getProperty(notes.get(0), ContentModel.PROP_AUTHOR));
    }

    @Test
    public void testDeleteNote() throws Exception {
        noteNodeRef = createNoteForNonCaseNode();
        assertTrue("A nodeRef of the created note should exist, but it doesn't", nodeService.exists(noteNodeRef));

        noteService.deleteNote(noteNodeRef);
        assertFalse("A nodeRef of the deleted note shouldn't exist", nodeService.exists(noteNodeRef));
    }

    private NodeRef createNoteForNonCaseNode() {
        return noteService.createNote(parentNodeRef, "My note", "Author");
    }

    @Test
    public void shouldAllowCaseWriterToCreateCaseNote() {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);
        noteNodeRef = createCaseNote(CaseHelper.DEFAULT_USERNAME);
        Assert.assertNotNull("Created note ref should not be null", noteNodeRef);
        assertTrue("A node of the created note should exist", nodeService.exists(noteNodeRef));
        assertTrue("Case should contain newly created note",
                noteService.getNotes(caseNodeRef).contains(noteNodeRef));
    }

    @Test
    public void shouldAllowCaseWriterToUpdateCaseNote() {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);
        noteNodeRef = createCaseNote(CaseHelper.DEFAULT_USERNAME);
        noteService.updateNote(noteNodeRef, "My updated note", "Updated author");

        NodeRef updatedNote = noteService.getNotes(caseNodeRef).get(0);

        assertEquals("Updated note should contain updated content", "My updated note",
                nodeService.getProperty(updatedNote, OpenESDHModel.PROP_NOTE_CONTENT));

        assertEquals("Updated note should contain updated author", "Updated author",
                nodeService.getProperty(updatedNote, ContentModel.PROP_AUTHOR));
    }

    @Test
    public void shouldAllowCaseWriterToDeleteCaseNote() {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);
        noteNodeRef = createCaseNote(CaseHelper.DEFAULT_USERNAME);
        Assert.assertNotNull("Created note ref should not be null", noteNodeRef);

        noteService.deleteNote(noteNodeRef);
        assertFalse("A nodeRef of the deleted note shouldn't exist", nodeService.exists(noteNodeRef));

        List<NodeRef> notes = noteService.getNotes(caseNodeRef);
        assertTrue("Case note list should be empty after the note has been deleted", notes.isEmpty());
    }

    @Test
    public void shouldAllowCaseReaderToReadCaseNotes() {
        noteNodeRef = createCaseNote(CaseHelper.ADMIN_USER_NAME);

        AuthenticationUtil.setFullyAuthenticatedUser(CASE_READER_USER_NAME);

        NodeRef note = noteService.getNotes(caseNodeRef).get(0);
        String noteContent = (String) nodeService.getProperty(note, OpenESDHModel.PROP_NOTE_CONTENT);
        Assert.assertEquals("The note content retrieved for case reader doesn't match the created note",
                TEST_NOTE_CONTENT, noteContent);
    }

    @Test
    public void shouldDisallowNonCaseReaderToReadCaseNotes() {
        noteNodeRef = createCaseNote(CaseHelper.ADMIN_USER_NAME);
        AuthenticationUtil.setFullyAuthenticatedUser(NON_CASE_READER_USER_NAME);
        thrown.expect(AccessDeniedException.class);
        noteService.getNotes(caseNodeRef);
    }

    @Test
    public void shouldDisallowNonCaseWriterToCreateCaseNote() {
        AuthenticationUtil.setFullyAuthenticatedUser(CASE_READER_USER_NAME);
        thrown.expect(AccessDeniedException.class);
        noteNodeRef = createCaseNote(CASE_READER_USER_NAME);
    }

    @Test
    public void shoulDisallowNonCaseWriterToUpdateCaseNote() {
        noteNodeRef = createCaseNote(CaseHelper.ADMIN_USER_NAME);
        Assert.assertNotNull("Created note ref should not be null", noteNodeRef);

        AuthenticationUtil.setFullyAuthenticatedUser(CASE_READER_USER_NAME);
        thrown.expect(AccessDeniedException.class);
        noteService.updateNote(noteNodeRef, "My updated note", CASE_READER_USER_NAME);
    }

    @Test
    public void shouldDisallowNonCaseWriterToDeleteCaseNote() {
        noteNodeRef = createCaseNote(CaseHelper.ADMIN_USER_NAME);
        Assert.assertNotNull("Created note ref should not be null", noteNodeRef);

        AuthenticationUtil.setFullyAuthenticatedUser(CASE_READER_USER_NAME);
        thrown.expect(AccessDeniedException.class);
        noteService.deleteNote(noteNodeRef);
    }

    private NodeRef createCaseNote(String noteAuthor) {
        return noteService.createNote(caseNodeRef, TEST_NOTE_CONTENT, noteAuthor);
    }
}
