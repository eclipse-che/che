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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.settings.SettingsPresenter;

/**
 * The class describes action which shows widget for configuring java properties.
 *
 * @author Dmitry Shnurenko
 */
public class SettingsAction extends Action {

    private final SettingsPresenter presenter;

    @Inject
    public SettingsAction(CoreLocalizationConstant locale, SettingsPresenter presenter, Resources resources) {
        super(locale.projectSettingsTitle(), null, null, resources.settings());

        this.presenter = presenter;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.show();
    }
}
