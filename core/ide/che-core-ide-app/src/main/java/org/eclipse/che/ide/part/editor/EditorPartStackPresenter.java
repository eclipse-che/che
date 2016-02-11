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

import com.google.common.base.Predicate;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithErrors;
import org.eclipse.che.ide.api.editor.EditorWithErrors.EditorState;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.client.inject.factories.TabItemFactory;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.part.PartsComparator;
import org.eclipse.che.ide.part.editor.event.CloseNonPinnedEditorsEvent;
import org.eclipse.che.ide.part.editor.event.CloseNonPinnedEditorsEvent.CloseNonPinnedEditorsHandler;
import org.eclipse.che.ide.part.editor.event.PinEditorTabEvent;
import org.eclipse.che.ide.part.editor.event.PinEditorTabEvent.PinEditorTabEventHandler;
import org.eclipse.che.ide.part.widgets.editortab.EditorTab;
import org.eclipse.che.ide.part.widgets.listtab.ListButton;
import org.eclipse.che.ide.part.widgets.listtab.ListItem;
import org.eclipse.che.ide.part.widgets.listtab.ListItemWidget;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Iterables.filter;
import static org.eclipse.che.ide.api.editor.EditorWithErrors.EditorState.ERROR;
import static org.eclipse.che.ide.api.editor.EditorWithErrors.EditorState.WARNING;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;

/**
 * EditorPartStackPresenter is a special PartStackPresenter that is shared among all
 * Perspectives and used to display Editors.
 *
 * @author Nikolay Zamosenchuk
 * @author Stéphane Daviet
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class EditorPartStackPresenter extends PartStackPresenter implements EditorPartStack,
                                                                            EditorTab.ActionDelegate,
                                                                            ListButton.ActionDelegate,
                                                                            PinEditorTabEventHandler,
                                                                            CloseNonPinnedEditorsHandler {

    private final EventBus   eventBus;
    private final ListButton listButton;

    private final Map<ListItem, TabItem> items;

    //this list need to save order of added parts
    private final LinkedList<PartPresenter> partsOrder;

    private PartPresenter activePart;

    @Inject
    public EditorPartStackPresenter(EditorPartStackView view,
                                    PartsComparator partsComparator,
                                    EventBus eventBus,
                                    TabItemFactory tabItemFactory,
                                    PartStackEventHandler partStackEventHandler,
                                    ListButton listButton) {
        //noinspection ConstantConditions
        super(eventBus, partStackEventHandler, tabItemFactory, partsComparator, view, null);
        this.eventBus = eventBus;

        this.listButton = listButton;
        this.listButton.setDelegate(this);

        this.view.setDelegate(this);
        view.setListButton(listButton);

        this.items = new HashMap<>();
        this.partsOrder = new LinkedList<>();

        eventBus.addHandler(PinEditorTabEvent.getType(), this);
        eventBus.addHandler(CloseNonPinnedEditorsEvent.getType(), this);
    }

    private void removeItemFromList(@NotNull TabItem tab) {
        ListItem listItem = getListItemByTab(tab);

        if (listItem != null) {
            listButton.removeListItem(listItem);
            items.remove(listItem);
        }
    }

    @Nullable
    private ListItem getListItemByTab(@NotNull TabItem tabItem) {
        for (Entry<ListItem, TabItem> entry : items.entrySet()) {
            if (tabItem.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
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
        if (!containsPart(part)) {
            part.addPropertyListener(propertyListener);

            VirtualFile file = part instanceof AbstractEditorPresenter ? ((AbstractEditorPresenter)part).getEditorInput().getFile()
                                                                       : null;

            final EditorTab editorTab = tabItemFactory.createEditorPartButton(file, part.getTitleSVGImage(), part.getTitle());

            part.addPropertyListener(new PropertyListener() {
                @Override
                public void propertyChanged(PartPresenter source, int propId) {
                    if (propId == EditorPartPresenter.PROP_INPUT && source instanceof EditorPartPresenter) {
                        editorTab.setReadOnlyMark(((EditorPartPresenter)source).getEditorInput().getFile().isReadOnly());
                    }
                }
            });

            editorTab.setDelegate(this);

            parts.put(editorTab, part);
            partsOrder.add(part);

            view.addTab(editorTab, part);

            TabItem tabItem = getTabByPart(part);

            if (tabItem != null) {
                ListItem item = new ListItemWidget(tabItem);
                listButton.addListItem(item);
                items.put(item, tabItem);
            }

            if (part instanceof EditorWithErrors) {
                final EditorWithErrors presenter = ((EditorWithErrors)part);

                part.addPropertyListener(new PropertyListener() {
                    @Override
                    public void propertyChanged(PartPresenter source, int propId) {
                        EditorState editorState = presenter.getErrorState();

                        editorTab.setErrorMark(ERROR.equals(editorState));
                        editorTab.setWarningMark(WARNING.equals(editorState));
                    }
                });
            }
        }

        view.selectTab(part);
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

    /** {@inheritDoc} */
    @Override
    public void removePart(PartPresenter part) {
        super.removePart(part);
        partsOrder.remove(part);
        activePart = partsOrder.isEmpty() ? null : partsOrder.getLast();
    }

    /** {@inheritDoc} */
    @Override
    public void onTabClose(@NotNull TabItem tab) {
        removeItemFromList(tab);
    }

    /** {@inheritDoc} */
    @Override
    public void onEditorTabPinned(PinEditorTabEvent event) {
        for (Entry<TabItem, PartPresenter> entry : parts.entrySet()) {
            if (entry.getValue() instanceof AbstractEditorPresenter) {
                AbstractEditorPresenter editor = (AbstractEditorPresenter)entry.getValue();

                if (editor.getEditorInput().getFile().equals(event.getFile())) {
                    ((EditorTab)entry.getKey()).setPinMark(event.isPin());
                    return;
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseNonPinnedEditors(CloseNonPinnedEditorsEvent event) {
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
                    eventBus.fireEvent(new FileEvent(((EditorTab)tabItem).getFile(), CLOSE));
                }
            });
        }
    }
}
