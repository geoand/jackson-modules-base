package tools.jackson.module.afterburner.ser;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.module.afterburner.AfterburnerTestBase;

public class TestInterfaceSerialize extends AfterburnerTestBase
{
    public interface Wat
    {
        String getFoo();
    }

    public void testInterfaceSerialize() throws Exception
    {
        ObjectMapper mapper = newAfterburnerMapper();

        Wat wat = new Wat() {
            @Override
            public String getFoo() {
                return "bar";
            }
        };

        // Causes IncompatibleClassChangeError
        assertEquals("{\"foo\":\"bar\"}", mapper.writerFor(Wat.class).writeValueAsString(wat));
    }
}
