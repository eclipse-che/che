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
package org.eclipse.che.plugin.docker.ext.client.manage.input;

import org.eclipse.che.plugin.docker.client.dto.AuthConfig;

/**
 * Interface for the input dialog component.
 *
 * @author Sergii Leschenko
 */
public interface InputDialog {
    /** Operate the input dialog: show it and manage user actions. */
    void show();

    /** Set the data which will be displayed in the dialog */
    void setData(AuthConfig authConfig);
}