package dk.openesdh.repo.audit;

import java.io.Serializable;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.repo.audit.extractor.DataExtractor;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class AbstractAnnotatedDataExtractor extends AbstractDataExtractor {

    @Autowired
    @Qualifier("auditModel.extractorRegistry")
    @Override
    public void setRegistry(NamedObjectRegistry<DataExtractor> registry) {
        super.setRegistry(registry);
    }

    @Override
    public boolean isSupported(Serializable data) {
        return true;
    }
}
