/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.openesdh.repo.services.files;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml", "classpath:alfresco/extension/openesdh-test-context.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OeFilesServiceImplIT {

    private final Logger logger = LoggerFactory.getLogger(OeFilesServiceImplIT.class);

    @Autowired
    @Qualifier("OeFilesService")
    private OeFilesService filesService;
    @Autowired
    @Qualifier("ContactService")
    private ContactService contactService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    private static final String NAME = "test1";
    private final InputStream fileBytes = new ByteArrayInputStream("Test file content".getBytes());
    private NodeRef file1;
    private NodeRef organization1;
    private String testComment;
    private String testFileTitle;

    @Before
    public void setUp() {
        setAdminUserAsFullyAuthenticatedUser();
        long timestamp = new Date().getTime();
        organization1 = createTestContact(NAME + timestamp + "@opene.dk");
        testComment = "Test Comment On file " + timestamp;
        testFileTitle = "test_file_" + timestamp + ".txt";
        file1 = filesService.addFile(organization1, testFileTitle, MimetypeMap.MIMETYPE_TEXT_PLAIN, fileBytes, testComment);
    }

    @After
    public void tearDown() {
        try {
            if (nodeService.exists(file1)) {
                filesService.delete(file1);
            }
        } catch (Exception unimportantTearDownException) {
            logger.warn("tearDown exception: failed to delete created file", unimportantTearDownException);
        }
        try {
            if (nodeService.exists(organization1)) {
                nodeService.deleteNode(organization1);
            }
        } catch (Exception unimportantTearDownException) {
            logger.warn("tearDown exception: failed to delete created organization", unimportantTearDownException);
        }
    }

    private NodeRef createTestContact(String email) {
        HashMap<QName, Serializable> personProps = new HashMap<>();
        personProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, email);
        personProps.put(OpenESDHModel.PROP_CONTACT_FIRST_NAME, NAME);
        personProps.put(OpenESDHModel.PROP_CONTACT_LAST_NAME, NAME);
        return contactService.createContact(email, ContactType.PERSON.name(), personProps);
    }

    @Test
    public void testGetFile() {
        JSONObject file = filesService.getFile(file1);
        checkIfExpectedFile(file);
    }

    @Test
    public void testGetFiles() {
        List<JSONObject> organizationFiles = filesService.getFiles(organization1);
        assertEquals("organization has incorrect number of files", 1, organizationFiles.size());
        checkIfExpectedFile(organizationFiles.get(0));
    }

    private void checkIfExpectedFile(JSONObject file) {
        assertEquals("file title is not as expected", testFileTitle, ((JSONObject) file.get("cm")).get("title"));
        JSONArray comments = (JSONArray) file.get("comments");
        assertEquals("file has incorrect number of comments", 1, comments.size());
        assertEquals("comment is not as expected", testComment, ((JSONObject) comments.get(0)).get("comment"));
    }

    public void testAddFile() {
        //tested in setUp()
    }

    public void testDelete() {
        //tested in tearDown
    }

    public void testAddToCase() {
        //tested in OeAuthorityFilesServiceImplIT.shouldMoveFileWithCommentsToCase
    }

}
