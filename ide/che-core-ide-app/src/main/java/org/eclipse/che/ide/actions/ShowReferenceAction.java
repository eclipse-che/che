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
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.reference.ShowReferencePresenter;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ShowReferenceAction extends Action {

    private final ShowReferencePresenter showReferencePresenter;
    private final AppContext             appContext;

    @Inject
    public ShowReferenceAction(CoreLocalizationConstant locale,
                               ShowReferencePresenter showReferencePresenter,
                               AppContext appContext) {
        super(locale.showReference());

        this.showReferencePresenter = showReferencePresenter;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent event) {
        final Resource[] resources = appContext.getResources();

        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(resources != null && resources.length == 1);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent event) {
        final Resource resource = appContext.getResource();

        checkState(resource != null, "Null resource occurred");

        showReferencePresenter.show(resource);
    }
}
