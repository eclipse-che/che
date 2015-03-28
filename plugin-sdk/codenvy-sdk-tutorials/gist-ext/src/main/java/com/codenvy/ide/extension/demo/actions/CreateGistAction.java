/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.demo.actions;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import com.codenvy.ide.extension.demo.GistExtensionLocalizationConstant;
import com.codenvy.ide.extension.demo.GistExtensionResources;
import com.codenvy.ide.extension.demo.createGist.CreateGistPresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** Action for creating Gist on GitHub. */
@Singleton
public class CreateGistAction extends Action {
    private CreateGistPresenter createGistPresenter;

    @Inject
    public CreateGistAction(GistExtensionResources resources,
                            CreateGistPresenter createGistPresenter,
                            GistExtensionLocalizationConstant localizationConstants) {
        super(localizationConstants.createGistActionText(), localizationConstants.createGistActionDescription(), resources.github());
        this.createGistPresenter = createGistPresenter;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        createGistPresenter.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}