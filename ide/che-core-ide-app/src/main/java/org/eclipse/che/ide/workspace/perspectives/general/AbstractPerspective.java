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
package org.eclipse.che.ide.workspace.perspectives.general;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.workspace.PartStackPresenterFactory;
import org.eclipse.che.ide.workspace.PartStackViewFactory;
import org.eclipse.che.ide.workspace.WorkBenchControllerFactory;
import org.eclipse.che.ide.workspace.WorkBenchPartController;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;
import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.BELOW;
import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.LEFT;
import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.RIGHT;

/**
 * The class which contains general business logic for all perspectives.
 *
 * @author Dmitry Shnurenko
 */
public abstract class AbstractPerspective implements Presenter, Perspective, ActivePartChangedHandler {

    protected final Map<PartStackType, PartStack> partStacks;
    protected final PerspectiveViewImpl           view;

    private final String                  perspectiveId;
    private final WorkBenchPartController leftPartController;
    private final WorkBenchPartController rightPartController;
    private final WorkBenchPartController belowPartController;

    private double        leftPartSize;
    private double        rightPartSize;
    private double        belowPartSize;
    private PartPresenter activePart;
    private PartPresenter activePartBeforeChangePerspective;

    protected AbstractPerspective(@NotNull String perspectiveId,
                                  @NotNull PerspectiveViewImpl view,
                                  @NotNull PartStackPresenterFactory stackPresenterFactory,
                                  @NotNull PartStackViewFactory partViewFactory,
                                  @NotNull WorkBenchControllerFactory controllerFactory,
                                  @NotNull EventBus eventBus) {
        this.view = view;
        this.perspectiveId = perspectiveId;
        this.partStacks = new HashMap<>();

        PartStackView navigationView = partViewFactory.create(LEFT, view.getLeftPanel());

        leftPartController = controllerFactory.createController(view.getSplitPanel(), view.getNavigationPanel());
        PartStack navigationPartStack = stackPresenterFactory.create(navigationView, leftPartController);
        partStacks.put(NAVIGATION, navigationPartStack);

        PartStackView informationView = partViewFactory.create(BELOW, view.getBottomPanel());

        belowPartController = controllerFactory.createController(view.getSplitPanel(), view.getInformationPanel());
        PartStack informationStack = stackPresenterFactory.create(informationView, belowPartController);
        partStacks.put(INFORMATION, informationStack);

        PartStackView toolingView = partViewFactory.create(RIGHT, view.getRightPanel());

        rightPartController = controllerFactory.createController(view.getSplitPanel(), view.getToolPanel());
        PartStack toolingPartStack = stackPresenterFactory.create(toolingView, rightPartController);
        partStacks.put(TOOLING, toolingPartStack);

        /* Makes splitters much better */
        view.tuneSplitters();

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    /**
     * Opens previous active tab on current perspective.
     *
     * @param partStackType
     *         part type on which need open previous active part
     */
    protected void openActivePart(@NotNull PartStackType partStackType) {
        PartStack partStack = partStacks.get(partStackType);

        partStack.openPreviousActivePart();
    }

    @Override
    public void storeState() {
        activePartBeforeChangePerspective = activePart;

        if (activePartBeforeChangePerspective != null) {
            activePartBeforeChangePerspective.storeState();
        }
    }

    @Override
    public void restoreState() {
        if (activePartBeforeChangePerspective != null) {
            setActivePart(activePartBeforeChangePerspective);

            activePartBeforeChangePerspective.restoreState();
        }
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        activePart = event.getActivePart();
    }

    /** {@inheritDoc} */
    @Override
    public void removePart(@NotNull PartPresenter part) {
        PartStack destPartStack = findPartStackByPart(part);
        if (destPartStack != null) {
            destPartStack.removePart(part);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void hidePart(@NotNull PartPresenter part) {
        PartStack destPartStack = findPartStackByPart(part);
        if (destPartStack != null) {
            destPartStack.hidePart(part);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void maximizeCentralPart() {
        leftPartSize = leftPartController.getSize();
        rightPartSize = rightPartController.getSize();
        belowPartSize = belowPartController.getSize();

        leftPartController.setHidden(true);
        rightPartController.setHidden(true);
        belowPartController.setHidden(true);
    }

    /** {@inheritDoc} */
    @Override
    public void maximizeBottomPart() {
        leftPartSize = leftPartController.getSize();
        rightPartSize = rightPartController.getSize();
        belowPartSize = belowPartController.getSize();

        leftPartController.setHidden(true);
        rightPartController.setHidden(true);
        belowPartController.maximize();
    }

    /** {@inheritDoc} */
    @Override
    public void restoreParts() {
        leftPartController.setSize(leftPartSize);
        rightPartController.setSize(rightPartSize);
        belowPartController.setSize(belowPartSize);
    }

    /** {@inheritDoc} */
    @Override
    public void setActivePart(@NotNull PartPresenter part) {
        PartStack destPartStack = findPartStackByPart(part);
        if (destPartStack != null) {
            destPartStack.setActivePart(part);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setActivePart(@NotNull PartPresenter part, @NotNull PartStackType type) {
        PartStack destPartStack = partStacks.get(type);
        destPartStack.setActivePart(part);
    }

    /**
     * Find parent PartStack for given Part
     *
     * @param part
     *         part for which need find parent
     * @return Parent PartStackPresenter or null if part not registered
     */
    public PartStack findPartStackByPart(@NotNull PartPresenter part) {
        for (PartStackType partStackType : PartStackType.values()) {

            if (partStacks.get(partStackType).containsPart(part)) {
                return partStacks.get(partStackType);
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part, @NotNull PartStackType type) {
        addPart(part, type, null);
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part, @NotNull PartStackType type, @Nullable Constraints constraint) {
        PartStack destPartStack = partStacks.get(type);

        List<String> rules = part.getRules();

        if (rules.isEmpty() && !destPartStack.containsPart(part)) {
            destPartStack.addPart(part, constraint);

            return;
        }

        if (rules.contains(perspectiveId)) {
            destPartStack.addPart(part, constraint);
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public PartStack getPartStack(@NotNull PartStackType type) {
        return partStacks.get(type);
    }

}
