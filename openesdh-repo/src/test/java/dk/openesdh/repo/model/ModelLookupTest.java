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
public class ModelLookupTest {

    private static final String ADMIN_USER_NAME = "admin";

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



    protected ModelLookup modelLookup = new ModelLookup();
    protected String testPrefix = OpenESDHModel.DOC_PREFIX;
    protected String testType = OpenESDHModel.TYPE_SIMPLE_NAME;




    @Test
    public void testCreateSimpleCase() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        modelLookup.setDictionaryService(dictionaryService);
        modelLookup.setNamespaceService(namespaceService);
        TypeDefinition modelType = modelLookup.getTypeDefinition(testPrefix
                        + ":" + testType);


        Map properties = modelLookup.getProperties(modelType);
        JSONObject property = (JSONObject)properties.get("oe:id");
        try {
            assertEquals(property.get("type"),"d:text");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Map associations = modelLookup.getAssociations(modelType);
        System.out.println(associations);

        JSONObject association = (JSONObject)associations.get("doc:main");

        try {
            assertEquals(association.get("isTargetMany"),false);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    

}
