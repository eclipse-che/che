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
package org.eclipse.che.workspace.infrastructure.docker.provisioner;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Alexander Garagatyi */
public class ContainerSystemSettingsProvisioningModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), ContainerSystemSettingsProvisioner.class);
  }
}
