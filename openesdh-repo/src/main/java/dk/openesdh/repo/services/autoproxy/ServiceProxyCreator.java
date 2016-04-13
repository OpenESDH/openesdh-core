package dk.openesdh.repo.services.autoproxy;

import javax.annotation.PostConstruct;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.cases.PartyService;
import dk.openesdh.repo.services.documents.DocumentEmailService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.workflow.CaseWorkflowService;

/**
 * Creates proxies for provided services by bean ids.
 * 
 * @author rudinjur
 *
 */
@SuppressWarnings("serial")
@Component
public class ServiceProxyCreator extends BeanNameAutoProxyCreator {

    @PostConstruct
    public void init() {
        setBeanNames(new String[] { 
                CaseService.BEAN_ID, 
                DocumentService.BEAN_ID, 
                PartyService.BEAN_ID,
                DocumentEmailService.BEAN_ID, 
                CaseWorkflowService.NAME 
        });
    }

}
