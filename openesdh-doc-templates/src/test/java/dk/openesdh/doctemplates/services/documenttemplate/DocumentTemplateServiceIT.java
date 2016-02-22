package dk.openesdh.doctemplates.services.documenttemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
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

import dk.openesdh.doctemplates.model.DocumentTemplateInfo;
import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplateService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class DocumentTemplateServiceIT {

    private static final String TEMPLATE_NAME = "TestTemplate";
    private static final String TEMPLATE_FILE_NAME = "test-template.ott";
    private static final String TEMPLATE_FILE_PATH = "/officetemplates/" + TEMPLATE_FILE_NAME;

    private NodeRef savedTemplate;

    @Autowired
    @Qualifier("DocumentTemplateService")
    private DocumentTemplateService documentTemplateService;
    @Autowired
    @Qualifier("OfficeTemplateService")
    private OfficeTemplateService officeTemplateService;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        InputStream testTemplateInputStream = getClass().getResourceAsStream(TEMPLATE_FILE_PATH);
        savedTemplate = officeTemplateService.saveTemplate(
                TEMPLATE_NAME,
                getClass().getSimpleName(),
                TEMPLATE_FILE_NAME,
                testTemplateInputStream,
                MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_TEMPLATE);
    }

    @After
    public void cleanUp() {
        if (savedTemplate != null) {
            officeTemplateService.deleteTemplate(savedTemplate);
        }
    }

    @Test
    public void testFindTemplates() {
        List<DocumentTemplateInfo> templates = documentTemplateService.findTemplates("Tes", 10);
        assertEquals("Template is found", 1, templates.size());
    }

    @Test
    public void testGetTemplateInfo() {
        DocumentTemplateInfo templateInfo = documentTemplateService.getTemplateInfo(savedTemplate);
        assertNotNull("Template is retrieved", templateInfo);
        assertEquals("Description is the same", getClass().getSimpleName(), templateInfo.getDescription());
    }

}
