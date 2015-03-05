package dk.openesdh.repo.model;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * Created by flemming on 18/08/14.
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass=SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
//@Ignore
public class ModelLookupIT {

    @Autowired
    @Qualifier("dictionaryService")
    protected DictionaryService dictionaryService;

    @Autowired
    @Qualifier("namespaceService")
    protected NamespaceService namespaceService;

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("nodeLocatorService")
    protected NodeLocatorService nodeLocatorService;



    protected ModelLookup modelLookup;
    protected String testPrefix = OpenESDHModel.DOC_PREFIX;
    protected String testType = OpenESDHModel.TYPE_SIMPLE_NAME;

    @Before
    public void setUp() {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        modelLookup = new ModelLookup();
        modelLookup.setDictionaryService(dictionaryService);
        modelLookup.setNamespaceService(namespaceService);
    }


    @Test
    public void testCreateSimpleCase() throws Exception {

        TypeDefinition modelType = modelLookup.getTypeDefinition(testPrefix + ":" + testType);

        Map properties = modelLookup.getProperties(modelType);
        JSONObject property = (JSONObject)properties.get("oe:id");
        assertEquals(property.get("type"),"d:text");

        Map associations = modelLookup.getAssociations(modelType);

        JSONObject association = (JSONObject)associations.get("doc:owner");

        assertEquals(association.get("isTargetMany"),false);
    }
    

}
