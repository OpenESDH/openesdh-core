package dk.openesdh.repo.services.cases;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
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
import dk.openesdh.repo.model.CasePrintInfo;
import fr.opensagres.xdocreport.core.XDocReportException;
import freemarker.template.TemplateModelException;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class PrintCaseServiceImplIT {

    @Autowired
    private PrintCaseService printCaseService;

    @Autowired
    private CaseService caseService;

    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    protected CaseDocumentTestHelper docTestHelper;

    private static final String TEST_CASE_NAME1 = "TestCase1";

    private NodeRef testFolder;
    private NodeRef testCase1;
    private NodeRef owner;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        owner = caseHelper.createDummyUser();
        testCase1 = caseHelper.createSimpleCase(TEST_CASE_NAME1, AuthenticationUtil.getAdminUserName(), owner);
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        List<NodeRef> folders = Arrays.asList(new NodeRef[] { testFolder });
        List<NodeRef> cases = Arrays.asList(new NodeRef[] { testCase1 });
        List<String> users = Arrays.asList(new String[] { CaseHelper.DEFAULT_USERNAME });
        try {
            docTestHelper.removeNodesAndDeleteUsersInTransaction(folders, cases, users);
        } catch (Exception ignored) {
        }
    }

    @Test
    public void shouldPrintCaseInfoToPdf() throws TemplateModelException, IOException, XDocReportException,
            JSONException {
        String caseId = caseService.getCaseId(testCase1);
        CasePrintInfo printInfo = new CasePrintInfo();
        printInfo.setCaseDetails(true);
        Optional<InputStream> optStream = printCaseService.getCaseInfoPdfToPrint(caseId, printInfo);
        Assert.assertTrue("Should return case details pdf stream", optStream.isPresent());
        optStream.get().close();
    }
}
