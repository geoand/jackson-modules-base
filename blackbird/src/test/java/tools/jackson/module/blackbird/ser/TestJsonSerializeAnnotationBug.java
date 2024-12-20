package tools.jackson.module.blackbird.ser;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public class TestJsonSerializeAnnotationBug
    extends tools.jackson.module.blackbird.BlackbirdTestBase
{
    public static class TestObjectWithJsonSerialize {
        @JsonSerialize(using = ToStringSerializer.class)
        private final BigDecimal amount;

        @JsonCreator
        public TestObjectWithJsonSerialize(@JsonProperty("amount") BigDecimal amount) {
            this.amount = amount;
        }

        @JsonSerialize(using = ToStringSerializer.class) @JsonProperty("amount")
        public BigDecimal getAmount() {
            return amount;
        }
    }

    public void testAfterburnerModule() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();

        String value = mapper.writeValueAsString(new TestObjectWithJsonSerialize(new BigDecimal("870.04")));
        assertNotNull(value);
    }
}
