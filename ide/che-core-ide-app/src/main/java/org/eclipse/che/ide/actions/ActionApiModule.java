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
package org.eclipse.che.ide.actions;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.eclipse.che.ide.actions.find.FindActionView;
import org.eclipse.che.ide.actions.find.FindActionViewImpl;
import org.eclipse.che.ide.api.action.ActionManager;

/** GIN module for configuring Action API components. */
public class ActionApiModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(StartUpActionsProcessor.class).asEagerSingleton();
    bind(ActionManager.class).to(ActionManagerImpl.class).in(Singleton.class);
    bind(FindActionView.class).to(FindActionViewImpl.class).in(Singleton.class);
  }
}
