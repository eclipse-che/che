package org.eclipse.che.api.languageserver.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * This class forwards invocation of methods from a wrapped instance to another object, if it
 * declares the method in question. It serves to create wrapper classes without having to implement
 * a large protocol
 *
 * @author thomas
 */
public class DynamicWrapper implements InvocationHandler {
  private Object overrides;
  private Object wrapped;

  public DynamicWrapper(Object overrides, Object wrapped) {
    this.overrides = overrides;
    this.wrapped = wrapped;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Method declaredMethod =
          overrides.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
      return declaredMethod.invoke(overrides, args);
    } catch (NoSuchMethodException e) {
      return method.invoke(wrapped, args);
    }
  }
}
