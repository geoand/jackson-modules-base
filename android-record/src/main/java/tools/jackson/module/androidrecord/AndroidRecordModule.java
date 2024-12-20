package tools.jackson.module.androidrecord;

import java.lang.reflect.*;
import java.util.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.jackson.core.Version;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.*;
import tools.jackson.databind.module.SimpleModule;

/**
 * Module that allows (de)serialization of records using the canonical constructor and accessors on Android,
 * where java records are supported through desugaring, and Jackson's built-in support for records doesn't work,
 * since the desugared classes have a non-standard super class,
 * and record component-related reflection methods are missing.
 *
 * <p>
 * See <a href="https://android-developers.googleblog.com/2023/06/records-in-android-studio-flamingo.html">
 * Android Developers Blog article</a>
 * <p>
 * Note: this module is a no-op when no Android-desugared records are being (de)serialized,
 * so it is safe to use in code shared between Android and non-Android platforms.
 * <p>
 * Note: the canonical record constructor is identified through matching of parameter names and types with fields.
 * Therefore, this module doesn't allow a deserialized desugared record to have a custom constructor
 * with the same set of parameter names and types as the canonical one.
 * For the same reason, this module requires that desugared canonical record constructor parameter names
 * be stored in class files. Apparently, with Android SDK 34 tooling, that is the case by default.
 * If that ever changes, it may require an explicit setting in build files.
 *
 * @author Eran Leshem
 **/
public class AndroidRecordModule extends SimpleModule
{
  private static final long serialVersionUID = 1L;

  static final class AndroidRecordNaming
    extends DefaultAccessorNamingStrategy
  {
    /**
     * Names of actual Record components from definition; auto-detected.
     */
    private final Set<String> _componentNames;

    AndroidRecordNaming(MapperConfig<?> config, AnnotatedClass forClass) {
      super(config, forClass,
              // no setters for (immutable) Records:
              null,
              "get", "is", null);
      _componentNames = getDesugaredRecordComponents(forClass.getRawType()).map(Field::getName)
              .collect(Collectors.toSet());
    }

    @Override
    public String findNameForRegularGetter(AnnotatedMethod am, String name)
    {
      // By default, field names are un-prefixed, but verify so that we will not
      // include "toString()" or additional custom methods (unless latter are
      // annotated for inclusion)
      if (_componentNames.contains(name)) {
        return name;
      }
      // but also allow auto-detecting additional getters, if any?
      return super.findNameForRegularGetter(am, name);
    }
  }

  static class AndroidRecordClassIntrospector extends BasicClassIntrospector
  {
      private static final long serialVersionUID = 1L;

      public AndroidRecordClassIntrospector() {
          super();
      }

      AndroidRecordClassIntrospector(MapperConfig<?> config) {
          super(config);
      }

      @Override
      protected POJOPropertiesCollector collectProperties(JavaType type, AnnotatedClass classDef,
            boolean forSerialization, String mutatorPrefix)
      {
          if (isDesugaredRecordClass(type.getRawClass())) {
            AccessorNamingStrategy accNaming = new AndroidRecordNaming(_config, classDef);
            return constructPropertyCollector(type, classDef, forSerialization, accNaming);
          }
          return super.collectProperties(type, classDef, forSerialization, mutatorPrefix);
      }

      @Override
      public BasicClassIntrospector forMapper() {
          return this;
      }

      @Override
      public BasicClassIntrospector forOperation(MapperConfig<?> config) {
          return new AndroidRecordClassIntrospector(config);
      }
  }

  static class AndroidRecordAnnotationIntrospector extends AnnotationIntrospector
  {
    private static final long serialVersionUID = 1L;

    @Override
    public Version version() {
      return PackageVersion.VERSION;
    }

    @Override
    public PotentialCreator findPreferredCreator(MapperConfig<?> config,
            AnnotatedClass valueClass,
            List<PotentialCreator> declaredConstructors,
            List<PotentialCreator> declaredFactories)
    {
      PotentialCreator foundCreator = null;
      if (AndroidRecordModule.isDesugaredRecordClass(valueClass.getRawType())) {
        Map<String, Type> components = AndroidRecordModule.getDesugaredRecordComponents(valueClass.getRawType())
                .collect(Collectors.toMap(Field::getName, Field::getGenericType));

        for (PotentialCreator creator : declaredConstructors) {
          if (!(creator.creator() instanceof AnnotatedConstructor)) {
            continue;
          }
          AnnotatedConstructor constructor = (AnnotatedConstructor) creator.creator();
          Parameter[] parameters = constructor.getAnnotated().getParameters();
          Map<String, Type> parameterTypes = Arrays.stream(parameters)
                  .collect(Collectors.toMap(Parameter::getName, parameter -> fixAndroidGenericType(parameter.getParameterizedType())));

          if (parameterTypes.equals(components)) {
            if (foundCreator != null) {
              throw new IllegalArgumentException(String.format(
                      "Multiple constructors match set of components for record %s", valueClass.getRawType().getName()));
            }

            foundCreator = creator.introspectParamNames(config, Arrays.stream(parameters).map(Parameter::getName).map(PropertyName::new).toArray(PropertyName[]::new));
          }
        }
      }
      return foundCreator;
    }
  }
  
  @Override
  public void setupModule(SetupContext context) {
    super.setupModule(context);
    MapperBuilder<?,?> builder = (MapperBuilder<?,?>) context.getOwner();
    builder.classIntrospector(new AndroidRecordClassIntrospector());
    context.insertAnnotationIntrospector(new AndroidRecordAnnotationIntrospector());
  }

  static boolean isDesugaredRecordClass(Class<?> raw) {
    Class<?> sup = raw.getSuperclass();
    return (sup != null) && sup.getName().equals("com.android.tools.r8.RecordTag");
  }

  static Stream<Field> getDesugaredRecordComponents(Class<?> raw) {
      return Arrays.stream(raw.getDeclaredFields()).filter(field -> ! Modifier.isStatic(field.getModifiers()));
  }

  static Class<?> arrayTypeCompat(Class<?> componentType) {
    return Array.newInstance(componentType, 0).getClass();
  }

  static Type fixAndroidGenericType(Type type) {
    if (type instanceof GenericArrayType) {
      Type componentType = fixAndroidGenericType(((GenericArrayType) type).getGenericComponentType());
      if (componentType instanceof Class<?>) {
        return arrayTypeCompat((Class<?>) componentType);
      }
    }
    else if (type instanceof ParameterizedType) {
      //if the parameterized type is not actually parameterized, deduce the raw type
      ParameterizedType parameterizedType = (ParameterizedType) type;
      if (parameterizedType.getOwnerType() == null) {
        Type rawType = parameterizedType.getRawType();
        if (rawType instanceof Class<?>) {
          Class<?> rawComponentClass = (Class<?>) rawType;
          if (rawComponentClass.getTypeParameters().length == 0) {
            return rawComponentClass;
          }
        }
      }
    }
    return type;
  }
}
