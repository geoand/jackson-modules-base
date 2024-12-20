package tools.jackson.module.blackbird.deser.filter;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tools.jackson.databind.*;
import tools.jackson.module.blackbird.BlackbirdTestBase;

public class IgnoreCreatorProp1317Test extends BlackbirdTestBase
{
    static class Testing {
        @JsonIgnore
        public String ignore;

        String notIgnore;

        public Testing() {}

        @ConstructorProperties({"ignore", "notIgnore"})
        public Testing(String ignore, String notIgnore) {
            super();
            this.ignore = ignore;
            this.notIgnore = notIgnore;
        }

        public String getIgnore() {
            return ignore;
        }

        public void setIgnore(String ignore) {
            this.ignore = ignore;
        }

        public String getNotIgnore() {
            return notIgnore;
        }

        public void setNotIgnore(String notIgnore) {
            this.notIgnore = notIgnore;
        }
    }

    public void testThatJsonIgnoreWorksWithConstructorProperties() throws Exception {
        final ObjectMapper om = newBlackbirdMapper();
        Testing testing = new Testing("shouldBeIgnored", "notIgnore");
        String json = om.writeValueAsString(testing);
//        System.out.println(json);
        assertFalse(json.contains("shouldBeIgnored"));
    }
}
