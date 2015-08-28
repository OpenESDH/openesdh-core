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
import dk.openesdh.repo.model.Note;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;

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
    @Qualifier("CaseService")
    protected CaseService caseService;

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

    private static final String TEST_NOTE_HEADLINE = "My headline";
    private static final String TEST_NOTE_CONTENT = "My note";

    private NodeRef parentNodeRef;
    private NodeRef caseNodeRef;
    private NodeRef noteNodeRef;
    private List<NodeRef> notes = new ArrayList<NodeRef>();

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        parentNodeRef = caseService.getCasesRootNodeRef();
        caseNodeRef = docTestHelper.createCaseBehaviourOn(TEST_CASE_NAME, parentNodeRef, CaseHelper.ADMIN_USER_NAME);
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
        nodes.addAll(notes);
        
        docTestHelper.removeNodesAndDeleteUsersInTransaction(nodes, Arrays.asList(caseNodeRef),
                Arrays.asList(NON_CASE_READER_USER_NAME, CaseHelper.DEFAULT_USERNAME, CASE_READER_USER_NAME));
    }

    @Test
    public void testCreateNote() throws Exception {
        assertTrue("Notes list is empty", noteService.getNotes(parentNodeRef).isEmpty());

        noteNodeRef = createNoteForNonCaseNode();

        assertTrue("A node of the created note should exist, but it doesn't", nodeService.exists(noteNodeRef));
        assertTrue("Notes list should contain newly created note", 
                noteService.getNotes(parentNodeRef)
                .stream()
                .anyMatch(note -> noteNodeRef.equals(note.getNodeRef()))
        );
    }

    @Test
    public void testUpdateNote() throws Exception {
        noteNodeRef = createNoteForNonCaseNode();

        Note note = new Note();
        note.setNodeRef(noteNodeRef);
        note.setParent(parentNodeRef);
        note.setHeadline("Updated headline");
        note.setAuthor("Updated author");
        note.setContent("My updated note");

        noteService.updateNote(note);

        NodeRef updatedNote = noteService.getNotes(parentNodeRef).get(0).getNodeRef();

        assertEquals("Updated note should contain updated headline", "Updated headline",
                nodeService.getProperty(updatedNote, OpenESDHModel.PROP_NOTE_HEADLINE));

        assertEquals("Updated note should contain updated content", "My updated note",
                nodeService.getProperty(updatedNote, OpenESDHModel.PROP_NOTE_CONTENT));

        assertEquals("Updated note should contain updated author", "Updated author",
                nodeService.getProperty(updatedNote, ContentModel.PROP_AUTHOR));
    }
    
    @Test
    public void testGetNotesPaging()throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);
        NodeRef firstNote = createCaseNote(CaseHelper.DEFAULT_USERNAME, "first note", "first note content");
        NodeRef secondNote = createCaseNote(CaseHelper.DEFAULT_USERNAME, "second note", "second note content");
        notes.addAll(Arrays.asList(firstNote, secondNote));

        List<Note> firstNoteList = noteService.getNotes(caseNodeRef, 0, 1).getResultList();
        Assert.assertEquals("Result notes list should contain only 1 element", 1, firstNoteList.size());
        Assert.assertEquals("Result notes list should contain first note", "first note", firstNoteList.get(0)
                .getHeadline());

        List<Note> secondNoteList = noteService.getNotes(caseNodeRef, 1, 1).getResultList();
        Assert.assertEquals("Result notes list should contain only 1 element", 1, secondNoteList.size());
        Assert.assertEquals("Result notes list should contain second note", "second note", secondNoteList.get(0)
                .getHeadline());

        List<Note> twoNotesList = noteService.getNotes(caseNodeRef, 0, 3).getResultList();
        Assert.assertEquals("Result notes list should contain 2 elements", 2, twoNotesList.size());
    }

    @Test
    public void testDeleteNote() throws Exception {
        noteNodeRef = createNoteForNonCaseNode();
        assertTrue("A nodeRef of the created note should exist, but it doesn't", nodeService.exists(noteNodeRef));

        noteService.deleteNote(noteNodeRef);
        assertFalse("A nodeRef of the deleted note shouldn't exist", nodeService.exists(noteNodeRef));
    }

    private NodeRef createNoteForNonCaseNode() {

        Note note = new Note();
        note.setParent(parentNodeRef);
        note.setAuthor("Author");
        note.setHeadline("Test headline");
        note.setContent("My note");

        return noteService.createNote(note);
    }

    @Test
    public void shouldAllowCaseWriterToCreateCaseNote() {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);
        noteNodeRef = createCaseNote(CaseHelper.DEFAULT_USERNAME);
        Assert.assertNotNull("Created note ref should not be null", noteNodeRef);
        assertTrue("A node of the created note should exist", nodeService.exists(noteNodeRef));
        assertTrue("Case should contain newly created note",
                noteService.getNotes(caseNodeRef)
                .stream()
                .anyMatch(note -> noteNodeRef.equals(note.getNodeRef())));
    }

    @Test
    public void shouldAllowCaseWriterToUpdateCaseNote() {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);
        noteNodeRef = createCaseNote(CaseHelper.DEFAULT_USERNAME);

        Note note = new Note();
        note.setNodeRef(noteNodeRef);
        note.setParent(caseNodeRef);
        note.setHeadline("Updated headline");
        note.setContent("My updated note");
        note.setAuthor("Updated author");

        noteService.updateNote(note);

        NodeRef updatedNote = noteService.getNotes(caseNodeRef).get(0).getNodeRef();

        assertEquals("Updated note should contain updated content", "Updated headline",
                nodeService.getProperty(updatedNote, OpenESDHModel.PROP_NOTE_HEADLINE));

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

        List<Note> notes = noteService.getNotes(caseNodeRef);
        assertTrue("Case note list should be empty after the note has been deleted", notes.isEmpty());
    }

    @Test
    public void shouldAllowCaseReaderToReadCaseNotes() {
        noteNodeRef = createCaseNote(CaseHelper.ADMIN_USER_NAME);

        AuthenticationUtil.setFullyAuthenticatedUser(CASE_READER_USER_NAME);

        NodeRef note = noteService.getNotes(caseNodeRef).get(0).getNodeRef();
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

        Note note = new Note();
        note.setNodeRef(noteNodeRef);
        note.setParent(caseNodeRef);
        note.setHeadline("Updated header");
        note.setContent("Updated note");
        note.setAuthor(CASE_READER_USER_NAME);

        AuthenticationUtil.setFullyAuthenticatedUser(CASE_READER_USER_NAME);
        thrown.expect(AccessDeniedException.class);
        noteService.updateNote(note);
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
        return createCaseNote(noteAuthor, TEST_NOTE_HEADLINE, TEST_NOTE_CONTENT);
    }
    
    private NodeRef createCaseNote(String noteAuthor, String headline, String content) {
        Note note = new Note();
        note.setParent(caseNodeRef);
        note.setHeadline(headline);
        note.setContent(content);
        note.setAuthor(noteAuthor);

        return noteService.createNote(note);
    }
}
