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
package org.eclipse.che.api.core.rest;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/** @author andrew00x */
public class CoreRestModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(CheJsonProvider.class);
    bind(ApiExceptionMapper.class);
    bind(RuntimeExceptionMapper.class);
    Multibinder.newSetBinder(binder(), Class.class, Names.named("che.json.ignored_classes"));
  }
}
