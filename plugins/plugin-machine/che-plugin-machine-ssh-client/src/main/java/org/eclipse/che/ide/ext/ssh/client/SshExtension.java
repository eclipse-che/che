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
package org.eclipse.che.ide.ext.ssh.client;

import org.eclipse.che.ide.api.extension.Extension;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Extension add Ssh support to the IDE Application.
 *
 * @author Andrey Plotnikov
 */
@Singleton
@Extension(title = "SSH", version = "3.0.0")
public class SshExtension {
    @Inject
    public SshExtension() {
    }
}