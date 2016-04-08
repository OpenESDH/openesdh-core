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
public class DocumentCategoryServiceImplIT {

    private static final String TEST_CATEGORY_NAME_PROOF = "testProof";
    private static final String TEST_CATEGORY_NAME_ANNEX = "testAnnex";
    private ClassifValue documentCategory1;
    private ClassifValue documentCategory2;

    @Autowired
    @Qualifier("DocumentCategoryService")
    private DocumentCategoryServiceImpl documentCategoryService;
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
        safelyDelete(documentCategory1);
        safelyDelete(documentCategory2);
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
    public void documentCategoryCrudExecutesSuccessfully() {
        transactionRunner.runInTransaction(() -> {
            //create
            documentCategory1 = new ClassifValue();
            documentCategory1.setName(TEST_CATEGORY_NAME_ANNEX);
            documentCategory1.setDisplayName(TEST_CATEGORY_NAME_ANNEX + "DN");
            documentCategory1 = documentCategoryService.createOrUpdateClassifValue(documentCategory1, createMLName(TEST_CATEGORY_NAME_ANNEX + "DN"));
            //read
            ClassifValue saved = documentCategoryService.getClassifValue(documentCategory1.getNodeRef());
            assertEquals(documentCategory1.getNodeRef(), saved.getNodeRef());
            assertEquals(TEST_CATEGORY_NAME_ANNEX, saved.getName());
            assertEquals(TEST_CATEGORY_NAME_ANNEX + "DN", saved.getDisplayName());
            //update
            saved.setName(TEST_CATEGORY_NAME_PROOF);
            saved = documentCategoryService.createOrUpdateClassifValue(saved, createMLName(TEST_CATEGORY_NAME_PROOF + "DN"));
            //get by name
            documentCategory2 = documentCategoryService.getClassifValueByName(TEST_CATEGORY_NAME_PROOF)
                    .orElseThrow(AssertionError::new);
            assertEquals(saved.getNodeRef().toString(), documentCategory2.getNodeRef().toString());
            assertEquals(TEST_CATEGORY_NAME_PROOF, saved.getName());
            assertEquals(TEST_CATEGORY_NAME_PROOF + "DN", saved.getDisplayName());
            //readList
            List<ClassifValue> documentCategories = documentCategoryService.getClassifValues();
            assertTrue(documentCategories.size() > 0);
            //delete
            documentCategoryService.deleteClassifValue(saved.getNodeRef());
            saved = documentCategoryService.getClassifValue(saved.getNodeRef());
            assertNull(saved);
            return null;
        });
    }

    @Test
    public void testSystemCategoriesExists() {
        assertTrue(documentCategoryService.getClassifValueByName(OpenESDHModel.DOCUMENT_CATEGORY_ANNEX).isPresent());
    }

    private void safelyDelete(ClassifValue documentCategory) {
        if (documentCategory != null && documentCategory.getNodeRef() != null) {
            try {
                nodeService.deleteNode(documentCategory.getNodeRef());
            } catch (Exception unimportantException) {
            }
        }
    }

}
