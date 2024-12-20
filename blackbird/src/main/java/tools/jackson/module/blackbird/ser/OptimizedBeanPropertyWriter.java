package tools.jackson.module.blackbird.ser;

import java.util.logging.Level;
import java.util.logging.Logger;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.SerializableString;

import tools.jackson.databind.*;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.BeanPropertyWriter;

/**
 * Intermediate base class that is used for concrete
 * per-type implementations
 */
@SuppressWarnings("serial")
abstract class OptimizedBeanPropertyWriter<T extends OptimizedBeanPropertyWriter<T>>
    extends BeanPropertyWriter
{
    /**
     * Locally stored version of efficiently serializable name.
     * Used to work around earlier problems with typing between
     * interface, implementation
     */
    protected final SerializableString _fastName;

    protected final BeanPropertyWriter fallbackWriter;
    // Not volatile to prevent overhead, worst case is we trip the exception a few extra times
    protected boolean broken = false;

    protected OptimizedBeanPropertyWriter(BeanPropertyWriter src, ValueSerializer<Object> ser)
    {
        super(src);
        this.fallbackWriter = unwrapFallbackWriter(src);
        // either use the passed on serializer or the original one
        _serializer = (ser != null) ? ser : src.getSerializer();
        _fastName = src.getSerializedName();
    }

    protected OptimizedBeanPropertyWriter(OptimizedBeanPropertyWriter<?> base, PropertyName name) {
        super(base, name);
        // 24-Oct-2017, tatu: Should this be changed too?
        fallbackWriter = base.fallbackWriter;
        _serializer = base._serializer;
        _fastName = getSerializedName();
    }

    private BeanPropertyWriter unwrapFallbackWriter(BeanPropertyWriter srcIn)
    {
        while (srcIn instanceof OptimizedBeanPropertyWriter) {
            srcIn = ((OptimizedBeanPropertyWriter<?>)srcIn).fallbackWriter;
        }
        return srcIn;
    }

    // Ensure it gets defined
    @Override
    protected abstract BeanPropertyWriter _new(PropertyName newName);

    @Override
    public void assignTypeSerializer(TypeSerializer typeSer) {
        super.assignTypeSerializer(typeSer);
        if (fallbackWriter != null) {
            fallbackWriter.assignTypeSerializer(typeSer);
        }
        // 04-Oct-2015, tatu: Should we handle this wrt [module-afterburner#59]?
        //    Seems unlikely, as String/long/int/boolean are final types; and for
        //    basic 'Object' we delegate to deserializer as expected
    }

    @Override
    public void assignSerializer(ValueSerializer<Object> ser) {
        super.assignSerializer(ser);
        if (fallbackWriter != null) {
            fallbackWriter.assignSerializer(ser);
        }
        // 04-Oct-2015, tatu: To fix [module-afterburner#59], need to disable use of
        //    fully optimized variant
        if (!SerializerUtil.isDefaultSerializer(ser)) {
            broken = true;
        }
    }

    @Override
    public void assignNullSerializer(ValueSerializer<Object> nullSer) {
        super.assignNullSerializer(nullSer);
        if (fallbackWriter != null) {
            fallbackWriter.assignNullSerializer(nullSer);
        }
    }

    public abstract BeanPropertyWriter withSerializer(ValueSerializer<Object> ser);

    @Override
    public abstract void serializeAsProperty(Object bean, JsonGenerator g, SerializationContext ctxt) throws Exception;

    @Override
    public abstract void serializeAsElement(Object bean, JsonGenerator g, SerializationContext ctxt) throws Exception;

    // note: synchronized used to try to minimize race conditions; also, should NOT
    // be a performance problem
    protected synchronized void _handleProblem(Object bean, JsonGenerator g, SerializationContext ctxt,
            Throwable t, boolean element) throws Exception
    {
        if ((t instanceof IllegalAccessError)
                || (t instanceof SecurityException)) {
            _reportProblem(bean, t);
            if (element) {
                fallbackWriter.serializeAsElement(bean, g, ctxt);
            } else {
                fallbackWriter.serializeAsProperty(bean, g, ctxt);
            }
            return;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        throw (Exception) t;
    }

    protected void _reportProblem(Object bean, Throwable e)
    {
        broken = true;
        String msg = String.format("Disabling Blackbird serialization for %s (field %s; mutator %s), due to access error (type %s, message=%s)%n",
                bean.getClass(), _name, getClass().getName(),
                e.getClass().getName(), e.getMessage());
        Logger.getLogger(OptimizedBeanPropertyWriter.class.getName()).log(Level.WARNING, msg, e);
    }
}
