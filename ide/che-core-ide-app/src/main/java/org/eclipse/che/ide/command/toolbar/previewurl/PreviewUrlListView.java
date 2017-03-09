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

/** View for preview URLs list. */
public interface PreviewUrlListView extends View<PreviewUrlListView.ActionDelegate> {

    /** Add preview URL to the list. */
    void addUrl(String url);

    /** Remove preview URL from the list. */
    void removeUrl(String url);

    /** Clear preview URLs list. */
    void clearList();

    interface ActionDelegate {

        /** Called when preview URL has been chosen. */
        void onUrlChosen(String url);
    }
}
