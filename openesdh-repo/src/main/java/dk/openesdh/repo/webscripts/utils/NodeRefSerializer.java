package dk.openesdh.repo.webscripts.utils;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class NodeRefSerializer extends StdSerializer<NodeRef> {

    private NodeRefSerializer() {
        super(NodeRef.class);
    }

    @Override
    public void serialize(NodeRef value, com.fasterxml.jackson.core.JsonGenerator jgen,
            com.fasterxml.jackson.databind.SerializerProvider provider)
                    throws IOException, JsonProcessingException {
        jgen.writeString(value.toString());
    }

    public static SimpleModule getSerializerModule() {
        Version version = new Version(1, 0, 0, "SNAPSHOT", "dk.openesdh", "node-ref-serializer");
        SimpleModule module = new SimpleModule("NodeRefSerializer", version);
        module = module.addSerializer(new NodeRefSerializer());
        return module;
    }
}
