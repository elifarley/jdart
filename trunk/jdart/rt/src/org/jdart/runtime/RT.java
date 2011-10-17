package org.jdart.runtime;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

public class RT {
  public static final String BSM_DESC =
      methodType(CallSite.class, Lookup.class, String.class, MethodType.class).
        toMethodDescriptorString();
  public static final String BSM_PLUS_CLASS_DESC =
      methodType(CallSite.class, Lookup.class, String.class, MethodType.class, Class.class).
        toMethodDescriptorString();
  
  private static final ClassValue<HashMap<String, MethodHandle>> VTABLE_MAP =
      new ClassValue<HashMap<String,MethodHandle>>() {
    @Override
    protected HashMap<String, MethodHandle> computeValue(Class<?> type) {
      HashMap<String, MethodHandle> map = new HashMap<>();
      for(Constructor<?> constructor: type.getConstructors()) {
        // only keep the default constructor
        if (constructor.getParameterTypes().length != 0) {
          continue;
        }
        
        MethodHandle mh;
        try {
          mh = MethodHandles.publicLookup().unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
          throw new AssertionError(e.getMessage(), e);
        }
        map.put("<init>", mh);
        break;
      }
      
      for(Method method: type.getMethods()) {
        if (method.getDeclaringClass() == Object.class) {
          // don't inherits method from java.lang.Object !
          continue;
        }

        String name = method.getName();
        MethodHandle mh;
        switch(name.charAt(0)) {
        case '@': // native
          try {
            Class<?> nativeClass = getNativeClass(type);
            name = name.substring(1);
            mh = MethodHandles.publicLookup().findStatic(nativeClass, name,
                methodType(method.getReturnType(), method.getParameterTypes()));
          } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            //mh = prepareError(LINKAGE_ERROR, method.getParameterTypes().length);
            throw new LinkageError("no native for "+method.getDeclaringClass().getName()+'.'+method.getName(), e);
          }
          break;
        default:
          if (map.containsKey(name)) {
            mh = prepareError(NO_MULTI_DISPATCH_YET, method.getParameterTypes().length);
          } else {
            try {
              mh = MethodHandles.publicLookup().unreflect(method);
            } catch (IllegalAccessException e) {
              throw new AssertionError(e.getMessage(), e);
            }
          }
        }
        
        map.put(name, mh);
      }
      return map;
    }
  };
  
  static Class<?> getNativeClass(Class<?> type) throws ClassNotFoundException {
    String name = type.getName();
    int index = name.lastIndexOf('.');
    if (index != -1) {
      name = name.substring(index + 1);
    }
    return Class.forName("org.jdart.runtime.impls."+name);
  }
  
  static MethodHandle prepareError(MethodHandle mh, int parameterCount) {
    //FIXME, lazy store in an array of 256 cells
    MethodType type = MethodType.genericMethodType(parameterCount);
    MethodHandle target = MethodHandles.dropArguments(mh, 0, type.parameterList());
    return target.asType(type);
  }
  
  public static void noMultiDispatchYet() {
    throw new AssertionError("this runtime doesn't support multi-dispatch yet !");
  }
  public static void linkageError() {
    throw new LinkageError("no native available");
  }
  
  static final MethodHandle NO_MULTI_DISPATCH_YET;
  static final MethodHandle LINKAGE_ERROR;
  static {
    try {
      Lookup publicLookup = MethodHandles.publicLookup();
      NO_MULTI_DISPATCH_YET = publicLookup.findStatic(RT.class, "noMultiDispatchYet",
          methodType(void.class));
      LINKAGE_ERROR = publicLookup.findStatic(RT.class, "linkageError",
          methodType(void.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e.getMessage(), e);
    }
  }
  
  static MethodHandle lookup(Class<?> declaringClass, String name, int parameterCount) {
    MethodHandle target = VTABLE_MAP.get(declaringClass).get(name);
    if (target == null) {
      throw new LinkageError("no method "+name+" in "+declaringClass.getName());
    }
    if (target.type().parameterCount() != parameterCount) {
      throw new LinkageError("wrong number of parameter for method "+name+" in "+declaringClass.getName());
    }
    return target;
  }
  
  public static CallSite newCall(@SuppressWarnings("unused") Lookup lookup, String name, MethodType type) {
    Class<?> clazz = type.returnType();
    
    // get default constructor
    MethodHandle defaultInit = lookup(clazz, "<init>", 0);
    defaultInit = MethodHandles.dropArguments(defaultInit, 0, type.parameterList());
    
    // get pseudo constructor
    MethodHandle pseudoInit = lookup(clazz, name, 1 + type.parameterCount());
    
    MethodHandle identity = MethodHandles.identity(clazz);
    identity = MethodHandles.dropArguments(identity, 1, type.parameterList());
    MethodHandle init = MethodHandles.foldArguments(identity, pseudoInit);
    
    MethodHandle target = MethodHandles.foldArguments(init, defaultInit);
    
    target = target.asType(type);
    return new ConstantCallSite(target);
  }
  
  public static CallSite funCall(@SuppressWarnings("unused") Lookup lookup, String name, MethodType type, Class<?> declaringClass) {
    MethodHandle target = lookup(declaringClass, name, type.parameterCount());
    target = target.asType(type);
    return new ConstantCallSite(target);
  }
  
  public static CallSite superCall(Lookup lookup, String name, MethodType type) {
    return funCall(lookup, name, type, type.parameterType(0));
  }
  
  public static CallSite methCall(@SuppressWarnings("unused") Lookup lookup, String name, MethodType type) {
    InliningCacheCallSite callSite = new InliningCacheCallSite(type, name);
    
    MethodHandle fallback = FALLBACK.bindTo(callSite);
    fallback = fallback.asCollector(Object[].class, type.parameterCount());
    fallback = fallback.asType(type);
    callSite.setTarget(fallback);
    return callSite;
  }
  
  public static class InliningCacheCallSite extends MutableCallSite {
    private final String name;

    public InliningCacheCallSite(MethodType type, String name) {
      super(type);
      this.name = name;
    }
    
    public Object fallback(Object[] args) throws Throwable {
      Class<?> receiverClass = args[0].getClass();
      
      MethodHandle target = lookup(receiverClass, name, args.length);
      target = target.asType(type());
      
      MethodHandle test = CHECK_ClASS.bindTo(receiverClass);
      test = test.asType(methodType(boolean.class, type().parameterType(0)));
      MethodHandle guard = MethodHandles.guardWithTest(test, target, getTarget());
      setTarget(guard);
      
      return target.invokeWithArguments(args);
    }
  }
  
  public static boolean checkClass(Class<?> type, Object o) {
    return type == o.getClass();
  }
  
  static final MethodHandle CHECK_ClASS;
  static final MethodHandle FALLBACK;
  static {
    try {
      CHECK_ClASS = MethodHandles.publicLookup().findStatic(RT.class, "checkClass",
          methodType(boolean.class, Class.class, Object.class));
      FALLBACK = MethodHandles.publicLookup().findVirtual(InliningCacheCallSite.class, "fallback",
          methodType(Object.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e.getMessage(), e);
    }
  }
}
