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

package org.eclipse.che.api.agent;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.inject.DynaModule;

/** Test language server module */
@DynaModule
public class TestLSModule extends AbstractModule {
  @Override
  protected void configure() {

    if (System.getProperty("test_language_server_enabled") != null) {
      Multibinder<Agent> agents = Multibinder.newSetBinder(binder(), Agent.class);
      agents.addBinding().to(TestLSAgent.class);
    }
  }
}
