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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.search.FullTextSearchPresenter;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for finding text in the files on the workspace.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
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
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              locale.actionFullTextSearch(),
              locale.actionFullTextSearchDescription(),
              null,
              resources.find());
        this.presenter = presenter;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        final Project project = appContext.getRootProject();

        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(project != null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        final Resource[] resources = appContext.getResources();
        final Path searchPath;

        if (resources == null || resources.length == 0 || resources.length > 1) {
            searchPath = Path.ROOT;
        } else {
            if (resources[0] instanceof Container) {
                searchPath = resources[0].getLocation();
            } else {
                final Container parent = resources[0].getParent();
                searchPath = parent != null ? parent.getLocation() : Path.ROOT;
            }
        }

        presenter.showDialog(searchPath);
    }
}
