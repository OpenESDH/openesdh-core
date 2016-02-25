package dk.openesdh.repo.services.tenant;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;

import dk.openesdh.repo.autoproxy.BeanPackageNameAutoProxyCreator;

@SuppressWarnings("serial")
public class TenantModulesSecurityAutoProxyCreator extends BeanPackageNameAutoProxyCreator {

    private String openeModuleId;

    public void setOpeneModuleId(String openeModuleId) {
        this.openeModuleId = openeModuleId;
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName,
            TargetSource customTargetSource) throws BeansException {
        Object[] advicesAndAdvisors = super.getAdvicesAndAdvisorsForBean(beanClass, beanName, customTargetSource);
        if (advicesAndAdvisors == DO_NOT_PROXY) {
            return DO_NOT_PROXY;
        }
        List<Object> advisorsList = new ArrayList<>(Arrays.asList(advicesAndAdvisors));
        advisorsList.add(new DefaultPointcutAdvisor((MethodBeforeAdvice) this::checkTenantModuleEnabled));
        return advisorsList.toArray();
    }

    private void checkTenantModuleEnabled(Method method, Object[] args, Object target) throws Throwable {
        TenantOpeneModulesService tenantModulesService = (TenantOpeneModulesService) this.getBeanFactory()
                .getBean("TenantOpeneModulesService");
        tenantModulesService.checkCurrentTenantModuleEnabled(openeModuleId);
    }

}
