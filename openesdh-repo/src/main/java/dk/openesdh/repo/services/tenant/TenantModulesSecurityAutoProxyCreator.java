package dk.openesdh.repo.services.tenant;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;

import dk.openesdh.repo.autoproxy.BeanPackageNameAutoProxyCreator;

@SuppressWarnings("serial")
public class TenantModulesSecurityAutoProxyCreator extends BeanPackageNameAutoProxyCreator {

    @Value(value = "${openesdh.tenant.security}")
    private String tennantSecurityTogle;
    private boolean tennantSecurityOFF;

    @PostConstruct
    public final void initProps() {
        tennantSecurityOFF = "OFF".equalsIgnoreCase(tennantSecurityTogle);
    }

    private String openeModuleId;

    public void setOpeneModuleId(String openeModuleId) {
        this.openeModuleId = openeModuleId;
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName,
            TargetSource customTargetSource) throws BeansException {
        if (tennantSecurityOFF) {
            return DO_NOT_PROXY;
        }
        Object[] advicesAndAdvisors = super.getAdvicesAndAdvisorsForBean(beanClass, beanName, customTargetSource);
        if (advicesAndAdvisors == DO_NOT_PROXY) {
            return DO_NOT_PROXY;
        }
        return ArrayUtils.add(
                advicesAndAdvisors,
                new DefaultPointcutAdvisor((MethodBeforeAdvice) this::checkTenantModuleEnabled));
    }

    private void checkTenantModuleEnabled(Method method, Object[] args, Object target) throws Throwable {
        TenantOpeneModulesService tenantModulesService = (TenantOpeneModulesService) this.getBeanFactory()
                .getBean("TenantOpeneModulesService");
        tenantModulesService.checkCurrentTenantModuleEnabled(openeModuleId);
    }

}
