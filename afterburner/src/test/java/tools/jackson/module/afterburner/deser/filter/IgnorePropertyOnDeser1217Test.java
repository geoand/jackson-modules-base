package tools.jackson.module.afterburner.deser.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.module.afterburner.AfterburnerTestBase;

public class IgnorePropertyOnDeser1217Test extends AfterburnerTestBase
{
    static class IgnoreObject {
        public int x = 1;
        public int y = 2;
    }

    final static class TestIgnoreObject {
        @JsonIgnoreProperties({ "x" })
        public IgnoreObject obj;

        @JsonIgnoreProperties({ "y" })
        public IgnoreObject obj2;
    }

    /*
    /****************************************************************
    /* Unit tests
    /****************************************************************
     */

    private final ObjectMapper MAPPER = newAfterburnerMapper();

    public void testIgnoreOnProperty() throws Exception
    {
        TestIgnoreObject result = MAPPER.readValue(
                aposToQuotes("{'obj':{'x': 10, 'y': 20}, 'obj2':{'x': 10, 'y': 20}}"),
                TestIgnoreObject.class);
        assertEquals(20, result.obj.y);
        assertEquals(10, result.obj2.x);

        assertEquals(1, result.obj.x);
        assertEquals(2, result.obj2.y);
        
        TestIgnoreObject result1 = MAPPER.readValue(
                  aposToQuotes("{'obj':{'x': 20, 'y': 30}, 'obj2':{'x': 20, 'y': 40}}"),
                  TestIgnoreObject.class);
        assertEquals(1, result1.obj.x);
        assertEquals(30, result1.obj.y);
       
        assertEquals(20, result1.obj2.x);
        assertEquals(2, result1.obj2.y);
    }

    public void testIgnoreViaConfigOverride() throws Exception
    {
        ObjectMapper mapper = afterburnerMapperBuilder()
                .withConfigOverride(Point.class,
                        o -> o.setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("y")))
                .build();
        Point p = mapper.readValue(aposToQuotes("{'x':1,'y':2}"), Point.class);
        // bind 'x', but ignore 'y'
        assertEquals(1, p.x);
        assertEquals(0, p.y);
    }
}
