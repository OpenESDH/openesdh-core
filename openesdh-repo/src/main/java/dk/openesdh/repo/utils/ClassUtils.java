package dk.openesdh.repo.utils;

import java.lang.reflect.Method;

import org.alfresco.error.AlfrescoRuntimeException;

public class ClassUtils {

    public static void checkHasMethods(Class<?> c, String... methodNames) {
        for (String methodName : methodNames) {
            checkHasMethod(c, methodName);
        }
    }

    private static void checkHasMethod(Class<?> c, String methodName) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(methodName)) {
                return;
            }
        }
        throw new AlfrescoRuntimeException(c.getName() + " class has no method named " + methodName);
    }
}
