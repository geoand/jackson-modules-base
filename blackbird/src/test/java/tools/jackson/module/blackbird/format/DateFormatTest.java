package tools.jackson.module.blackbird.format;

import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.*;
import tools.jackson.module.blackbird.BlackbirdTestBase;

public class DateFormatTest extends BlackbirdTestBase
{
    protected static class DateWrapper {
        public Date value;

        public DateWrapper() { }
        public DateWrapper(long l) { value = new Date(l); }
        public DateWrapper(Date v) { value = v; }
    }

    public void testTypeDefaults() throws Exception
    {
        ObjectMapper mapper = mapperBuilder()
                .withConfigOverride(Date.class,
                        o -> o.setFormat(JsonFormat.Value.forPattern("yyyy.dd.MM")))
                .build();
        // First serialize, should result in this (in UTC):
        String json = mapper.writeValueAsString(new DateWrapper(0L));
        assertEquals(aposToQuotes("{'value':'1970.01.01'}"), json);

        // and then read back
        DateWrapper w = mapper.readValue(aposToQuotes("{'value':'1981.13.3'}"), DateWrapper.class);
        assertNotNull(w);
        // arbitrary TimeZone, but good enough to ensure year is right
        Calendar c = Calendar.getInstance();
        c.setTime(w.value);
        assertEquals(1981, c.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, c.get(Calendar.MONTH));
    }
}
