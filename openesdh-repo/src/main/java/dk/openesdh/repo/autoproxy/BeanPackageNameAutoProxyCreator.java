package dk.openesdh.repo.autoproxy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

@SuppressWarnings("serial")
public class BeanPackageNameAutoProxyCreator extends AbstractAutoProxyCreator {

    private List<String> beanPackageNames;

    public void setBeanPackageNames(String... beanPackageNames) {
        Assert.notEmpty(beanPackageNames, "'beanPackageNames' must not be empty");
        this.beanPackageNames = Arrays.asList(beanPackageNames)
                .stream()
                .map(StringUtils::trimWhitespace)
                .collect(Collectors.toList());
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName,
            TargetSource customTargetSource) throws BeansException {
        if (Objects.isNull(beanPackageNames) || beanPackageNames.isEmpty()
                || Objects.isNull(beanClass.getPackage())) {
            return DO_NOT_PROXY;
        }
        String beanPackage = beanClass.getPackage().getName();
        return beanPackageNames.stream()
                .filter(packageToProxy -> PatternMatchUtils.simpleMatch(packageToProxy, beanPackage))
                .map(pack -> PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS)
                .findAny()
                .orElse(DO_NOT_PROXY);
    }

}
