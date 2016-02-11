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
package org.eclipse.che.ide.ext.git.ssh.client;

import org.eclipse.che.ide.api.extension.Extension;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Extension add Ssh support to the IDE Application.
 *
 * @author Andrey Plotnikov
 */
@Singleton
@Extension(title = "Git SSH", version = "3.0.0")
public class GitSshExtension {
    @Inject
    public GitSshExtension() {
    }
}