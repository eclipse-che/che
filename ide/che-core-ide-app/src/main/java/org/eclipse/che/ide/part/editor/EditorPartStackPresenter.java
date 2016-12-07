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
package org.eclipse.che.ide.part.editor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithErrors;
import org.eclipse.che.ide.api.editor.EditorWithErrors.EditorState;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.parts.base.MaximizePartEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.part.PartsComparator;
import org.eclipse.che.ide.part.editor.actions.CloseAllTabsPaneAction;
import org.eclipse.che.ide.part.editor.actions.ClosePaneAction;
import org.eclipse.che.ide.part.editor.event.CloseNonPinnedEditorsEvent;
import org.eclipse.che.ide.part.editor.event.CloseNonPinnedEditorsEvent.CloseNonPinnedEditorsHandler;
import org.eclipse.che.ide.part.widgets.TabItemFactory;
import org.eclipse.che.ide.part.widgets.panemenu.EditorPaneMenu;
import org.eclipse.che.ide.part.widgets.panemenu.EditorPaneMenuItem;
import org.eclipse.che.ide.part.widgets.panemenu.EditorPaneMenuItemFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.filter;
import static org.eclipse.che.ide.actions.EditorActions.SPLIT_HORIZONTALLY;
import static org.eclipse.che.ide.actions.EditorActions.SPLIT_VERTICALLY;
import static org.eclipse.che.ide.api.editor.EditorWithErrors.EditorState.ERROR;
import static org.eclipse.che.ide.api.editor.EditorWithErrors.EditorState.WARNING;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.part.editor.actions.EditorAbstractAction.CURRENT_FILE_PROP;
import static org.eclipse.che.ide.part.editor.actions.EditorAbstractAction.CURRENT_PANE_PROP;
import static org.eclipse.che.ide.part.editor.actions.EditorAbstractAction.CURRENT_TAB_PROP;

/**
 * EditorPartStackPresenter is a special PartStackPresenter that is shared among all
 * Perspectives and used to display Editors.
 *
 * @author Nikolay Zamosenchuk
 * @author Stéphane Daviet
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 * @author Roman Nikitenko
 */
public class EditorPartStackPresenter extends PartStackPresenter implements EditorPartStack,
                                                                            EditorTab.ActionDelegate,
                                                                            CloseNonPinnedEditorsHandler,
                                                                            ResourceChangedHandler {
    private final PresentationFactory              presentationFactory;
    private final EditorPaneMenuItemFactory        editorPaneMenuItemFactory;
    private final EventBus                         eventBus;
    private final EditorPaneMenu                   editorPaneMenu;
    private final ActionManager                    actionManager;
    private final ClosePaneAction                  closePaneAction;
    private final CloseAllTabsPaneAction           closeAllTabsPaneAction;
    private final Map<EditorPaneMenuItem, TabItem> items;

    //this list need to save order of added parts
    private final LinkedList<EditorPartPresenter> partsOrder;
    private final LinkedList<EditorPartPresenter> closedParts;

    private HandlerRegistration       closeNonPinnedEditorsHandler;
    private HandlerRegistration       resourceChangeHandler;

    @VisibleForTesting
    PaneMenuActionItemHandler paneMenuActionItemHandler;
    @VisibleForTesting
    PaneMenuTabItemHandler    paneMenuTabItemHandler;

    @Inject
    public EditorPartStackPresenter(EditorPartStackView view,
                                    PartsComparator partsComparator,
                                    EditorPaneMenuItemFactory editorPaneMenuItemFactory,
                                    PresentationFactory presentationFactory,
                                    EventBus eventBus,
                                    TabItemFactory tabItemFactory,
                                    PartStackEventHandler partStackEventHandler,
                                    EditorPaneMenu editorPaneMenu,
                                    ActionManager actionManager,
                                    ClosePaneAction closePaneAction,
                                    CloseAllTabsPaneAction closeAllTabsPaneAction) {
        super(eventBus, partStackEventHandler, tabItemFactory, partsComparator, view, null);
        this.editorPaneMenuItemFactory = editorPaneMenuItemFactory;
        this.eventBus = eventBus;
        this.presentationFactory = presentationFactory;
        this.editorPaneMenu = editorPaneMenu;
        this.actionManager = actionManager;
        this.closePaneAction = closePaneAction;
        this.closeAllTabsPaneAction = closeAllTabsPaneAction;
        this.view.setDelegate(this);
        this.items = new HashMap<>();
        this.partsOrder = new LinkedList<>();
        this.closedParts = new LinkedList<>();

        initializePaneMenu();
        view.addPaneMenuButton(editorPaneMenu);
    }

    private void initializePaneMenu() {
        paneMenuTabItemHandler = new PaneMenuTabItemHandler();
        paneMenuActionItemHandler = new PaneMenuActionItemHandler();

        final EditorPaneMenuItem<Action> closePaneItemWidget = editorPaneMenuItemFactory.createMenuItem(closePaneAction);
        closePaneItemWidget.setDelegate(paneMenuActionItemHandler);
        editorPaneMenu.addItem(closePaneItemWidget);

        final EditorPaneMenuItem<Action> closeAllTabsItemWidget = editorPaneMenuItemFactory.createMenuItem(closeAllTabsPaneAction);
        closeAllTabsItemWidget.setDelegate(paneMenuActionItemHandler);
        editorPaneMenu.addItem(closeAllTabsItemWidget, true);

        final Action splitHorizontallyAction = actionManager.getAction(SPLIT_HORIZONTALLY);
        final EditorPaneMenuItem<Action> splitHorizontallyItemWidget = editorPaneMenuItemFactory.createMenuItem(splitHorizontallyAction);
        splitHorizontallyItemWidget.setDelegate(paneMenuActionItemHandler);
        editorPaneMenu.addItem(splitHorizontallyItemWidget);

        final Action splitVerticallyAction = actionManager.getAction(SPLIT_VERTICALLY);
        final EditorPaneMenuItem<Action> splitVerticallyItemWidget = editorPaneMenuItemFactory.createMenuItem(splitVerticallyAction);
        splitVerticallyItemWidget.setDelegate(paneMenuActionItemHandler);
        editorPaneMenu.addItem(splitVerticallyItemWidget);
    }

    @Nullable
    private EditorPaneMenuItem getPaneMenuItemByTab(@NotNull TabItem tabItem) {
        for (Entry<EditorPaneMenuItem, TabItem> entry : items.entrySet()) {
            if (tabItem.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public List<EditorPartPresenter> getParts() {
        return new ArrayList<>(partsOrder);
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus(boolean focused) {
        view.setFocus(focused);
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part, Constraints constraint) {
        addPart(part);
    }

    /** {@inheritDoc} */
    @Override
    public void addPart(@NotNull PartPresenter part) {
        checkArgument(part instanceof AbstractEditorPresenter, "Can not add part " + part.getTitle() + " to editor part stack");

        EditorPartPresenter editorPart = (AbstractEditorPresenter)part;
        if (containsPart(editorPart)) {
            setActivePart(editorPart);
            return;
        }

        VirtualFile file = editorPart.getEditorInput().getFile();
        checkArgument(file != null, "File doesn't provided");

        addHandlers();
        updateListClosedParts(file);

        editorPart.addPropertyListener(propertyListener);

        final EditorTab editorTab = tabItemFactory.createEditorPartButton(editorPart, this);

        editorPart.addPropertyListener(new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                if (propId == EditorPartPresenter.PROP_INPUT && source instanceof EditorPartPresenter) {
                    editorTab.setReadOnlyMark(((EditorPartPresenter)source).getEditorInput().getFile().isReadOnly());
                }
            }
        });

        editorTab.setDelegate(this);

        parts.put(editorTab, editorPart);
        partsOrder.add(editorPart);

        view.addTab(editorTab, editorPart);

        TabItem tabItem = getTabByPart(editorPart);

        if (tabItem != null) {
            final EditorPaneMenuItem<TabItem> item = editorPaneMenuItemFactory.createMenuItem(tabItem);
            item.setDelegate(paneMenuTabItemHandler);
            editorPaneMenu.addItem(item);
            items.put(item, tabItem);
        }

        if (editorPart instanceof EditorWithErrors) {
            final EditorWithErrors presenter = ((EditorWithErrors)editorPart);

            editorPart.addPropertyListener(new PropertyListener() {
                @Override
                public void propertyChanged(PartPresenter source, int propId) {
                    EditorState editorState = presenter.getErrorState();

                    editorTab.setErrorMark(ERROR.equals(editorState));
                    editorTab.setWarningMark(WARNING.equals(editorState));
                }
            });
        }
        view.selectTab(editorPart);
    }

    private void updateListClosedParts(VirtualFile file) {
        if (closedParts.isEmpty()) {
            return;
        }

        for (EditorPartPresenter closedEditorPart : closedParts) {
            Path path = closedEditorPart.getEditorInput().getFile().getLocation();
            if (path.equals(file.getLocation())) {
                closedParts.remove(closedEditorPart);
                return;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public PartPresenter getActivePart() {
        return activePart;
    }

    /** {@inheritDoc} */
    @Override
    public void setActivePart(@NotNull PartPresenter part) {
        activePart = part;
        view.selectTab(part);
    }

    /** {@inheritDoc} */
    @Override
    public void onTabClicked(@NotNull TabItem tab) {
        activePart = parts.get(tab);
        view.selectTab(parts.get(tab));
    }

    @Override
    public void onTabDoubleClicked(@NotNull TabItem tab) {
        eventBus.fireEvent(new MaximizePartEvent(parts.get(tab)));
    }

    /** {@inheritDoc} */
    @Override
    public void removePart(PartPresenter part) {
        super.removePart(part);
        partsOrder.remove(part);
        activePart = partsOrder.isEmpty() ? null : partsOrder.getLast();

        if (activePart != null) {
            onRequestFocus();
        }

        if (parts.isEmpty()) {
            removeHandlers();
        }
    }

    @Override
    public void openPreviousActivePart() {
        if (activePart != null) {
            view.selectTab(activePart);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onTabClose(@NotNull TabItem tab) {
        final EditorPaneMenuItem editorPaneMenuItem = getPaneMenuItemByTab(tab);
        editorPaneMenu.removeItem(editorPaneMenuItem);
        items.remove(editorPaneMenuItem);

        EditorPartPresenter part = ((EditorTab)tab).getRelativeEditorPart();
        closedParts.add(part);
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseNonPinnedEditors(CloseNonPinnedEditorsEvent event) {
        EditorPartPresenter editorPart = event.getEditorTab().getRelativeEditorPart();
        if (!containsPart(editorPart)) {
            return;
        }

        Iterable<TabItem> nonPinned = filter(parts.keySet(), new Predicate<TabItem>() {
            @Override
            public boolean apply(@Nullable TabItem input) {
                return input instanceof EditorTab && !((EditorTab)input).isPinned();
            }
        });

        for (final TabItem tabItem : nonPinned) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    eventBus.fireEvent(FileEvent.createCloseFileEvent((EditorTab)tabItem));
                }
            });
        }
    }

    @Override
    public EditorPartPresenter getPartByTabId(@NotNull String tabId) {
        for (TabItem tab : parts.keySet()) {
            EditorTab currentTab = (EditorTab)tab;
            if (currentTab.getId().equals(tabId)) {
                return (EditorPartPresenter)parts.get(currentTab);
            }
        }
        return null;
    }

    @Nullable
    public EditorTab getTabByPart(@NotNull EditorPartPresenter part) {
        for (Map.Entry<TabItem, PartPresenter> entry : parts.entrySet()) {
            PartPresenter currentPart = entry.getValue();
            if (part.equals(currentPart)) {
                return (EditorTab)entry.getKey();
            }
        }
        return null;
    }

    @Nullable
    @Override
    public EditorTab getTabByPath(@NotNull Path path) {
        for (TabItem tab : parts.keySet()) {
            EditorTab editorTab = (EditorTab)tab;
            Path currentPath = editorTab.getFile().getLocation();

            if (currentPath.equals(path)) {
                return editorTab;
            }
        }
        return null;
    }

    @Nullable
    public PartPresenter getPartByPath(Path path) {
        for (TabItem tab : parts.keySet()) {
            EditorTab editorTab = (EditorTab)tab;
            Path currentPath = editorTab.getFile().getLocation();

            if (currentPath.equals(path)) {
                return parts.get(tab);
            }
        }
        return null;
    }

    @Override
    public EditorPartPresenter getNextFor(EditorPartPresenter editorPart) {
        int indexForNext = partsOrder.indexOf(editorPart) + 1;
        return indexForNext >= partsOrder.size() ? partsOrder.getFirst() : partsOrder.get(indexForNext);
    }

    @Override
    public EditorPartPresenter getPreviousFor(EditorPartPresenter editorPart) {
        int indexForNext = partsOrder.indexOf(editorPart) - 1;
        return indexForNext < 0 ? partsOrder.getLast() : partsOrder.get(indexForNext);
    }

    @Nullable
    @Override
    public EditorPartPresenter getLastClosed() {
        if (closedParts.isEmpty()) {
            return null;
        }
        return closedParts.getLast();
    }

    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();
        if (delta.getKind() != REMOVED) {
            return;
        }

        Path resourcePath = delta.getResource().getLocation();
        for (EditorPartPresenter editorPart : closedParts) {
            Path editorPath = editorPart.getEditorInput().getFile().getLocation();
            if (editorPath.equals(resourcePath)) {
                closedParts.remove(editorPart);
                return;
            }
        }
    }

    private void addHandlers() {
        if (closeNonPinnedEditorsHandler == null) {
            closeNonPinnedEditorsHandler = eventBus.addHandler(CloseNonPinnedEditorsEvent.getType(), this);
        }

        if (resourceChangeHandler == null) {
            resourceChangeHandler = eventBus.addHandler(ResourceChangedEvent.getType(), this);
        }
    }

    private void removeHandlers() {
        if (resourceChangeHandler != null) {
            resourceChangeHandler.removeHandler();
            resourceChangeHandler = null;
        }

        if (closeNonPinnedEditorsHandler != null) {
            closeNonPinnedEditorsHandler.removeHandler();
            closeNonPinnedEditorsHandler = null;
        }
    }

    @VisibleForTesting
    protected class PaneMenuActionItemHandler implements EditorPaneMenuItem.ActionDelegate<Action> {

        @Override
        public void onItemClicked(@NotNull EditorPaneMenuItem<Action> item) {
            editorPaneMenu.hide();

            final Action action = item.getData();
            final Presentation presentation = presentationFactory.getPresentation(action);
            presentation.putClientProperty(CURRENT_PANE_PROP, EditorPartStackPresenter.this);

            final PartPresenter activePart = getActivePart();
            final TabItem tab = getTabByPart(activePart);

            if (tab != null) {
                final VirtualFile virtualFile = ((EditorTab)tab).getFile();
                //pass into action file property and editor tab
                presentation.putClientProperty(CURRENT_TAB_PROP, tab);
                presentation.putClientProperty(CURRENT_FILE_PROP, virtualFile);
            }
            action.actionPerformed(new ActionEvent(presentation, actionManager, null));
        }

        @Override
        public void onCloseButtonClicked(@NotNull EditorPaneMenuItem<Action> item) {}
    }

    @VisibleForTesting
    protected class PaneMenuTabItemHandler implements EditorPaneMenuItem.ActionDelegate<TabItem> {

        @Override
        public void onItemClicked(@NotNull EditorPaneMenuItem<TabItem> item) {
            editorPaneMenu.hide();

            final TabItem tabItem = item.getData();
            activePart = parts.get(tabItem);
            view.selectTab(activePart);
        }

        @Override
        public void onCloseButtonClicked(@NotNull EditorPaneMenuItem<TabItem> item) {
            editorPaneMenu.hide();

            final TabItem tabItem = item.getData();
            if (tabItem instanceof EditorTab) {
                EditorTab editorTab = (EditorTab)tabItem;
                eventBus.fireEvent(FileEvent.createCloseFileEvent(editorTab));
            }
        }
    }
}
