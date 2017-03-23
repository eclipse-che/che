/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.ssh.key.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.Extension;

/**
 * Extension add Ssh support to the IDE Application.
 *
 * @author Andrey Plotnikov
 */
@Singleton
@Extension(title = "SSH key", version = "4.0.0")
public class SshKeyExtension {
    @Inject
    public SshKeyExtension() {
    }
}
