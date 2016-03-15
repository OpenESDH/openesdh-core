package dk.openesdh.repo.services.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service(CapabilitiesService.BEAN_ID)
public class CapabilitiesServiceImpl implements CapabilitiesService {

    private Map<String, Supplier<Boolean>> capabilityProviders = new HashMap<>();
    
    private List<Supplier<Map<String, Boolean>>> capabilitiesProviders = new ArrayList<>(); 

    @Override
    public void registerCapabilityProvider(String capability, Supplier<Boolean> provider) {
        capabilityProviders.put(capability, provider);
    }
    
    @Override
    public void registerCapabilitiesProvider(Supplier<Map<String, Boolean>> provider) {
        capabilitiesProviders.add(provider);
    }

    @Override
    public Map<String, Boolean> getCapabilities() {
        Map<String, Boolean> capabilities = capabilityProviders.entrySet()
            .stream()
            .collect(
                Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().get()));
        
        capabilitiesProviders.stream()
            .map(Supplier::get)
            .forEach(map -> capabilities.putAll(map));
        return capabilities;
    }

}
