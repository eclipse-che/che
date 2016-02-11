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
package org.eclipse.che.ide.ext.git.client.url;

import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The view of {@link ShowProjectGitReadOnlyUrlPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface ShowProjectGitReadOnlyUrlView extends View<ShowProjectGitReadOnlyUrlView.ActionDelegate> {
    /** Needs for delegate some function into Git url view. */
    public interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Close button. */
        void onCloseClicked();
    }

    /**
     * Set project locale URL into field on the view.
     *
     * @param url
     *         text what will be shown on view
     */
    void setLocaleUrl(@NotNull String url);

    /**
     * Set project remote URL into field on the view.
     *
     * @param remotes
     *         remote URLs what will be shown on view
     */
    void setRemotes(List<Remote> remotes);

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();
}