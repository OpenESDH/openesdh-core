package dk.openesdh.repo.services.cases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.simplecase.model.SimpleCaseModel;
import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class CaseTypeServiceImplIT {

    @Autowired
    @Qualifier("CaseTypeService")
    private CaseTypeService caseTypeService;
    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    private CaseDocumentTestHelper caseTestHelper;
    @Autowired
    @Qualifier("CaseService")
    private CaseServiceImpl caseService;

    @Test
    public void testGetCaseType_NodeRef() {
        doWithCreatedCase(caseNodeRef -> {
            assertEquals("simple:case", caseTypeService.getCaseType(caseNodeRef));
        });
    }

    @Test
    public void testGetCaseType_QName() {
        assertEquals("simple:case", caseTypeService.getCaseType(SimpleCaseModel.TYPE_CASE_SIMPLE));
    }

    @Test
    public void testGetCaseTypeTitle_NodeRef() {
        doWithCreatedCase(caseNodeRef -> {
            assertEquals(
                    I18NUtil.getMessage("case.type.simple_case.title"),
                    caseTypeService.getCaseTypeTitle(caseNodeRef));
        });
    }

    @Test
    public void testGetCaseTypeTitle_QName() {
        assertEquals(
                I18NUtil.getMessage("case.type.simple_case.title"),
                caseTypeService.getCaseTypeTitle(SimpleCaseModel.TYPE_CASE_SIMPLE));
    }

    private interface WithCaseExecutor {

        void execute(NodeRef caseNodeRef);
    }

    private void doWithCreatedCase(WithCaseExecutor action) {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
        NodeRef caseNodeRef = null;
        try {
            caseNodeRef = caseTestHelper.createCaseBehaviourOn(
                    "caseTypeTest case", caseService.getCasesRootNodeRef(), CaseHelper.ADMIN_USER_NAME);
            action.execute(caseNodeRef);
        } finally {
            //cleanup
            caseTestHelper.removeNodesAndDeleteUsersInTransaction(
                    Collections.emptyList(),
                    caseNodeRef == null ? Collections.emptyList() : Arrays.asList(caseNodeRef),
                    Collections.emptyList());
        }
    }

}
