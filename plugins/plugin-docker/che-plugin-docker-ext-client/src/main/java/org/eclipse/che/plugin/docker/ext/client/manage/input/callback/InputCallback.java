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
package org.eclipse.che.plugin.docker.ext.client.manage.input.callback;

import org.eclipse.che.plugin.docker.client.dto.AuthConfig;

/**
 * Callback called when the user clicks on "Save" in the input dialog.
 *
 * @author Sergii Leschenko
 */
public interface InputCallback {
    void saved(AuthConfig authConfig);
}