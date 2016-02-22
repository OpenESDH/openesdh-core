package dk.openesdh.doctemplates.services;

import java.io.InputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplateService;

public abstract class TemplateTester {

    private static final String TEMPLATE_NAME = "TestTemplate";
    private static final String TEMPLATE_FILE_NAME = "test-template.ott";
    private static final String TEMPLATE_FILE_PATH = "/officetemplates/" + TEMPLATE_FILE_NAME;

    protected NodeRef savedTemplate;

    @Autowired
    @Qualifier("OfficeTemplateService")
    protected OfficeTemplateService officeTemplateService;

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

}
