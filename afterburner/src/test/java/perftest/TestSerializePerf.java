package perftest;

import java.io.*;

import tools.jackson.core.json.JsonFactory;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.afterburner.AfterburnerModule;

public class TestSerializePerf
{
    public final static class IntBean
    {
        public int getA() { return 37; }
        public int getBaobab() { return -123; }
        public int getC() { return 0; }
        public int getDonkey() { return 999999; }

        public int getE() { return 1; }
        public int getFoobar() { return 21; }
        public int getG() { return 345; }
        public int getHibernate() { return 99; }
    }
    
    public static void main(String[] args) throws Exception
    {
//        JsonFactory f = new org.codehaus.jackson.smile.SmileFactory();
        JsonFactory f = new JsonFactory();
        ObjectMapper mapperSlow = JsonMapper.builder(f)
        // !!! TEST -- to get profile info, comment out:
                .addModule(new AfterburnerModule())
                .build();

        ObjectMapper mapperFast = JsonMapper.builder(f)
                .addModule(new AfterburnerModule())
                .build();
        new TestSerializePerf().testWith(mapperSlow, mapperFast);
    }

    private void testWith(ObjectMapper slowMapper, ObjectMapper fastMapper)
        throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean fast = true;
        final IntBean bean = new IntBean();
        
        while (true) {
            long now = System.currentTimeMillis();

            ObjectMapper m = fast ? fastMapper : slowMapper;
            int len = 0;
            
            for (int i = 0; i < 399999; ++i) {
                out.reset();
                m.writeValue(out, bean);
                len = out.size();
            }
            long time = System.currentTimeMillis() - now;
            
            System.out.println("Mapper (fast: "+fast+"; "+len+"); took "+time+" msecs");

            fast = !fast;
        }
    }
   
}
