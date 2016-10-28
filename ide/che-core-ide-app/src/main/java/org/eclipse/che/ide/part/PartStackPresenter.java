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
package org.eclipse.che.ide.part;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.EditorDirtyStateChangedEvent;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.part.widgets.TabItemFactory;
import org.eclipse.che.ide.part.widgets.partbutton.PartButton;
import org.eclipse.che.ide.workspace.WorkBenchPartController;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements "Tab-like" UI Component, that accepts PartPresenters as child elements.
 * <p/>
 * PartStack support "focus" (please don't mix with GWT Widget's Focus feature). Focused PartStack will highlight active Part, notifying
 * user what component is currently active.
 *
 * @author Nikolay Zamosenchuk
 * @author Stéphane Daviet
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public class PartStackPresenter implements Presenter, PartStackView.ActionDelegate, PartButton.ActionDelegate, PartStack {

    /** The default size for the part. */
    private static final double DEFAULT_PART_SIZE = 260;

    /** The minimum allowable size for the part. */
    private static final int MIN_PART_SIZE = 100;

    private final WorkBenchPartController         workBenchPartController;
    private final PartsComparator                 partsComparator;
    private final Map<PartPresenter, Constraints> constraints;
    private final PartStackEventHandler           partStackHandler;

    protected final Map<TabItem, PartPresenter> parts;
    protected final TabItemFactory              tabItemFactory;
    protected final PartStackView               view;
    protected final PropertyListener            propertyListener;

    protected PartPresenter activePart;
    protected TabItem       activeTab;
    protected double        currentSize;

    @Inject
    public PartStackPresenter(final EventBus eventBus,
                              PartStackEventHandler partStackEventHandler,
                              TabItemFactory tabItemFactory,
                              PartsComparator partsComparator,
                              @Assisted final PartStackView view,
                              @Assisted @NotNull WorkBenchPartController workBenchPartController) {
        this.view = view;
        this.view.setDelegate(this);

        this.partStackHandler = partStackEventHandler;
        this.workBenchPartController = workBenchPartController;
        this.tabItemFactory = tabItemFactory;
        this.partsComparator = partsComparator;

        this.parts = new HashMap<>();
        this.constraints = new LinkedHashMap<>();

        this.propertyListener = new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                if (PartPresenter.TITLE_PROPERTY == propId) {
                    updatePartTab(source);
                } else if (EditorPartPresenter.PROP_DIRTY == propId) {
                    eventBus.fireEvent(new EditorDirtyStateChangedEvent((EditorPartPresenter)source));
                }
            }
        };

        if (workBenchPartController != null) {
            this.workBenchPartController.setSize(DEFAULT_PART_SIZE);
            this.workBenchPartController.setMinSize(MIN_PART_SIZE);
        }

        currentSize = DEFAULT_PART_SIZE;
    }

    private void updatePartTab(@NotNull PartPresenter part) {
        if (!containsPart(part)) {
            return;
        }

        view.updateTabItem(part);
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part) {
        addPart(part, null);
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part, @Nullable Constraints constraint) {
        if (containsPart(part)) {
            workBenchPartController.setHidden(true);

            TabItem selectedItem = getTabByPart(part);

            if (selectedItem != null) {
                selectedItem.unSelect();
            }

            return;
        }

        if (part instanceof BasePresenter) {
            ((BasePresenter)part).setPartStack(this);
        }

        part.addPropertyListener(propertyListener);

        PartButton partButton = tabItemFactory.createPartButton(part.getTitle())
                                              .setTooltip(part.getTitleToolTip())
                                              .setIcon(part.getTitleImage());
        partButton.setDelegate(this);

        parts.put(partButton, part);
        constraints.put(part, constraint);

        view.addTab(partButton, part);

        sortPartsOnView();

        onRequestFocus();
    }

    private void sortPartsOnView() {
        List<PartPresenter> sortedParts = new ArrayList<>();
        sortedParts.addAll(parts.values());
        partsComparator.setConstraints(constraints);

        Collections.sort(sortedParts, partsComparator);

        view.setTabPositions(sortedParts);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsPart(PartPresenter part) {
        return parts.values().contains(part);
    }

    /** {@inheritDoc} */
    @Override
    public PartPresenter getActivePart() {
        return activePart;
    }

    /** {@inheritDoc} */
    @Override
    public void setActivePart(@NotNull PartPresenter part) {
        TabItem activeTab = getTabByPart(part);

        if (activeTab == null) {
            return;
        }

        activePart = part;
        selectActiveTab(activeTab);
    }

    @Nullable
    protected TabItem getTabByPart(@NotNull PartPresenter part) {
        for (Map.Entry<TabItem, PartPresenter> entry : parts.entrySet()) {

            if (part.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void hidePart(PartPresenter part) {
        TabItem activeTab = getTabByPart(part);

        if (activeTab == null) {
            return;
        }

        this.activeTab = activeTab;

        onTabClicked(activeTab);
    }

    /** {@inheritDoc} */
    @Override
    public void removePart(PartPresenter part) {
        parts.remove(getTabByPart(part));

        view.removeTab(part);
    }

    /** {@inheritDoc} */
    @Override
    public void openPreviousActivePart() {
        if (activePart == null) {
            return;
        }

        TabItem selectedTab = getTabByPart(activePart);

        if (selectedTab != null) {
            selectActiveTab(selectedTab);
        }
    }

    @Override
    public void updateStack() {
        for (PartPresenter partPresenter : parts.values()) {
            if (partPresenter instanceof BasePresenter) {
                ((BasePresenter)partPresenter).setPartStack(this);
            }
        }
    }

    @Override
    public List<? extends PartPresenter> getParts() {
        return new ArrayList<>(constraints.keySet());
    }

    /** {@inheritDoc} */
    @Override
    public void onRequestFocus() {
        partStackHandler.onRequestFocus(PartStackPresenter.this);
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus(boolean focused) {
        view.setFocus(focused);
    }

    /** {@inheritDoc} */
    @Override
    public void onTabClicked(@NotNull TabItem selectedTab) {
        //handle somehow part close event
        if (selectedTab.equals(activeTab)) {
            selectedTab.unSelect();

            currentSize = workBenchPartController.getSize();

            workBenchPartController.setSize(0);

            activeTab = null;
            activePart = null;

            return;
        }

        activeTab = selectedTab;
        activePart = parts.get(selectedTab);
        activePart.onOpen();
        selectActiveTab(activeTab);
    }

    private void selectActiveTab(@NotNull TabItem selectedTab) {
        double partSize = workBenchPartController.getSize();
        currentSize = partSize >= MIN_PART_SIZE ? partSize : currentSize;

        workBenchPartController.setSize(currentSize);
        workBenchPartController.setHidden(false);

        PartPresenter selectedPart = parts.get(selectedTab);

        view.selectTab(selectedPart);
    }

    /** Handles PartStack actions */
    public interface PartStackEventHandler {
        /** PartStack is being clicked and requests Focus */
        void onRequestFocus(PartStack partStack);
    }
}
