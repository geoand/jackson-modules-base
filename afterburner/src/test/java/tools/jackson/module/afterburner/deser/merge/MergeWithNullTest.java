package tools.jackson.module.afterburner.deser.merge;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.module.afterburner.AfterburnerTestBase;

public class MergeWithNullTest extends AfterburnerTestBase
{
    static class ConfigDefault {
        @JsonMerge
        public AB loc = new AB(1, 2);

        protected ConfigDefault() { }
        public ConfigDefault(int a, int b) {
            loc = new AB(a, b);
        }
    }

    static class ConfigSkipNull {
        @JsonMerge
        @JsonSetter(nulls=Nulls.SKIP)
        public AB loc = new AB(1, 2);

        protected ConfigSkipNull() { }
        public ConfigSkipNull(int a, int b) {
            loc = new AB(a, b);
        }
    }

    static class ConfigAllowNullOverwrite {
        @JsonMerge
        @JsonSetter(nulls=Nulls.SET)
        public AB loc = new AB(1, 2);

        protected ConfigAllowNullOverwrite() { }
        public ConfigAllowNullOverwrite(int a, int b) {
            loc = new AB(a, b);
        }
    }
    
    // another variant where all we got is a getter
    static class NoSetterConfig {
        AB _value = new AB(2, 3);

        @JsonMerge
        public AB getValue() { return _value; }
    }

    static class AB {
        public int a;
        public int b;

        protected AB() { }
        public AB(int a0, int b0) {
            a = a0;
            b = b0;
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final ObjectMapper MAPPER = afterburnerMapperBuilder()
            // 26-Oct-2016, tatu: Make sure we'll report merge problems by default
            .disable(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE)
            .build();

    public void testBeanMergingWithNullDefault() throws Exception
    {
        // By default `null` should simply overwrite value
        ConfigDefault config = MAPPER.readerForUpdating(new ConfigDefault(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNull(config.loc);

        // but it should be possible to override setting to, say, skip

        // First: via specific type override
        // important! We'll specify for value type to be merged
        ObjectMapper mapper = afterburnerMapperBuilder()
                .withConfigOverride(AB.class,
                        o -> o.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.SKIP)))
                .build();
        config = mapper.readerForUpdating(new ConfigDefault(137, -3))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config.loc);
        assertEquals(137, config.loc.a);
        assertEquals(-3, config.loc.b);

        // Second: by global defaults
        mapper = afterburnerMapperBuilder()
                .changeDefaultNullHandling(n -> n.withValueNulls(Nulls.SKIP))
                .build();
        config = mapper.readerForUpdating(new ConfigDefault(12, 34))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config.loc);
        assertEquals(12, config.loc.a);
        assertEquals(34, config.loc.b);
    }

    public void testBeanMergingWithNullSkip() throws Exception
    {
        ConfigSkipNull config = MAPPER.readerForUpdating(new ConfigSkipNull(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNotNull(config.loc);
        assertEquals(5, config.loc.a);
        assertEquals(7, config.loc.b);
    }

    public void testBeanMergingWithNullSet() throws Exception
    {
        ConfigAllowNullOverwrite config = MAPPER.readerForUpdating(new ConfigAllowNullOverwrite(5, 7))
                .readValue(aposToQuotes("{'loc':null}"));
        assertNotNull(config);
        assertNull(config.loc);
    }
    
    public void testSetterlessMergingWithNull() throws Exception
    {
        NoSetterConfig input = new NoSetterConfig();
        NoSetterConfig result = MAPPER.readerForUpdating(input)
                .readValue(aposToQuotes("{'value':null}"));
        assertNotNull(result.getValue());
        assertEquals(2, result.getValue().a);
        assertEquals(3, result.getValue().b);
        assertSame(input, result);
    }
}
