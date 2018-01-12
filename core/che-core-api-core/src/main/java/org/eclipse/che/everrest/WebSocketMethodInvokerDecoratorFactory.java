/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.everrest;

import org.everrest.core.impl.method.MethodInvokerDecorator;
import org.everrest.core.impl.method.MethodInvokerDecoratorFactory;
import org.everrest.core.method.MethodInvoker;

/**
 * Create new instance of WebSocketMethodInvokerDecorator.
 *
 * @author andrew00x
 * @see WebSocketMethodInvokerDecorator
 */
public class WebSocketMethodInvokerDecoratorFactory implements MethodInvokerDecoratorFactory {
  @Override
  public MethodInvokerDecorator makeDecorator(MethodInvoker invoker) {
    return new WebSocketMethodInvokerDecorator(invoker);
  }
}
