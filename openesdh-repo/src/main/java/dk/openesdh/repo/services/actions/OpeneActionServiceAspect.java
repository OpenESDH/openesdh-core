package dk.openesdh.repo.services.actions;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.alfresco.service.cmr.action.Action;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * Intended for various fixes of alfresco actions (e.g. mail action).
 * 
 * @author rudinjur
 *
 */
@Component("OpeneActionServiceAspect")
public class OpeneActionServiceAspect implements BeanFactoryAware {

    private static final String ACTION_SERVICE = "ActionService";
    
    private Map<Predicate<Action>, Consumer<Action>> beforeInterceptors = new HashMap<>();
    
    public void addBeforeActionInterceptor(Predicate<Action> predicate, Consumer<Action> consumer){
        beforeInterceptors.put(predicate, consumer);
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!beanFactory.containsBean(ACTION_SERVICE)) {
            return;
        }
        Advised proxy = (Advised) beanFactory.getBean(ACTION_SERVICE);
        NameMatchMethodPointcutAdvisor beforeExecuteActionAdvisor = new NameMatchMethodPointcutAdvisor(
                (MethodBeforeAdvice) this::beforeExecuteAction);
        beforeExecuteActionAdvisor.addMethodName("executeAction");
        proxy.addAdvisor(beforeExecuteActionAdvisor);
    }

    public void beforeExecuteAction(Method method, Object[] args, Object target) {
        Action action = (Action) args[0];
        beforeInterceptors.entrySet()
            .stream()
            .filter(interceptor -> interceptor.getKey().test(action))
            .map(entry -> entry.getValue())
            .findAny()
            .ifPresent(interceptor -> interceptor.accept(action));
    }
}
