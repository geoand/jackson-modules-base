package tools.jackson.module.blackbird.deser;

import java.util.function.Function;
import java.util.function.Supplier;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdValueInstantiator;

class OptimizedValueInstantiator extends StdValueInstantiator
{
    private final Supplier<?> _optimizedDefaultCreator;
    private final Function<Object[], Object> _optimizedArgsCreator;

    protected OptimizedValueInstantiator(StdValueInstantiator original,
            Supplier<?> defaultCreator, Function<Object[], Object> argsCreator) {
        super(original);
        this._optimizedDefaultCreator = defaultCreator;
        this._optimizedArgsCreator = argsCreator;
    }

    @Override
    public boolean canCreateUsingDefault() {
        return _optimizedDefaultCreator != null || super.canCreateUsingDefault();
    }

    @Override
    public boolean canCreateFromObjectWith() {
        return _optimizedArgsCreator != null || super.canCreateFromObjectWith();
    }

    @Override
    public Object createUsingDefault(DeserializationContext ctxt) throws JacksonException {
        if (_optimizedDefaultCreator != null) {
            try {
                return _optimizedDefaultCreator.get();
            } catch (Exception e) {
                return ctxt.handleInstantiationProblem(_valueClass, null, e);
            }
        }
        return super.createUsingDefault(ctxt);
    }

    @Override
    public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws JacksonException {
        if (_optimizedArgsCreator != null) {
            try {
                return _optimizedArgsCreator.apply(args);
            } catch (Exception e) {
                return ctxt.handleInstantiationProblem(_valueClass, args, e);
            }
        }
        return super.createFromObjectWith(ctxt, args);
    }
}
