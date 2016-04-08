/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.openesdh.repo.services.documents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.javascript.NativeObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.model.ClassifValue;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.system.MultiLanguageValue;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml", "classpath:alfresco/extension/openesdh-test-context.xml"})
public class DocumentTypeServiceImplIT {

    private static final String TEST_TYPE_NAME_LETTER = "testLetter";
    private static final String TEST_TYPE_NAME_INVOICE = "testInvoice";
    private ClassifValue documentType1;
    private ClassifValue documentType2;

    @Autowired
    @Qualifier("DocumentTypeService")
    private DocumentTypeServiceImpl documentTypeService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    private TransactionRunner transactionRunner;

    @Before
    public void setUp() {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }

    @After
    public void tearUp() {
        safelyDelete(documentType1);
        safelyDelete(documentType2);
    }

    private MultiLanguageValue createMLName(String name) {
        MultiLanguageValue names = new MultiLanguageValue();
        names.defineProperty(
                I18NUtil.getContentLocale().getLanguage(),
                name,
                NativeObject.PERMANENT);
        return names;
    }

    @Test
    public void documentTypeCrudExecutesSuccessfully() {
        transactionRunner.runInTransaction(() -> {
            //create
            documentType1 = new ClassifValue();
            documentType1.setName(TEST_TYPE_NAME_INVOICE);
            documentType1 = documentTypeService.createOrUpdateClassifValue(documentType1, createMLName(TEST_TYPE_NAME_INVOICE + "DN"));
            //read
            ClassifValue saved = documentTypeService.getClassifValue(documentType1.getNodeRef());
            assertEquals(documentType1.getNodeRef(), saved.getNodeRef());
            assertEquals(TEST_TYPE_NAME_INVOICE, saved.getName());
            assertEquals(TEST_TYPE_NAME_INVOICE + "DN", saved.getDisplayName());
            assertEquals(TEST_TYPE_NAME_INVOICE + "DN", documentTypeService
                    .getMultiLanguageDisplayNames(saved.getNodeRef())
                    .get(I18NUtil.getContentLocale().getLanguage()));
            //update
            saved.setName(TEST_TYPE_NAME_LETTER);
            saved = documentTypeService.createOrUpdateClassifValue(saved, createMLName(TEST_TYPE_NAME_LETTER + "DN"));
            //get by name
            documentType2 = documentTypeService.getClassifValueByName(TEST_TYPE_NAME_LETTER)
                    .orElseThrow(AssertionError::new);
            assertEquals(saved.getNodeRef().toString(), documentType2.getNodeRef().toString());
            assertEquals(TEST_TYPE_NAME_LETTER, saved.getName());
            assertEquals(TEST_TYPE_NAME_LETTER + "DN", saved.getDisplayName());
            assertEquals(TEST_TYPE_NAME_LETTER + "DN", documentTypeService
                    .getMultiLanguageDisplayNames(saved.getNodeRef())
                    .get(I18NUtil.getContentLocale().getLanguage()));
            //readList
            List<ClassifValue> documentTypes = documentTypeService.getClassifValues();
            assertTrue(documentTypes.size() > 0);
            //delete
            documentTypeService.deleteClassifValue(saved.getNodeRef());
            saved = documentTypeService.getClassifValue(saved.getNodeRef());
            assertNull(saved);
            return null;
        });
    }

    @Test
    public void testSystemTypesExists() {
        assertTrue(documentTypeService.getClassifValueByName(OpenESDHModel.DOCUMENT_TYPE_INVOICE).isPresent());
        assertTrue(documentTypeService.getClassifValueByName(OpenESDHModel.DOCUMENT_TYPE_LETTER).isPresent());
    }

    private void safelyDelete(ClassifValue documentType) {
        if (documentType != null && documentType.getNodeRef() != null) {
            try {
                nodeService.deleteNode(documentType.getNodeRef());
            } catch (Exception unimportantException) {
            }
        }
    }
}
