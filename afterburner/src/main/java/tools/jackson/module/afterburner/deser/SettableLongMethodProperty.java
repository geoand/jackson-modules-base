package tools.jackson.module.afterburner.deser;

import tools.jackson.core.*;

import tools.jackson.databind.*;
import tools.jackson.databind.deser.SettableBeanProperty;

public final class SettableLongMethodProperty
    extends OptimizedSettableBeanProperty<SettableLongMethodProperty>
{
    private static final long serialVersionUID = 1L;

    public SettableLongMethodProperty(SettableBeanProperty src,
            BeanPropertyMutator mutator, int index)
    {
        super(src, mutator, index);
    }

    @Override
    protected SettableBeanProperty withDelegate(SettableBeanProperty del) {
        return new SettableLongMethodProperty(del, _propertyMutator, _optimizedIndex);
    }

    @Override
    public SettableBeanProperty withMutator(BeanPropertyMutator mut) {
        return new SettableLongMethodProperty(delegate, mut, _optimizedIndex);
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
        if (!p.isExpectedNumberIntToken()) {
            delegate.deserializeAndSet(p, ctxt, bean);
            return;
        }
        final long v = p.getLongValue();
        try {
            _propertyMutator.longSetter(bean, _optimizedIndex, v);
        } catch (Throwable e) {
            _reportProblem(ctxt, bean, v, e);
        }
    }

    @Override
    public Object deserializeSetAndReturn(JsonParser p,
            DeserializationContext ctxt, Object instance)
        throws JacksonException
    {
        if (p.isExpectedNumberIntToken()) {
            return setAndReturn(ctxt, instance, p.getLongValue());
        }
        return delegate.deserializeSetAndReturn(p, ctxt, instance);
    }    

    @Override
    public void set(DeserializationContext ctxt, Object bean, Object value)
    {
        // not optimal (due to boxing), but better than using reflection:
        final long v = ((Number) value).longValue();
        try {
            _propertyMutator.longSetter(bean, _optimizedIndex, v);
        } catch (Throwable e) {
            _reportProblem(ctxt, bean, v, e);
        }
    }
}
