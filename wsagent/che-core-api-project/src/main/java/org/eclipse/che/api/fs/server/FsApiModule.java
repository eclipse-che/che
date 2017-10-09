/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.fs.server;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.fs.server.impl.RootAwarePathTransformer;
import org.eclipse.che.api.fs.server.impl.SimpleFsDtoConverter;
import org.eclipse.che.api.fs.server.impl.ValidatingFsManager;

public class FsApiModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(FsManager.class).to(ValidatingFsManager.class);
    bind(FsDtoConverter.class).to(SimpleFsDtoConverter.class);
    bind(PathTransformer.class).to(RootAwarePathTransformer.class);
  }
}
