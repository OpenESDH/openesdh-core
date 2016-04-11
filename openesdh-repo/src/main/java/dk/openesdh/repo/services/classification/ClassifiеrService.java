package dk.openesdh.repo.services.classification;

import java.util.List;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;

import dk.openesdh.repo.model.ClassifValue;
import dk.openesdh.repo.services.system.MultiLanguageValue;

public interface ClassifiеrService<T extends ClassifValue> {

    /**
     * Retrieves a list of all classifier values
     * 
     * @return
     */
    List<T> getClassifValues();

    /**
     * Retrieves a list of classifier values which are not disabled.
     * 
     * @return
     */
    List<T> getEnabledClassifValues();

    /**
     * Read
     *
     * @param nodeRef
     * @return
     */
    public T getClassifValue(NodeRef nodeRef);

    /**
     * returns display names of all saved locales
     *
     * @param nodeRef
     * @return
     */
    public MultiLanguageValue getMultiLanguageDisplayNames(NodeRef nodeRef);

    /**
     * Retrieves classifiеr value object by name
     * 
     * @param name
     * @return
     */
    Optional<T> getClassifValueByName(String name);

}
