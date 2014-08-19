package dk.openesdh.repo;

import static org.junit.Assert.*;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.helper.CaseHelper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by ole on 18/08/14.
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass=SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class OpenESDHModelTest {

    private static final String ADMIN_USER_NAME = "admin";

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("nodeLocatorService")
    protected NodeLocatorService nodeLocatorService;



    @Test
    public void testCreateSimpleCase() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        CaseHelper helper = new CaseHelper();

        NodeRef companyHome = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);

        String name = "My repo case (" + System.currentTimeMillis() + ")";
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, name);
        ChildAssociationRef c = helper.createCase(nodeService, ADMIN_USER_NAME, companyHome, name, OpenESDHModel.TYPE_CASE_SIMPLE, properties);

        NodeRef caseNode = c.getChildRef();
        String caseName = (String) nodeService.getProperty(caseNode, ContentModel.PROP_NAME);
        assertEquals( name, caseName );

    }


}
