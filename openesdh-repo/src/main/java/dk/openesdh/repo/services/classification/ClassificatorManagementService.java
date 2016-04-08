package dk.openesdh.repo.services.classification;

import java.util.List;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;

import dk.openesdh.repo.model.ClassifValue;
import dk.openesdh.repo.services.system.MultiLanguageValue;

public interface ClassificatorManagementService {

    /**
     * Retrieves a list of all classifier values
     * 
     * @return
     */
    List<ClassifValue> getClassifValues();

    /**
     * Retrieves a list of classifier values which are not disabled.
     * 
     * @return
     */
    List<ClassifValue> getEnabledClassifValues();

    /**
     * Create/Update
     *
     * @param classifValue
     * @param mlDisplayNames
     * @return
     */
    public ClassifValue createOrUpdateClassifValue(ClassifValue classifValue) throws JSONException;

    /**
     * Create/Update
     *
     * @param documentType
     * @param mlDisplayNames
     * @return
     */
    public ClassifValue createOrUpdateClassifValue(ClassifValue documentType, MultiLanguageValue mlDisplayNames)
            throws JSONException;

    /**
     * Read
     *
     * @param nodeRef
     * @return
     */
    public ClassifValue getClassifValue(NodeRef nodeRef);

    /**
     * Delete
     *
     * @param classifValue
     */
    public void deleteClassifValue(NodeRef classifValueRef);

    /**
     * returns display names of all saved locales
     *
     * @param nodeRef
     * @return
     */
    public MultiLanguageValue getMultiLanguageDisplayNames(NodeRef nodeRef);

    /**
     * Retrieves classificator value object by name
     * 
     * @param name
     * @return
     */
    Optional<ClassifValue> getClassifValueByName(String name);

}
