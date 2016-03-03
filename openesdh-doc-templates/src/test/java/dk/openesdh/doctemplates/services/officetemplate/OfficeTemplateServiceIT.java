package dk.openesdh.doctemplates.services.officetemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.doctemplates.api.model.OfficeTemplate;
import dk.openesdh.doctemplates.services.TemplateTester;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class OfficeTemplateServiceIT extends TemplateTester {

    @Test
    public void testSaveTemplate() {
        //created in super.setUp()
        assertNotNull("Template created", savedTemplate);
    }

    @Test
    public void testDeleteTemplate() {
        officeTemplateService.deleteTemplate(savedTemplate);
        try {
            officeTemplateService.getTemplate(savedTemplate);
        } catch (InvalidNodeRefException noNode) {
            savedTemplate = null;
            return;
        }
        fail("Template was not deleted");
    }

    @Test
    public void testGetTemplates() {
        List<OfficeTemplate> templates = officeTemplateService.getTemplates();
        assertEquals("Template is found", 1, templates.size());
    }
}
