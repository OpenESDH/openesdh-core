package dk.openesdh.repo.services.cases;

import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by rasmutor on 3/12/15.
 */
@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class SearchDefinitionIT {

    private static final Logger LOG = Logger.getLogger(SearchDefinitionIT.class);

    @Autowired
    @Qualifier("DictionaryService")
    protected DictionaryService dictionaryService;

    private CaseServiceImpl caseService;

    @Before
    public void setUp() throws Exception {
        caseService = new CaseServiceImpl();
        caseService.setDictionaryService(dictionaryService);
    }

    @Test
    public void test1() throws Exception {
        Map<String, Object> result = caseService.getSearchDefinition(OpenESDHModel.TYPE_CASE_BASE);
        if (LOG.isDebugEnabled()) {
            LOG.debug(result);
        }
    }
}
