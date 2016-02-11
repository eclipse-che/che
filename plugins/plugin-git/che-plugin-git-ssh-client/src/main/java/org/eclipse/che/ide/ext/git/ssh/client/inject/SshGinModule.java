/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.ssh.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.ext.git.ssh.client.manage.SshKeyManagerPresenter;
import org.eclipse.che.ide.ext.git.ssh.client.manage.SshKeyManagerViewImpl;
import org.eclipse.che.ide.ext.git.ssh.client.upload.UploadSshKeyView;
import org.eclipse.che.ide.ext.git.ssh.client.upload.UploadSshKeyViewImpl;
import org.eclipse.che.ide.ext.git.ssh.client.manage.SshKeyManagerView;

/** @author Andrey Plotnikov */
@ExtensionGinModule
public class SshGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(SshKeyManagerView.class).to(SshKeyManagerViewImpl.class).in(Singleton.class);
        bind(UploadSshKeyView.class).to(UploadSshKeyViewImpl.class).in(Singleton.class);
        GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(SshKeyManagerPresenter.class);
    }
}