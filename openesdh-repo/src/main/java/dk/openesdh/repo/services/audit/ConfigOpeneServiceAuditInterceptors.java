package dk.openesdh.repo.services.audit;

import java.util.Arrays;
import java.util.List;

import org.alfresco.repo.audit.AuditMethodInterceptor;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.cases.PartyService;
import dk.openesdh.repo.services.documents.DocumentEmailService;

@Component
public class ConfigOpeneServiceAuditInterceptors implements BeanFactoryAware {

    @Autowired
    @Qualifier("AuditMethodInterceptor")
    private AuditMethodInterceptor auditInterceptor;

    private List<String> auditableBeans = Arrays.asList(
            PartyService.BEAN_ID, 
            DocumentEmailService.BEAN_ID
    );

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!beanFactory.containsBean(PartyService.BEAN_ID)) {
            return;
        }
        for (String beanId : auditableBeans) {
            Advised partyService = (Advised) beanFactory.getBean(beanId);
            partyService.addAdvice(auditInterceptor);
        }
    }
}
