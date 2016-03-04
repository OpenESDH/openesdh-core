package dk.openesdh.repo.services.smtp;

import javax.annotation.PostConstruct;

import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.management.subsystems.SubsystemProxyFactory;
import org.alfresco.service.cmr.repository.TemplateProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Importing mailFreeMarkerProcessor from the OutboundSMTP subsystem to be able
 * to register custom message resolvers.
 * 
 * @author rudinjur
 *
 */
@SuppressWarnings("serial")
@Service(MailFreeMarkerProcessorProxyFactory.BEAN_ID)
public class MailFreeMarkerProcessorProxyFactory extends SubsystemProxyFactory{
    
    public static final String BEAN_ID = "mailFreeMarkerProcessor";

    @PostConstruct
    public void init() {
        setSourceBeanName(BEAN_ID);
        Class[] interfaces = { TemplateProcessor.class };
        setInterfaces(interfaces);
    }

    @Autowired
    @Qualifier("OutboundSMTP")
    @Override
    public void setSourceApplicationContextFactory(ApplicationContextFactory sourceApplicationContextFactory) {
        super.setSourceApplicationContextFactory(sourceApplicationContextFactory);
    }

}
