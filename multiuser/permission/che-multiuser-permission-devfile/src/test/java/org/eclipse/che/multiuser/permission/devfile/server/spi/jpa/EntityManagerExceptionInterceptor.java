/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.devfile.server.spi.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.persistence.EntityManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class EntityManagerExceptionInterceptor implements MethodInterceptor {
  @Inject Provider<EntityManager> emf;

  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    emf.get().getTransaction().setRollbackOnly();
    throw new RuntimeException("Database exception");
  }
}
