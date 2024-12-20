package tools.jackson.module.blackbird.ser;

import java.util.function.ToIntFunction;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.BeanPropertyWriter;

final class IntPropertyWriter
    extends OptimizedBeanPropertyWriter<IntPropertyWriter>
{
    private static final long serialVersionUID = 1L;

    private final int _suppressableInt;
    private final boolean _suppressableIntSet;

    private final ToIntFunction<Object> _acc;

    public IntPropertyWriter(BeanPropertyWriter src, ToIntFunction<Object> acc, ValueSerializer<Object> ser) {
        super(src, ser);
        _acc = acc;

        if (_suppressableValue instanceof Integer) {
            _suppressableInt = (Integer)_suppressableValue;
            _suppressableIntSet = true;
        } else {
            _suppressableInt = 0;
            _suppressableIntSet = false;
        }
    }

    protected IntPropertyWriter(IntPropertyWriter base, PropertyName name) {
        super(base, name);
        _suppressableInt = base._suppressableInt;
        _suppressableIntSet = base._suppressableIntSet;
        _acc = base._acc;
    }

    @Override
    protected BeanPropertyWriter _new(PropertyName newName) {
        return new IntPropertyWriter(this, newName);
    }

    @Override
    public BeanPropertyWriter withSerializer(ValueSerializer<Object> ser) {
        return new IntPropertyWriter(this, _acc, ser);
    }

    /*
    /**********************************************************************
    /* Overrides
    /**********************************************************************
     */

    @Override
    public final void serializeAsProperty(Object bean, JsonGenerator g, SerializationContext ctxt)
        throws Exception
    {
        if (broken) {
            fallbackWriter.serializeAsProperty(bean, g, ctxt);
            return;
        }
        int value;
        try {
            value = _acc.applyAsInt(bean);
        } catch (Throwable t) {
            _handleProblem(bean, g, ctxt, t, false);
            return;
        }
        if (!_suppressableIntSet || _suppressableInt != value) {
            g.writeName(_fastName);
            g.writeNumber(value);
        }
    }

    @Override
    public final void serializeAsElement(Object bean, JsonGenerator g, SerializationContext ctxt)
        throws Exception
    {
        if (broken) {
            fallbackWriter.serializeAsElement(bean, g, ctxt);
            return;
        }
        int value;
        try {
            value = _acc.applyAsInt(bean);
        } catch (Throwable t) {
            _handleProblem(bean, g, ctxt, t, true);
            return;
        }
        if (!_suppressableIntSet || _suppressableInt != value) {
            g.writeNumber(value);
        } else { // important: MUST output a placeholder
            serializeAsOmittedElement(bean, g, ctxt);
        }
    }
}
