/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * InitialContextFactory implementation. Configured in <code>RmiServer</code>.
 *
 * @author Evgen Vidolob
 */
public class JNDI implements InvocationHandler, InitialContextFactory {

  @Override
  public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
    return (Context)
        Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {Context.class}, this);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class.equals(method.getDeclaringClass())) {
      return method.invoke(this, args);
    }
    throw new NamingException("JNDI can't serve method not form Object");
  }
}
