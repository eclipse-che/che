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
package org.eclipse.che.plugin.docker.ext.client.manage;


import org.eclipse.che.plugin.docker.ext.client.manage.input.InputDialog;
import org.eclipse.che.plugin.docker.ext.client.manage.input.callback.InputCallback;

import static org.eclipse.che.plugin.docker.ext.client.manage.input.InputDialogPresenter.InputMode;

/**
 * Factory for {@link InputDialog} component.
 *
 * @author Sergii Leschenko
 */
public interface CredentialsDialogFactory {
    /**
     * Create input dialog
     *
     * @param inputMode
     *         Input mode of dialog. Can be equals to CREATE or EDIT.
     * @param inputCallback
     *         callback which will be called when user click on Save button
     * @return created instance of {@link InputDialog}
     */
    InputDialog createInputDialog(InputMode inputMode, InputCallback inputCallback);
}
