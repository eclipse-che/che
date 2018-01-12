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
package org.eclipse.che.ide.dto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.client.DtoFactoryVisitorRegistry;

/** Registers client DTO providers. */
@Singleton
class DtoRegistrar {

  @Inject
  DtoRegistrar(DtoFactory dtoFactory, DtoFactoryVisitorRegistry registry) {
    registry
        .getDtoFactoryVisitors()
        .values()
        .forEach(provider -> ((DtoFactoryVisitor) provider.get()).accept(dtoFactory));
  }
}
