package tools.jackson.module.androidrecord.failing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.module.androidrecord.BaseMapTest;

import com.android.tools.r8.RecordTag;

public class RecordCreatorsFailingTest extends BaseMapTest {
  static final class RecordWithAltCtor extends RecordTag {
    private final int id;
    private final String name;

    RecordWithAltCtor(int id, String name) {
      this.id = id;
      this.name = name;
    }

    public int id() {
      return id;
    }

    public String name() {
      return name;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RecordWithAltCtor(@JsonProperty("id") int id) {
      this(id, "name2");
    }
  }

  private final ObjectMapper MAPPER = newJsonMapper();

  /*
  /**********************************************************************
  /* Test methods, alternate constructors
  /**********************************************************************
  */

  // Fails: Implicit canonical constructor still works too
  public void testDeserializeWithAltCtor() throws Exception {
    RecordWithAltCtor value = MAPPER.readValue("{\"id\":2812}",
            RecordWithAltCtor.class);
    assertEquals(2812, value.id());
    assertEquals("name2", value.name());

    // "Implicit" canonical constructor can no longer be used when there's explicit constructor
    try {
      MAPPER.readValue("{\"id\":2812,\"name\":\"Bob\"}",
              RecordWithAltCtor.class);
      fail("should not pass");
    } catch (UnrecognizedPropertyException e) {
      verifyException(e, "Unrecognized");
      verifyException(e, "\"name\"");
      verifyException(e, "RecordWithAltCtor");
    }
  }
}
