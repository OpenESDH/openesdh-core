package dk.openesdh.repo.audit;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:alfresco/application-context.xml",
        "classpath:alfresco/extension/openesdh-test-context.xml" })
public class CaseNodeRefExtractorIT {

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    protected CaseDocumentTestHelper docTestHelper;

    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    protected CaseService caseService;

    @Autowired
    private CaseNodeRefExtractor extractor;

    private static final String TEST_FOLDER_NAME = "DocumentServiceImpIT";
    private static final String TEST_CASE_NAME1 = "TestCase1";

    private NodeRef testFolder;
    private NodeRef testCase1;

    @Before
    public void setUp() throws Exception {

        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        testFolder = docTestHelper.createFolder(TEST_FOLDER_NAME);
        testCase1 = docTestHelper.createCaseBehaviourOn(TEST_CASE_NAME1, testFolder, CaseHelper.DEFAULT_USERNAME);
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        List<NodeRef> folders = Arrays.asList(new NodeRef[] { testFolder });
        List<NodeRef> cases = Arrays.asList(new NodeRef[] { testCase1 });
        List<String> users = Arrays.asList(new String[] { CaseHelper.DEFAULT_USERNAME });
        docTestHelper.removeNodesAndDeleteUsersInTransaction(folders, cases, users);
    }

    /**
     * Check extractor to retrieve case id from a path of the following format:
     *      /app:company_home/oe:OpenESDH/oe:cases/case:2015/case:6/case:12/case:20150612-860 
     */
    @Test
    public void shouldRetrieveCaseIdFromPathEndingWithCaseId() {
        String caseId = caseService.getCaseId(testCase1);
        String casePath = createPathOfTestCase1();
        String resultCaseId = extractor.getCaseIdFromPath(casePath);
        Assert.assertEquals("Should extract case id from path ", caseId, resultCaseId);
    }

    /**
     * Check extractor to retrieve case id from a path of the following format:
     *      /app:company_home/oe:OpenESDH/oe:cases/case:2015/case:6/case:12/case:20150612-860/cm:note-1434092920935 
     */
    @Test
    public void shouldRetrieveCaseIdFromPathEndingWithNoteId() {
        String caseId = caseService.getCaseId(testCase1);
        String casePath = createPathOfTestCase1WithNoteId();
        String resultCaseId = extractor.getCaseIdFromPath(casePath);
        Assert.assertEquals("Should extract case id from path ", caseId, resultCaseId);
    }

    @Test
    public void shouldRetrieveCaseNodeRefByCaseIdFromPath() {
        String casePath = createPathOfTestCase1();
        String resultNodeRef = extractor.getNodeRefFromPath(casePath);
        Assert.assertEquals("Should extract case node ref from path ", testCase1.toString(), resultNodeRef);
    }

    private String createPathOfTestCase1WithNoteId() {
        return createPathOfTestCase1() + "cm:note-1434092920935";
    }

    private String createPathOfTestCase1() {
        String casesRootFolder = OpenESDHFoldersService.CASES_ROOT_PATH;
        String caseId = caseService.getCaseId(testCase1);
        Calendar c = Calendar.getInstance();
        String casePath = String.join("/case:", 
                casesRootFolder, 
                Integer.toString(c.get(Calendar.YEAR)),
                Integer.toString(c.get(Calendar.MONTH) + 1),
                Integer.toString(c.get(Calendar.DAY_OF_MONTH)), 
                caseId
        );
        return casePath;
    }
}
