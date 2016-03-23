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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.preferences.PreferencesPresenter;

/** @author Evgen Vidolob */
@Singleton
public class ShowPreferencesAction extends Action {

    private final PreferencesPresenter presenter;

    private final AppContext           appContext;

    @Inject
    public ShowPreferencesAction(Resources resources, PreferencesPresenter presenter, AppContext appContext) {
        super("Preferences", "Preferences", null, resources.preferences());
        this.presenter = presenter;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.showPreferences();
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setVisible(true);
        if ((appContext.getCurrentProject() == null && !appContext.getCurrentUser().isUserPermanent())) {
            e.getPresentation().setEnabled(false);
        } else {
            e.getPresentation().setEnabled(true);
        }
    }
}
