package tools.jackson.module.afterburner.format;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import tools.jackson.databind.*;
import tools.jackson.module.afterburner.AfterburnerTestBase;

public class ColletionFormatShapeTest extends AfterburnerTestBase
{
    // [databind#40]: Allow serialization 'as POJO' (resulting in JSON Object) 
    @JsonPropertyOrder({ "size", "value" })
    @JsonFormat(shape=Shape.POJO)
    @JsonIgnoreProperties({
        "empty", // from 'isEmpty()'
        "first", "last" // JDK 21 additions
    })
    static class CollectionAsPOJO
        extends ArrayList<String>
    {
        private static final long serialVersionUID = 1L;

        @JsonProperty("size")
        public int foo() { return size(); }
        
        public List<String> getValues() {
            return new ArrayList<String>(this);
        }

        public void setValues(List<String> v) {
            addAll(v);
        }
        
        // bogus setter to handle "size" property
        public void setSize(int i) { }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final static ObjectMapper MAPPER = newAfterburnerMapper();    

    public void testListAsObjectRoundtrip() throws Exception
    {
        // First, serialize a "POJO-List"
        CollectionAsPOJO list = new CollectionAsPOJO();
        list.add("a");
        list.add("b");
        String json = MAPPER.writeValueAsString(list);

        // 2023-10-17, tatu: JDK 21 introduced new properties, so check
        //  just that we have certain things, ignore extra
        JsonNode doc = MAPPER.readTree(json);
        //assertEquals("{\"size\":2,\"values\":[\"a\",\"b\"]}", json);

        assertEquals(2, doc.path("size").intValue());
        assertEquals("[\"a\",\"b\"]", doc.path("values").toString());

        // and then bring it back!
        CollectionAsPOJO result = MAPPER.readValue(json, CollectionAsPOJO.class);
        assertEquals(2, result.size());
    }
}
