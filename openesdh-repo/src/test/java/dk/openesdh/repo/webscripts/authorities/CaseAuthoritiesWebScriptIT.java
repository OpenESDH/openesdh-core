package dk.openesdh.repo.webscripts.authorities;

import org.json.JSONObject;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.webscripts.utils.AbstractWebScriptMockTest;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class CaseAuthoritiesWebScriptIT extends AbstractWebScriptMockTest {

    @Autowired
    private CaseAuthoritiesWebScript script;

    @Test
    public void testGetCaseAuthorities() throws Exception {
        JSONObject json = new JSONObject(extractResolution(script.getCaseAuthorities("OE", null)));
        JSONObject authorityJson = json.getJSONObject("data").getJSONArray("items").getJSONObject(0);
        assertTrue(authorityJson.has("nodeRef"));
        assertTrue(authorityJson.has("selectable"));
        assertTrue(authorityJson.has("name"));
        assertTrue(authorityJson.has("isContainer"));
        assertTrue(authorityJson.has("description"));
        assertTrue(authorityJson.has("type"));
        assertTrue(authorityJson.has("shortName"));
        assertTrue(authorityJson.has("title"));
        assertTrue(authorityJson.has("parentType"));

    }

}
