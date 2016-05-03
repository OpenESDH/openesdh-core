package dk.openesdh.repo.webscripts.utils;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import dk.openesdh.repo.webscripts.WebScriptParams;

public class PersonInfoDeserializer extends StdDeserializer<PersonInfo> {

    private PersonInfoDeserializer() {
        super(PersonInfo.class);
    }

    @Override
    public PersonInfo deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Map<String, String> map = jp.readValueAs(Map.class);
        NodeRef nodeRef = new NodeRef(map.get(WebScriptParams.NODE_REF));
        return new PersonInfo(nodeRef, map.get("userName"), map.get("firstName"), map.get("lastName"));
    }

    public static SimpleModule getDeserializerModule() {
        Version version = new Version(1, 0, 0, "SNAPSHOT", "dk.openesdh", "person-info-deserializer");
        SimpleModule module = new SimpleModule("PersonInfoDeserializer", version);
        module = module.addDeserializer(PersonInfo.class, new PersonInfoDeserializer());
        return module;
    }
}