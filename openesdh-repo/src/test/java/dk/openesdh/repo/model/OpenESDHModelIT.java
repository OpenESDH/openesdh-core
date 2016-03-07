package dk.openesdh.repo.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.simplecase.model.SimpleCaseModel;
import dk.openesdh.repo.helper.CaseHelper;

/**
 * Created by ole on 18/08/14.
 */

/**
 * Created by torben on 19/08/14.
 */
@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class OpenESDHModelIT {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("nodeLocatorService")
    private NodeLocatorService nodeLocatorService;


    @Autowired
    @Qualifier("repositoryHelper")
    private Repository repositoryHelper;

    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testCreateSimpleCase() {
        AuthenticationUtil.setFullyAuthenticatedUser(OpenESDHModel.ADMIN_USER_NAME);

        NodeRef companyHome = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);

        String name = "My repo case (" + System.currentTimeMillis() + ")";
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        List<NodeRef> owners = new LinkedList<>();
        owners.add(repositoryHelper.getPerson());
        NodeRef caseNode = caseHelper.createCase(
                companyHome,
                name, SimpleCaseModel.TYPE_CASE_SIMPLE, properties, owners,
                true);
        String caseName = (String) nodeService.getProperty(caseNode, ContentModel.PROP_NAME);
//        assertEquals(name, caseName);

    }
}

