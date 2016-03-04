/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.openesdh.office.services;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.office.model.OutlookModel;
import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class OfficeServiceImplIT {

    private NodeRef caseNodeRef;
    private NodeRef docNodeRef;

    @Autowired
    @Qualifier("OfficeService")
    private OfficeService officeService;
    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    private CaseDocumentTestHelper caseTestHelper;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    private TransactionRunner transactionRunner;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
        caseNodeRef = caseTestHelper.createCaseBehaviourOn(
                "caseTypeTest case", caseService.getCasesRootNodeRef(), CaseHelper.ADMIN_USER_NAME);
    }

    @After
    public void cleanUp() {
        caseTestHelper.removeNodesAndDeleteUsersInTransaction(
                Collections.emptyList(),
                caseNodeRef == null ? Collections.emptyList() : Arrays.asList(caseNodeRef),
                Collections.emptyList());
    }

    @Test
    public void testCreateEmailDocument() {
        String caseId = caseService.getCaseId(caseNodeRef);
        docNodeRef = transactionRunner.runInTransaction(() -> {
            return officeService.createEmailDocument(caseId, "Test Email " + new Date().getTime(), "test email content");
        });
        assertTrue("Ducumet is marked 'fromOutlook'", nodeService.getAspects(docNodeRef).contains(OutlookModel.ASPECT_OFFICE_OUTLOOK_RECEIVABLE));
    }

}
