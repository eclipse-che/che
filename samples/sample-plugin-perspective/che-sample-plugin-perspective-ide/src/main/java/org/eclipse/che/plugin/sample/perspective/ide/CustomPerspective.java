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
package org.eclipse.che.plugin.sample.perspective.ide;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.workspace.PartStackPresenterFactory;
import org.eclipse.che.ide.workspace.PartStackViewFactory;
import org.eclipse.che.ide.workspace.WorkBenchControllerFactory;
import org.eclipse.che.ide.workspace.perspectives.general.AbstractPerspective;
import org.eclipse.che.ide.workspace.perspectives.general.PerspectiveViewImpl;
import org.eclipse.che.providers.DynaProvider;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;
import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;

/**
 *
 */
@Singleton
public class CustomPerspective extends AbstractPerspective {

    public final static String OPERATIONS_PERSPECTIVE_ID = "Operations Perspective";

    @Inject
    public CustomPerspective(PerspectiveViewImpl view,
                             PartStackViewFactory partViewFactory,
                             SamplePresenter samplePresenter,
                             NavigationPresenter navigationPresenter,
                             InformationPresenter informationPresenter,
                             WorkBenchControllerFactory controllerFactory,
                             PartStackPresenterFactory stackPresenterFactory,
                             EventBus eventBus,
                             DynaProvider dynaProvider) {
        super(OPERATIONS_PERSPECTIVE_ID, view, stackPresenterFactory, partViewFactory, controllerFactory, eventBus, dynaProvider);
        //central panel
        partStacks.put(EDITING, samplePresenter);

        addPart(navigationPresenter, NAVIGATION);
        addPart(informationPresenter, INFORMATION);
        PartStack navigation = getPartStack(NAVIGATION);
        PartStack editing = getPartStack(EDITING);
        PartStack information = getPartStack(INFORMATION);

        if (navigation == null || editing == null) {
            return;
        }

        navigation.go(view.getNavigationPanel());
        editing.go(view.getEditorPanel());
        information.go(view.getInformationPanel());
        openActivePart(EDITING);
        openActivePart(NAVIGATION);

    }

    /** {@inheritDoc} */
    @Override
    public void go(@NotNull AcceptsOneWidget container) {
        container.setWidget(view);
    }

}
