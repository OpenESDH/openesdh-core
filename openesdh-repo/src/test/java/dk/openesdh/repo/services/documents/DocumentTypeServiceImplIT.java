/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.openesdh.repo.services.documents;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml", "classpath:alfresco/extension/openesdh-test-context.xml"})
public class DocumentTypeServiceImplIT {

    @Autowired
    @Qualifier("DocumentTypeService")
    protected DocumentTypeServiceImpl documentTypeService;
    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    private DocumentType documentType1;
    private DocumentType documentType2;

    @Before
    public void setUp() {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }

    @After
    public void tearUp() {
        safelyDelete(documentType1);
        safelyDelete(documentType2);
    }

    @Test
    public void documentTypeCrudExecutesSuccessfully() {
        //create
        documentType1 = new DocumentType();
        documentType1.setName("testInvoice");
        documentType1.setDisplayName("testInvoiceDN");
        documentType1 = documentTypeService.saveDocumentType(documentType1);
        //read
        DocumentType saved = documentTypeService.getDocumentType(documentType1.getNodeRef());
        assertEquals(documentType1.getNodeRef(), saved.getNodeRef());
        assertEquals("testInvoice", saved.getName());
        assertEquals("testInvoiceDN", saved.getDisplayName());
        //update
        saved.setName("testLetter");
        saved.setDisplayName("testLetterDN");
        saved = documentTypeService.saveDocumentType(saved);
        //get by name
        documentType2 = documentTypeService.getDocumentTypeByName("testLetter")
                .orElseThrow(AssertionError::new);
        assertEquals(saved.getNodeRef().toString(), documentType2.getNodeRef().toString());
        assertEquals("testLetter", saved.getName());
        assertEquals("testLetterDN", saved.getDisplayName());
        //readList
        List<DocumentType> documentTypes = documentTypeService.getDocumentTypes();
        assertTrue(documentTypes.size() > 0);
        //delete
        documentTypeService.deleteDocumentType(saved);
        saved = documentTypeService.getDocumentType(saved.getNodeRef());
        assertNull(saved);
    }

    @Test
    public void testSystemTypesExists() {
        assertTrue(documentTypeService.getDocumentTypeByName(OpenESDHModel.DOCUMENT_TYPE_INVOICE).isPresent());
        assertTrue(documentTypeService.getDocumentTypeByName(OpenESDHModel.DOCUMENT_TYPE_LETTER).isPresent());
    }

    private void safelyDelete(DocumentType documentType) {
        if (documentType != null && documentType.getNodeRef() != null) {
            try {
                nodeService.deleteNode(documentType.getNodeRef());
            } catch (Exception unimportantException) {
            }
        }
    }
}
