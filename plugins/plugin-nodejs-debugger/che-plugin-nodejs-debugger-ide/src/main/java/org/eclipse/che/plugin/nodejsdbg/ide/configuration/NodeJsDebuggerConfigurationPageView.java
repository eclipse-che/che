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
package org.eclipse.che.plugin.nodejsdbg.ide.configuration;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link NodeJsDebuggerConfigurationPagePresenter}.
 *
 * @author Anatolii Bazko
 */
public interface NodeJsDebuggerConfigurationPageView extends View<NodeJsDebuggerConfigurationPageView.ActionDelegate> {

    /** Returns path to the binary. */
    String getScriptPath();

    /** Sets path to the binary. */
    void setScriptPath(String path);

    /** Action handler for the view's controls. */
    interface ActionDelegate {

        /** Called when 'Binary Path' has been changed. */
        void onScriptPathChanged();
    }
}
