package dk.openesdh.repo.webscripts.utils;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import dk.openesdh.repo.services.system.MultiLanguageValue;

public class MultiLanguageValueDeserializer extends StdDeserializer<MultiLanguageValue> {

    private MultiLanguageValueDeserializer() {
        super(MultiLanguageValue.class);
    }

    @Override
    public MultiLanguageValue deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Map<String, String> map = jp.readValueAs(Map.class);
        return MultiLanguageValue.createFromMap(map);
    }

    public static SimpleModule getDeserializerModule() {
        Version version = new Version(1, 0, 0, "SNAPSHOT", "dk.openesdh", "multi-language-value-deserializer");
        SimpleModule module = new SimpleModule("MultiLanguageValueDeserializer", version);
        module = module.addDeserializer(MultiLanguageValue.class, new MultiLanguageValueDeserializer());
        return module;
    }
}
