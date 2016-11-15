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
package org.eclipse.che.ide.extension.machine.client.perspective;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.MachineAppliancePresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel.MachinePanelPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.RecipePartPresenter;
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
 * Special view perspective which defines how must main window be displayed when we choose machine perspective.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class OperationsPerspective extends AbstractPerspective {

    public final static String OPERATIONS_PERSPECTIVE_ID = "Operations Perspective";

    @Inject
    public OperationsPerspective(PerspectiveViewImpl view,
                                 PartStackViewFactory partViewFactory,
                                 WorkBenchControllerFactory controllerFactory,
                                 PartStackPresenterFactory stackPresenterFactory,
                                 MachinePanelPresenter machinePanel,
                                 RecipePartPresenter recipePanel,
                                 MachineAppliancePresenter infoContainer,
                                 EventBus eventBus,
                                 DynaProvider dynaProvider) {
        super(OPERATIONS_PERSPECTIVE_ID, view, stackPresenterFactory, partViewFactory, controllerFactory, eventBus, dynaProvider);

        //central panel
        partStacks.put(EDITING, infoContainer);

        addPart(machinePanel, NAVIGATION);
        addPart(recipePanel, NAVIGATION);

        setActivePart(machinePanel);
        PartStack information = getPartStack(INFORMATION);
        PartStack navigation = getPartStack(NAVIGATION);
        PartStack editing = getPartStack(EDITING);

        if (information == null || navigation == null || editing == null) {
            return;
        }

        information.updateStack();

        information.go(view.getInformationPanel());
        navigation.go(view.getNavigationPanel());
        editing.go(view.getEditorPanel());
        openActivePart(INFORMATION);
        openActivePart(NAVIGATION);
    }

    /** {@inheritDoc} */
    @Override
    public void go(@NotNull AcceptsOneWidget container) {
        container.setWidget(view);
    }

}
