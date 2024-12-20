package tools.jackson.module.blackbird.deser;

import java.util.function.BiConsumer;

import tools.jackson.core.*;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.SettableBeanProperty;

final class SettableObjectProperty
    extends OptimizedSettableBeanProperty<SettableObjectProperty>
{
    private static final long serialVersionUID = 1L;
    private BiConsumer<Object, Object> _optimizedSetter;

    public SettableObjectProperty(SettableBeanProperty src, BiConsumer<Object, Object> optimizedSetter)
    {
        super(src);
        this._optimizedSetter = optimizedSetter;
    }

    @Override
    protected SettableBeanProperty withDelegate(SettableBeanProperty del) {
        return new SettableObjectProperty(del, _optimizedSetter);
    }

    /*
    /**********************************************************************
    /* Deserialization
    /**********************************************************************
     */

    @Override
    public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object bean)
        throws JacksonException
    {
        Object value;
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            if (_skipNulls) {
                return;
            }
            value = _nullProvider.getNullValue(ctxt);
        } else if (_valueTypeDeserializer == null) {
            value = _valueDeserializer.deserialize(p, ctxt);
            // 04-May-2018, tatu: [databind#2023] Coercion from String (mostly) can give null
            if (value == null) {
                if (_skipNulls) {
                    return;
                }
                value = _nullProvider.getNullValue(ctxt);
            }
        } else {
            value = _valueDeserializer.deserializeWithType(p, ctxt, _valueTypeDeserializer);
        }
        set(ctxt, bean, value);
    }

    @Override
    public Object deserializeSetAndReturn(JsonParser p,
            DeserializationContext ctxt, Object instance)
        throws JacksonException
    {
        Object value;
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            if (_skipNulls) {
                return instance;
            }
            value = _nullProvider.getNullValue(ctxt);
        } else if (_valueTypeDeserializer == null) {
            value = _valueDeserializer.deserialize(p, ctxt);
            // 04-May-2018, tatu: [databind#2023] Coercion from String (mostly) can give null
            if (value == null) {
                if (_skipNulls) {
                    return instance;
                }
                value = _nullProvider.getNullValue(ctxt);
            }
        } else {
            value = _valueDeserializer.deserializeWithType(p, ctxt, _valueTypeDeserializer);
        }
        return setAndReturn(ctxt, instance, value);
    }

    @Override
    public void set(DeserializationContext ctxt, Object bean, Object v)
    {
        try {
            _optimizedSetter.accept(bean, v);
        } catch (Throwable e) {
            _reportProblem(ctxt, bean, v, e);
        }
    }
}
