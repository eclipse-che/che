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
package org.eclipse.che.ide.api.dialogs;

/**
 * Interface to the input dialog component.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface InputDialog {

    /** Operate the input dialog: show it and manage user actions. */
    void show();

    /**
     * Set the {@link InputValidator} to be called whenever the text changes in the input field in the view.
     * <p/>
     * If validator finds the input invalid, the error message is displayed in the dialog's view.
     *
     * @param inputValidator
     *         validator to use
     * @return this {@link InputDialog}
     */
    InputDialog withValidator(InputValidator inputValidator);
}
