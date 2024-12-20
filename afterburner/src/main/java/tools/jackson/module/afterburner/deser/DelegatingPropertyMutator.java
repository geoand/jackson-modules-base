package tools.jackson.module.afterburner.deser;

import tools.jackson.databind.deser.SettableBeanProperty;

/**
 * Fallback mutator used as replacement in case a "broken" mutator
 * (failure via couple of well-known indicators of broken generated
 * mutator) is encountered
 *
 * @since 2.9
 */
public final class DelegatingPropertyMutator
    extends BeanPropertyMutator
{
    protected final SettableBeanProperty _fallback;

    public DelegatingPropertyMutator(SettableBeanProperty prop) {
        _fallback = prop;
    }

    @Override
    public void intSetter(Object bean, int propertyIndex, int value) {
        _fallback.set(null, bean, value);
    }
    @Override
    public void longSetter(Object bean, int propertyIndex, long value) {
        _fallback.set(null, bean, value);
    }
    @Override
    public void booleanSetter(Object bean, int propertyIndex, boolean value) {
        _fallback.set(null, bean, value);
    }
    @Override
    public void stringSetter(Object bean, int propertyIndex, String value) {
        _fallback.set(null, bean, value);
    }
    @Override
    public void objectSetter(Object bean, int propertyIndex, Object value) {
        _fallback.set(null, bean, value);
    }

    @Override
    public void intField(Object bean, int propertyIndex, int value) {
        _fallback.set(null, bean, value);
    }
    @Override
    public void longField(Object bean, int propertyIndex, long value) {
        _fallback.set(null, bean, value);
    }
    @Override
    public void booleanField(Object bean, int propertyIndex, boolean value) {
        _fallback.set(null, bean, value);
    }
    @Override
    public void stringField(Object bean, int propertyIndex, String value) {
        _fallback.set(null, bean, value);
    }
    @Override
    public void objectField(Object bean, int propertyIndex, Object value) {
        _fallback.set(null, bean, value);
    }
}
