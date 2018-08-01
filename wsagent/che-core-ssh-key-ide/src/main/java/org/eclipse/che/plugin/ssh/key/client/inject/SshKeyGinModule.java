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
package org.eclipse.che.plugin.ssh.key.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.plugin.ssh.key.client.manage.ShowSshKeyView;
import org.eclipse.che.plugin.ssh.key.client.manage.ShowSshKeyViewImpl;
import org.eclipse.che.plugin.ssh.key.client.manage.SshKeyManagerPresenter;
import org.eclipse.che.plugin.ssh.key.client.manage.SshKeyManagerView;
import org.eclipse.che.plugin.ssh.key.client.manage.SshKeyManagerViewImpl;
import org.eclipse.che.plugin.ssh.key.client.upload.UploadSshKeyView;
import org.eclipse.che.plugin.ssh.key.client.upload.UploadSshKeyViewImpl;

/** @author Andrey Plotnikov */
@ExtensionGinModule
public class SshKeyGinModule extends AbstractGinModule {
  @Override
  protected void configure() {
    bind(SshKeyManagerView.class).to(SshKeyManagerViewImpl.class).in(Singleton.class);
    bind(UploadSshKeyView.class).to(UploadSshKeyViewImpl.class).in(Singleton.class);
    bind(ShowSshKeyView.class).to(ShowSshKeyViewImpl.class).in(Singleton.class);
    GinMultibinder<PreferencePagePresenter> prefBinder =
        GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
    prefBinder.addBinding().to(SshKeyManagerPresenter.class);
  }
}
