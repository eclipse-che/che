/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
import org.eclipse.che.api.core.util.ApiInfoLogInformer;

/** @author andrew00x */
public class CoreRestModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(CheJsonProvider.class);
    bind(ApiExceptionMapper.class);
    bind(RuntimeExceptionMapper.class);
    bind(ApiInfo.class).toProvider(ApiInfoProvider.class);
    bind(ApiInfoLogInformer.class).asEagerSingleton();
    Multibinder.newSetBinder(binder(), Class.class, Names.named("che.json.ignored_classes"));
    bind(WebApplicationExceptionMapper.class);
  }
}
