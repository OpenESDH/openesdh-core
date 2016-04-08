package dk.openesdh.repo.services.classification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.BooleanUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.model.ClassifValue;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.system.MultiLanguagePropertyService;
import dk.openesdh.repo.services.system.MultiLanguageValue;

public abstract class ClassificatorManagementServiceImpl implements ClassificatorManagementService {

    private static final String CANNOT_CHANGE_SYSTEM_NAME = "Can not change name of system object.";

    private static final String CANNOT_DELETE_SYSTEM_OBJECT = "Cannot delete system object.";

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;
    @Autowired
    @Qualifier("MultiLanguagePropertyService")
    protected MultiLanguagePropertyService multiLanguagePropertyService;
    
    @Override
    public List<ClassifValue> getClassifValues() {
        return getClassifValuesStream().collect(Collectors.toList());
    }

    @Override
    public List<ClassifValue> getEnabledClassifValues() {
        return getClassifValuesStream()
                .filter(role -> BooleanUtils.isNotTrue(role.getDisabled()))
                .collect(Collectors.toList());
    }
    
    @Override
    public ClassifValue createOrUpdateClassifValue(ClassifValue classifValue, MultiLanguageValue mlDisplayNames)
            throws JSONException {
        classifValue.setMlDisplayNames(mlDisplayNames);
        return createOrUpdateClassifValue(classifValue);
    }

    @Override
    public ClassifValue createOrUpdateClassifValue(ClassifValue classifValue) throws JSONException {
        MultiLanguageValue mlDisplayNames = classifValue.getMlDisplayNames();
        Map<QName, Serializable> properties = getPropertiesToSave(classifValue);
        classifValue.setDisplayName((String) mlDisplayNames.get(I18NUtil.getContentLocale().getLanguage()));

        if (classifValue.getNodeRef() == null) {
            ChildAssociationRef createdNode = nodeService.createNode(getClassificatorValuesRootFolder(),
                    ContentModel.ASSOC_CONTAINS, getClassifValueAssociationName(), getClassifValueType(),
                    properties);
            classifValue.setNodeRef(createdNode.getChildRef());
            setMLDisplayNames(classifValue, mlDisplayNames);
            return classifValue;
        }

        nodeService.setProperties(classifValue.getNodeRef(), properties);
        setMLDisplayNames(classifValue, mlDisplayNames);
        return classifValue;
    }

    @Override
    public void deleteClassifValue(NodeRef classifValueRef) {
        if (isSystemValue(classifValueRef)) {
            throw new AlfrescoRuntimeException(getCannotDeleteSystemMessage());
        }
        nodeService.deleteNode(classifValueRef);
    }

    @Override
    public ClassifValue getClassifValue(NodeRef nodeRef) {
        try {
            QName nodeType = nodeService.getType(nodeRef);
            if (!nodeType.isMatch(getClassifValueType())) {
                throw new AlfrescoRuntimeException(
                        "Invalid type. Expected: " + getClassifValueType() + ", actual: " + nodeType.toString());
            }
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            return getClassifValue(nodeRef, properties);
        } catch (InvalidNodeRefException none) {
            // node does not exist
            return null;
        }
    }

    @Override
    public Optional<ClassifValue> getClassifValueByName(String name) {
        return getClassifValuesStream().filter(value -> value.getName().equals(name)).findAny();
    }

    @Override
    public MultiLanguageValue getMultiLanguageDisplayNames(NodeRef nodeRef) {
        return multiLanguagePropertyService.getMLValues(nodeRef, OpenESDHModel.PROP_CLASSIF_DISPLAY_NAME);
    }

    protected ClassifValue getClassifValue(NodeRef nodeRef, Map<QName, Serializable> properties) {
        ClassifValue classifValue = newClassifValue();
        classifValue.setNodeRef(nodeRef);
        classifValue.setName((String) properties.get(ContentModel.PROP_NAME));
        classifValue.setDisplayName((String) properties.get(OpenESDHModel.PROP_CLASSIF_DISPLAY_NAME));
        classifValue.setMlDisplayNames(multiLanguagePropertyService.getMLValues(nodeRef, OpenESDHModel.PROP_CLASSIF_DISPLAY_NAME));
        classifValue.setDisabled(BooleanUtils.isTrue((Boolean) properties.get(OpenESDHModel.PROP_CLASSIF_DISABLED)));
        classifValue.setIsSystem(BooleanUtils.isTrue((Boolean) properties.get(OpenESDHModel.PROP_CLASSIF_IS_SYSTEM)));
        return classifValue;
    }

    protected ClassifValue newClassifValue() {
        return new ClassifValue();
    }

    protected Map<QName, Serializable> getPropertiesToSave(ClassifValue classifValue) {
        Map<QName, Serializable> props = getProperties(classifValue.getNodeRef());
        checkSysNameChanged(props, classifValue);
        props.put(ContentModel.PROP_NAME, classifValue.getName());
        props.remove(OpenESDHModel.PROP_CLASSIF_DISPLAY_NAME);

        if (Objects.isNull(classifValue.getDisabled())) {
            props.remove(OpenESDHModel.PROP_CLASSIF_DISABLED);
        } else {
            props.put(OpenESDHModel.PROP_CLASSIF_DISABLED, classifValue.getDisabled());
        }

        return props;
    }

    protected Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException {
        if (nodeRef == null) {
            return new HashMap<>();
        }
        return nodeService.getProperties(nodeRef);
    }

    protected void checkSysNameChanged(Map<QName, Serializable> properties, ClassifValue value) {
        if (isSystemValue(properties)) {
            throwErrorIfSystemNameWasChanged(properties, value);
        } else {
            properties.put(OpenESDHModel.PROP_CLASSIF_IS_SYSTEM, false);
        }
    }

    protected void throwErrorIfSystemNameWasChanged(Map<QName, Serializable> properties, ClassifValue value)
            throws AlfrescoRuntimeException {
        if (!properties.get(ContentModel.PROP_NAME).equals(value.getName())) {
            throw new AlfrescoRuntimeException(getCannotChangeNameMessage());
        }
    }

    protected String getCannotDeleteSystemMessage() {
        return CANNOT_DELETE_SYSTEM_OBJECT;
    }

    protected String getCannotChangeNameMessage() {
        return CANNOT_CHANGE_SYSTEM_NAME;
    }

    protected boolean isSystemValue(Map<QName, Serializable> properties) {
        return isValueTrue(properties.get(OpenESDHModel.PROP_CLASSIF_IS_SYSTEM));
    }

    protected boolean isSystemValue(NodeRef nodeRef) {
        return isValueTrue(nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CLASSIF_IS_SYSTEM));
    }

    protected boolean isValueTrue(Serializable value) {
        return BooleanUtils.isTrue((Boolean) value);
    }
    
    protected void setMLDisplayNames(ClassifValue classifValue, MultiLanguageValue mlDisplayNames) {
        multiLanguagePropertyService.setMLValues(
                classifValue.getNodeRef(),
                OpenESDHModel.PROP_CLASSIF_DISPLAY_NAME,
                mlDisplayNames);
    }
    
    protected Stream<ClassifValue> getClassifValuesStream() {
        return nodeService.getChildAssocs(getClassificatorValuesRootFolder())
                .stream()
                .map(assocItem -> getClassifValue(assocItem.getChildRef()))
                .filter(Objects::nonNull);
    }
    
    protected abstract QName getClassifValueType();

    protected abstract QName getClassifValueAssociationName();

    protected abstract NodeRef getClassificatorValuesRootFolder();
}
