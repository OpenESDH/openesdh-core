package dk.openesdh.repo.model;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.ModelLookup;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;

import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
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
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("nodeLocatorService")
    protected NodeLocatorService nodeLocatorService;



    protected ModelLookup modelLookup = new ModelLookup();
    protected String testURI = OpenESDHModel.DOC_URI;
    protected String testType = OpenESDHModel.TYPE_DOC_NAME;




    @Test
    public void testCreateSimpleCase() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
        System.out.println(testURI);
        System.out.println(testType);

        modelLookup.setDictionaryService(dictionaryService);
        TypeDefinition modelType = modelLookup.getTypeDefinition(testURI,testType);


        Map properties = modelLookup.getProperties(modelType);
        JSONObject property = (JSONObject)properties.get( "{http://openesdh.dk/model/openesdh/1.0/}title");
        try {
            assertEquals(property.get("type"),"{http://www.alfresco.org/model/dictionary/1.0}text");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Map associations = modelLookup.getAssociations(modelType);
        System.out.println(associations);

        JSONObject association = (JSONObject)associations.get( "{http://openesdh.dk/model/document/1.0/}main");

        try {
            assertEquals(association.get("isTargetMany"),false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    

}
