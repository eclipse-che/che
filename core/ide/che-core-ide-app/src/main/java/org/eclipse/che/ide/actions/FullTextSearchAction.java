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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.search.FullTextSearchPresenter;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for finding text in the files on the workspace.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class FullTextSearchAction extends AbstractPerspectiveAction {

    private final FullTextSearchPresenter presenter;
    private final AppContext              appContext;

    @Inject
    public FullTextSearchAction(FullTextSearchPresenter presenter,
                                AppContext appContext,
                                Resources resources,
                                CoreLocalizationConstant locale) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              locale.actionFullTextSearch(),
              locale.actionFullTextSearchDescription(),
              null,
              resources.find());
        this.presenter = presenter;
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabled(appContext.getCurrentProject() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.showDialog();
    }
}
