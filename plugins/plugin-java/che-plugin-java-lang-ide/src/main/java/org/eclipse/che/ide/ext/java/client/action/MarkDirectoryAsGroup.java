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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;

/**
 * Group for the actions which are configured build path.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class MarkDirectoryAsGroup extends DefaultActionGroup {
    private final AppContext appContext;

    @Inject
    public MarkDirectoryAsGroup(ActionManager actionManager, AppContext appContext, JavaLocalizationConstant locale) {
        super(locale.markDirectoryAs(), true, actionManager);

        this.appContext = appContext;
    }

    @Override
    public void update(ActionEvent e) {
        final Resource resource = appContext.getResource();

        if (resource == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        final Optional<Project> project = resource.getRelatedProject();

        e.getPresentation().setEnabledAndVisible(project.isPresent() && project.get().isTypeOf(JAVAC));
    }
}
