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
package org.eclipse.che.machine.authentication.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.machine.CheWsAgentLinksModifier;
import org.eclipse.che.machine.authentication.ide.CheAuthMachineLinksModifier;
import org.eclipse.che.machine.authentication.ide.MachineTokenServiceClient;

/** @author Anton Korneta */
@ExtensionGinModule
public class MachineAuthGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    //bind(AsyncRequestFactory.class).to(org.eclipse.che.machine.authentication.ide.MachineAsyncRequestFactory.class);
    bind(MachineTokenServiceClient.class)
        .to(org.eclipse.che.machine.authentication.ide.MachineTokenServiceClientImpl.class)
        .in(Singleton.class);
    bind(CheWsAgentLinksModifier.class).to(CheAuthMachineLinksModifier.class);
  }
}
