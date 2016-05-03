package dk.openesdh.repo.webscripts.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dk.openesdh.repo.model.CaseDocsFolder;
import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.model.CaseFolderItem;

public class CaseFolderItemDeserializer extends StdDeserializer<CaseFolderItem> {
    private CaseFolderItemDeserializer() {
        super(CaseFolderItem.class);
    }

    @Override
    public CaseFolderItem deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode obj = mapper.readTree(jp);
        Iterator<String> objFields = obj.fieldNames();
        Iterable<String> iterable = () -> objFields;

        boolean isFolder = StreamSupport.stream(iterable.spliterator(), false)
                .filter(field -> CaseDocsFolder.FIELD_CHILDREN.equals(field)).findAny().isPresent();

        if (isFolder) {
            return mapper.treeToValue(obj, CaseDocsFolder.class);
        }

        return mapper.treeToValue(obj, CaseDocument.class);
    }

    public static SimpleModule getDeserializerModule() {
        Version version = new Version(1, 0, 0, "SNAPSHOT", "dk.openesdh", "case-folder-item-deserializer");
        SimpleModule module = new SimpleModule("CaseFolderItemDeserializer", version);
        module = module.addDeserializer(CaseFolderItem.class, new CaseFolderItemDeserializer());
        return module;
    }
}
