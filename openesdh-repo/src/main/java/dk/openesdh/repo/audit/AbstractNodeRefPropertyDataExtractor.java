package dk.openesdh.repo.audit;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Checks if value is NodeRef or Collection<NodeRef> and calls extractor on every single value
 *
 * @author petraarn
 */
public abstract class AbstractNodeRefPropertyDataExtractor extends AbstractAnnotatedDataExtractor {

    abstract protected String extract(NodeRef nodeRef);

    @Override
    public final Serializable extractData(Serializable value) throws Throwable {
        if (value instanceof NodeRef) {
            return extract((NodeRef) value);
        } else if (value instanceof Collection) {
            List values = (List) ((Collection) value)
                    .stream()
                    .filter(NodeRef.class::isInstance)
                    .map(nodeRef -> this.extract((NodeRef) nodeRef))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return values.isEmpty() ? null : (Serializable) values;
        }
        return null;
    }
}
