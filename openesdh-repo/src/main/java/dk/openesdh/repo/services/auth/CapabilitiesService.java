package dk.openesdh.repo.services.auth;

import java.util.Map;
import java.util.function.Supplier;

public interface CapabilitiesService {
    String BEAN_ID = "CapabilitiesService";

    /**
     * Retrieves capabilities for the current authentication.
     * 
     * @return
     */
    Map<String, Boolean> getCapabilities();

    /**
     * Registers capability provider.
     * 
     * @param capability
     * @param provider
     */
    void registerCapabilityProvider(String capability, Supplier<Boolean> provider);

    /**
     * Registers capabilities provider.
     * 
     * @param provider
     */
    void registerCapabilitiesProvider(Supplier<Map<String, Boolean>> provider);
}
