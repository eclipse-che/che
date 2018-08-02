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
package org.eclipse.che.ide.ext.gwt.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.gwt.client.command.GwtCommandType;

/**
 * GIN module for Che GWT extension.
 *
 * @author Artem Zatsarynnyi
 */
@ExtensionGinModule
public class GwtGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    GinMultibinder.newSetBinder(binder(), CommandType.class).addBinding().to(GwtCommandType.class);
  }
}
