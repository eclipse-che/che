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
package org.eclipse.che.ide.machine;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.ide.machine.chooser.MachineChooserView;
import org.eclipse.che.ide.machine.chooser.MachineChooserViewImpl;
import org.eclipse.che.requirejs.ModuleHolder;

/** GIN module for configuring Machine API related components. */
public class MachineApiModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ProcessesOutputRestorer.class).asEagerSingleton();
    bind(MachineFailNotifier.class).asEagerSingleton();

    bind(MachineChooserView.class).to(MachineChooserViewImpl.class);
    bind(ModuleHolder.class).in(Singleton.class);
    bindConstant()
        .annotatedWith(Names.named("machine.extension.api_port"))
        .to(Constants.WS_AGENT_PORT);
  }
}
