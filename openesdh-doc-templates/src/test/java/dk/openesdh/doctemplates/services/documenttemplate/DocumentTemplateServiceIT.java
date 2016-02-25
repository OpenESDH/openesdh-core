package dk.openesdh.doctemplates.services.documenttemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.doctemplates.model.DocumentTemplateInfo;
import dk.openesdh.doctemplates.services.TemplateTester;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class DocumentTemplateServiceIT extends TemplateTester {

    @Autowired
    @Qualifier("DocumentTemplateService")
    private DocumentTemplateService documentTemplateService;

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
