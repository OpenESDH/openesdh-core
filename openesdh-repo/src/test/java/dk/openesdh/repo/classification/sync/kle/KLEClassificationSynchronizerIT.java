package dk.openesdh.repo.classification.sync.kle;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by syastrov on 6/1/15.
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class KLEClassificationSynchronizerIT {

    @Autowired
    @Qualifier("kleClassificationSynchronizer")
    protected KLEClassificationSynchronizer classificationSynchronizer;

    @Autowired
    @Qualifier("CategoryService")
    protected CategoryService categoryService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        classificationSynchronizer.deleteRootCategoryIfExists(KLEClassificationSynchronizer.KLE_EMNEPLAN_ROOT_CATEGORY_NAME);
    }

    @Test
    public void testSynchronize() throws Exception {
        // Load test XML file
        InputStream is = getClass().getClassLoader().getResourceAsStream("openesdh/classification/kle/test-kle-emneplan.xml");
        classificationSynchronizer.loadEmneplanXML(is);

        // TODO: Write better tests
        assertTrue("KLE Emneplan Root category is created",
                classificationSynchronizer.rootCategoryExists(KLEClassificationSynchronizer.KLE_EMNEPLAN_ROOT_CATEGORY_NAME));

        NodeRef rootCategory = classificationSynchronizer.getOrCreateRootCategory(KLEClassificationSynchronizer.KLE_EMNEPLAN_ROOT_CATEGORY_NAME);
        ChildAssociationRef hovedGruppe = categoryService.getCategory
                (rootCategory, ContentModel.ASPECT_GEN_CLASSIFIABLE, "00");
        assertNotNull("Hovedgruppe is created", hovedGruppe);
    }

    @After
    public void tearDown() throws Exception {
        classificationSynchronizer.deleteRootCategoryIfExists(KLEClassificationSynchronizer.KLE_EMNEPLAN_ROOT_CATEGORY_NAME);
    }
}