package dk.openesdh.repo.services.classification;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component("ClassifierAdminRegistry")
public class ClassifierAdminRegistry {

    private Map<String, ClassifierAdminService> registry = new HashMap<>();

    public void registerClassifierAdminService(String type, ClassifierAdminService service) {
        registry.put(type, service);
    }

    public ClassifierAdminService getService(String type) {
        return registry.get(type);
    }
}
