package dk.openesdh.repo.services.xsearch;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * Created by flemming on 18/08/14.
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass=SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class UserInvolvedSearchServiceImplTest
     {

    private static final String ADMIN_USER_NAME = "admin";

    @Autowired
    @Qualifier("authorityService")
    protected AuthorityService authorityService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    @Autowired
    @Qualifier("SearchService")
    protected SearchService searchService;




    protected UserInvolvedSearchServiceImpl involvedSearchService = new UserInvolvedSearchServiceImpl();


    @Test
    public void testDev() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        involvedSearchService = new UserInvolvedSearchServiceImpl();

        involvedSearchService.setAuthorityService(authorityService);
        System.out.println(repositoryHelper);
        involvedSearchService.setRepositoryHelper(repositoryHelper);
        involvedSearchService.setSearchService(searchService);
        involvedSearchService.getCaseGroupsNodedbid("admin");
        Map<String, String> params = new HashMap();
        params.put("user", "admin");
        params.put("filter", "");
        params.put("baseType", "");

      XResultSet nodes = involvedSearchService.getNodes(params, 0, 100, "", true);

        Iterator i = nodes.getNodeRefs().iterator();
        while (i.hasNext()) {
            NodeRef nodeRef = (NodeRef)i.next();
            System.out.println(nodeRef.toString());
        }



        assertEquals("ok", "ok");




    }


}
