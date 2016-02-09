package dk.openesdh.repo.classification.sync.kle;

import static org.junit.Assert.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
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
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("CategoryService")
    protected CategoryService categoryService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    private static final String CHANGED_CATEGORY_SUFFIX = "-CHANGED";

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        //classificationSynchronizer.deleteRootCategoryIfExists(KLEClassificationSynchronizer.EmneplanLoader.ROOT_CATEGORY_NAME);
        //classificationSynchronizer.deleteRootCategoryIfExists(KLEClassificationSynchronizer.FacetterLoader.ROOT_CATEGORY_NAME);
    }

    @Test
    public void testSynchronize() throws Exception {
        // Load test XML files
        classificationSynchronizer.synchronizeInternal();

        checkEmneplan(false);
        checkFacetter(false);
    }

    @Test
    public void testSynchronizeChanges() throws Exception {
        classificationSynchronizer.setKleEmneplanURL("classpath:openesdh/classification/kle/test-kle-emneplan-changed.xml");
        classificationSynchronizer.setKleFacetterURL("classpath:openesdh/classification/kle/test-kle-facetter-changed.xml");
        classificationSynchronizer.synchronizeInternal();

        checkEmneplan(true);
        checkFacetter(true);
    }

    private void checkEmneplan(boolean changed) {
        String suffix = changed ? CHANGED_CATEGORY_SUFFIX : "";

        assertTrue("KLE Emneplan Root category is created", classificationSynchronizer.rootCategoryExists(KLEClassificationSynchronizer.EmneplanLoader.ROOT_CATEGORY_NAME));

        // Hovedgruppe
        NodeRef rootCategory = classificationSynchronizer.getOrCreateRootCategory(KLEClassificationSynchronizer.EmneplanLoader.ROOT_CATEGORY_NAME);
        ChildAssociationRef hovedGruppe = categoryService.getCategory(rootCategory, ContentModel.ASPECT_GEN_CLASSIFIABLE, "00");
        assertNotNull("Hovedgruppe is created", hovedGruppe);
        assertEquals("Hovedgruppe title is set correctly", "Kommunens styrelse" + suffix, nodeService.getProperty(hovedGruppe
                .getChildRef(), ContentModel.PROP_TITLE));

        // Gruppe
        ChildAssociationRef gruppe = categoryService.getCategory(hovedGruppe.getChildRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, "00.01");
        assertNotNull("Gruppe is created", gruppe);
        assertEquals("Gruppe title is set correctly", "Kommunens styrelse" + suffix, nodeService.getProperty(gruppe.getChildRef(), ContentModel.PROP_TITLE));

        // Emne
        ChildAssociationRef emne = categoryService.getCategory(gruppe.getChildRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, "00.01.00");
        assertNotNull("Emne is created", emne);
        assertEquals("Emne title is set correctly", "Kommunens styrelse i almindelighed" + suffix, nodeService.getProperty(emne.getChildRef(), ContentModel.PROP_TITLE));
    }

    private void checkFacetter(boolean changed) {
        String suffix = changed ? CHANGED_CATEGORY_SUFFIX : "";

        assertTrue("KLE Facetter Root category is created",
                classificationSynchronizer.rootCategoryExists(KLEClassificationSynchronizer.FacetterLoader.ROOT_CATEGORY_NAME));

        // HandlingsfacetKategori
        NodeRef rootCategoryFacetter = classificationSynchronizer.getOrCreateRootCategory(KLEClassificationSynchronizer.FacetterLoader.ROOT_CATEGORY_NAME);
        ChildAssociationRef facetKategori = categoryService.getCategory(rootCategoryFacetter, ContentModel.ASPECT_GEN_CLASSIFIABLE, "A");
        assertNotNull("HandlingsfacetKategori is created", facetKategori);
        assertEquals("HandlingsfacetKategori title is set correctly", "Organisering mv." + suffix, nodeService.getProperty(facetKategori.getChildRef(), ContentModel.PROP_TITLE));

        // Handlingsfacet
        ChildAssociationRef facet = categoryService.getCategory(facetKategori.getChildRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, "A00");
        assertNotNull("Handlingsfacet is created", facet);
        assertEquals("Handlingsfacet title is set correctly", "Organisering mv. i almindelighed" + suffix, nodeService.getProperty(facet.getChildRef(), ContentModel.PROP_TITLE));
    }

    @After
    public void tearDown() throws Exception {
        //classificationSynchronizer.deleteRootCategoryIfExists(KLEClassificationSynchronizer.EmneplanLoader.ROOT_CATEGORY_NAME);
        //classificationSynchronizer.deleteRootCategoryIfExists(KLEClassificationSynchronizer.FacetterLoader.ROOT_CATEGORY_NAME);
    }
}
