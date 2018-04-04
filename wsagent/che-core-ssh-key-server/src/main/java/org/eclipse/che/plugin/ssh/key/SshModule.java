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
package org.eclipse.che.plugin.ssh.key;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.plugin.ssh.key.script.SshKeyUploader;

/** + * Guice module to install ssh key components + * @author Sergii Kabashnyuk */
public class SshModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SshServiceClient.class).to(HttpSshServiceClient.class);

    bind(org.eclipse.che.plugin.ssh.key.script.SshKeyProvider.class)
        .to(org.eclipse.che.plugin.ssh.key.script.SshKeyProviderImpl.class);

    Multibinder.newSetBinder(binder(), SshKeyUploader.class);
  }
}
