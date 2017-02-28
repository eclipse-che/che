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
package org.eclipse.che.ide.command.editor.page.context;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link ContextPage}.
 *
 * @author Artem Zatsarynnyi
 */
public interface ContextPageView extends View<ContextPageView.ActionDelegate> {

    void setWorkspace(boolean value);

    /** The action delegate for this view. */
    interface ActionDelegate {

        void onWorkspaceChanged(boolean value);
    }
}
