package dk.openesdh.repo.webscripts.authorities;

import org.json.JSONObject;
import static org.junit.Assert.assertEquals;
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
public class GroupsWebScriptTest extends AbstractWebScriptMockTest {

    @Autowired
    private GroupsWebScript script;

    @Test
    public void testGetAuthorities() throws Exception {
        int maxItems = 4;
        int skipCount = 2;
        String content = extractResolution(script.getAuthorities("OE", null, null, null, Boolean.TRUE, skipCount, maxItems));
        JSONObject json = new JSONObject(content);
        JSONObject authorityJson = json.getJSONArray("data").getJSONObject(0);
        assertTrue(authorityJson.has("displayName"));
        assertTrue(authorityJson.has("fullName"));
        assertTrue(authorityJson.has("shortName"));
        assertTrue(authorityJson.has("authorityType"));
        assertTrue(authorityJson.has("url"));

        JSONObject pagingJson = json.getJSONObject("paging");
        assertEquals(maxItems, pagingJson.getInt("maxItems"));
        assertTrue(pagingJson.has("totalItems"));
        assertEquals(skipCount, pagingJson.getInt("skipCount"));
    }

}