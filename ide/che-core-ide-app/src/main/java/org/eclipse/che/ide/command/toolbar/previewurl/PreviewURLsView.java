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
package org.eclipse.che.ide.command.toolbar.previewurl;

import org.eclipse.che.ide.api.mvp.View;

/** View for displaying preview URLs. */
public interface PreviewURLsView extends View<PreviewURLsView.ActionDelegate> {

    /** Add preview URL to the view. */
    void addUrl(String url);

    /** Remove preview URL from the view. */
    void removeUrl(String url);

    /** Remove all preview URLs from the view. */
    void removeAll();

    interface ActionDelegate {

        /** Called when preview URL has been chosen. */
        void onUrlChosen(String url);
    }
}
