package tools.jackson.module.afterburner;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.core.*;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.afterburner.testutil.NoCheckSubTypeValidator;

public abstract class AfterburnerTestBase extends junit.framework.TestCase
{
    // // // First some "shared" classes from databind's `BaseMapTest`
    
    public enum ABC { A, B, C; }

    protected static class BooleanWrapper {
        public boolean b;

        public BooleanWrapper() { }
        public BooleanWrapper(boolean value) { b = value; }

        public boolean getB() { return b; }
    }

    protected static class IntWrapper {
        public int i;

        public IntWrapper() { }
        public IntWrapper(int value) { i = value; }

        public int getI() { return i; }
    }

    protected static class LongWrapper {
        protected long l;

        public LongWrapper() { }
        public LongWrapper(long value) { l = value; }

        public void setL(long l0) { l = l0; }
        public long getL() { return l; }
    }

    protected static class DoubleWrapper {
        public double d;

        public DoubleWrapper() { }
        public DoubleWrapper(double value) { d = value; }

        public double getD() { return d; }
    }

    // since 2.8
    public static class Point {
        public int x, y;

        protected Point() { } // for deser
        public Point(int x0, int y0) {
            x = x0;
            y = y0;
        }
    
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Point)) {
                return false;
            }
            Point other = (Point) o;
            return (other.x == x) && (other.y == y);
        }

        @Override
        public String toString() {
            return String.format("[x=%d, y=%d]", x, y);
        }
    }

    // 24-Oct-2017, tatu: Added to check for glitches in serialization unrolling
    @JsonPropertyOrder({ "a", "b", "c", "d", "e", "f" })
    protected static class Pojo6 {
        private String b = "foo";
        private double e = 0.25;

        public Pojo6() { }

        public int a = 13;
        public String getB() { return b; }
        public boolean c = true;
        public long d = -13117L;
        public double getE() { return e; };
        public int[] f = new int[] { 1, 2, 3 };

        public void setB(String v) { b = v; }
        public void setE(double v) { e = v; }
    }
    
    /**
     * Sample class from Jackson tutorial ("JacksonInFiveMinutes")
     */
    @JsonPropertyOrder(alphabetic=true)
    protected static class FiveMinuteUser {
        public enum Gender { MALE, FEMALE };
	
	@JsonPropertyOrder(alphabetic=true)
        public static class Name
        {
          private String _first, _last;

          public Name() { }
          public Name(String f, String l) {
              _first = f;
              _last = l;
          }
          
          public String getFirst() { return _first; }
          public String getLast() { return _last; }

          public void setFirst(String s) { _first = s; }
          public void setLast(String s) { _last = s; }

          @Override
          public boolean equals(Object o)
          {
              if (o == this) return true;
              if (o == null || o.getClass() != getClass()) return false;
              Name other = (Name) o;
              return _first.equals(other._first) && _last.equals(other._last); 
          }
        }

        private Gender _gender;
        private Name _name;
        private boolean _isVerified;
        private byte[] _userImage;

        public FiveMinuteUser() { }

        public FiveMinuteUser(String first, String last, boolean verified, Gender g, byte[] data)
        {
            _name = new Name(first, last);
            _isVerified = verified;
            _gender = g;
            _userImage = data;
        }
        
        public Name getName() { return _name; }
        public boolean isVerified() { return _isVerified; }
        public Gender getGender() { return _gender; }
        public byte[] getUserImage() { return _userImage; }

        public void setName(Name n) { _name = n; }
        public void setVerified(boolean b) { _isVerified = b; }
        public void setGender(Gender g) { _gender = g; }
        public void setUserImage(byte[] b) { _userImage = b; }

        @Override
        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            FiveMinuteUser other = (FiveMinuteUser) o;
            if (_isVerified != other._isVerified) return false;
            if (_gender != other._gender) return false; 
            if (!_name.equals(other._name)) return false;
            byte[] otherImage = other._userImage;
            if (otherImage.length != _userImage.length) return false;
            for (int i = 0, len = _userImage.length; i < len; ++i) {
                if (_userImage[i] != otherImage[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    
    protected AfterburnerTestBase() { }

    /*
    /**********************************************************
    /* Factory methods: note, copied from `BaseMapTest`
    /**********************************************************
     */

    private static ObjectMapper SHARED_MAPPER;

    @Deprecated
    protected ObjectMapper objectMapper() {
        if (SHARED_MAPPER == null) {
            SHARED_MAPPER = afterburnerMapperBuilder().build();
        }
        return SHARED_MAPPER;
    }

    // preferred in 3.0:
    protected static ObjectMapper newAfterburnerMapper() {
        return afterburnerMapperBuilder()
                .build();
    }

    // preferred in 3.0:
    protected static JsonMapper.Builder afterburnerMapperBuilder() {
        return JsonMapper.builder()
                .polymorphicTypeValidator(new NoCheckSubTypeValidator())
                .addModule(new AfterburnerModule());
    }
    
    // One withOUT afterburner module
    protected static JsonMapper newVanillaJSONMapper() {
        return new JsonMapper();
    }

    // to deprecate
    protected static JsonMapper newObjectMapper() {
        return mapperBuilder().build();
    }

    // to deprecate
    protected static JsonMapper.Builder mapperBuilder() {
        return afterburnerMapperBuilder();
    }
    
    /*
    /**********************************************************
    /* Helper methods; assertions
    /**********************************************************
     */

    protected void assertToken(JsonToken expToken, JsonToken actToken)
    {
        if (actToken != expToken) {
            fail("Expected token "+expToken+", current token "+actToken);
        }
    }
    
    protected void assertToken(JsonToken expToken, JsonParser p)
    {
        assertToken(expToken, p.currentToken());
    }

    protected void assertType(Object ob, Class<?> expType)
    {
        if (ob == null) {
            fail("Expected an object of type "+expType.getName()+", got null");
        }
        Class<?> cls = ob.getClass();
        if (!expType.isAssignableFrom(cls)) {
            fail("Expected type "+expType.getName()+", got "+cls.getName());
        }
    }
    
    /**
     * Method that gets textual contents of the current token using
     * available methods, and ensures results are consistent, before
     * returning them
     */
    protected String getAndVerifyText(JsonParser p) throws IOException
    {
        // Ok, let's verify other accessors
        int actLen = p.getTextLength();
        char[] ch = p.getTextCharacters();
        String str2 = new String(ch, p.getTextOffset(), actLen);
        String str = p.getText();

        if (str.length() !=  actLen) {
            fail("Internal problem (jp.token == "+p.currentToken()+"): jp.getText().length() ['"+str+"'] == "+str.length()+"; jp.getTextLength() == "+actLen);
        }
        assertEquals("String access via getText(), getTextXxx() must be the same", str, str2);

        return str;
    }

    protected void verifyFieldName(JsonParser p, String expName)
        throws IOException
    {
        assertEquals(expName, p.getText());
        assertEquals(expName, p.currentName());
    }
    
    protected void verifyIntValue(JsonParser jp, long expValue)
        throws IOException
    {
        // First, via textual
        assertEquals(String.valueOf(expValue), jp.getText());
    }
    
    protected void verifyException(Throwable e, String... matches)
    {
        String msg = e.getMessage();
        String lmsg = (msg == null) ? "" : msg.toLowerCase();
        for (String match : matches) {
            String lmatch = match.toLowerCase();
            if (lmsg.indexOf(lmatch) >= 0) {
                return;
            }
        }
        fail("Expected an exception with one of substrings ("+Arrays.asList(matches)+"): got one with message \""+msg+"\"");
    }

    /*
    /**********************************************************
    /* Helper methods, other
    /**********************************************************
     */

    public String quote(String str) {
        return q(str);
    }

    public String q(String str) {
        return '"'+str+'"';
    }

    protected static String aposToQuotes(String json) {
        return a2q(json);
    }

    protected static String a2q(String json) {
        return json.replace("'", "\"");
    }
    
    protected byte[] utf8Bytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
