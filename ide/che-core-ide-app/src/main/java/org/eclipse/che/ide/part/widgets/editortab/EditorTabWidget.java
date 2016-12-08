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
package org.eclipse.che.ide.part.widgets.editortab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEvent.FileEventHandler;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.PartStackView.TabPosition;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.part.editor.EditorTabContextMenuFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.UUID;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

/**
 * Editor tab widget. Contains icon, title and close mark.
 * May be pinned. Pin state indicates whether this tab should be skipped during operation "Close All but Pinned".
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 * @author Vitaliy Guliy
 * @author Vlad Zhukovskyi
 */
public class EditorTabWidget extends Composite implements EditorTab, ContextMenuHandler, ResourceChangedHandler, FileEventHandler {

    interface EditorTabWidgetUiBinder extends UiBinder<Widget, EditorTabWidget> {
    }

    private static final EditorTabWidgetUiBinder UI_BINDER = GWT.create(EditorTabWidgetUiBinder.class);

    @UiField
    SimplePanel iconPanel;

    @UiField
    Label title;

    @UiField
    FlowPanel closeButton;

    @UiField(provided = true)
    final PartStackUIResources resources;

    private final EventBus                    eventBus;
    private final EditorTabContextMenuFactory editorTabContextMenu;
    private final FileTypeRegistry            fileTypeRegistry;
    private final String                      id;
    private final EditorPartPresenter         relatedEditorPart;
    private final EditorPartStack             relatedEditorPartStack;

    private ActionDelegate delegate;
    private SVGResource    icon;
    private boolean        pinned;
    private VirtualFile    file;

    @Inject
    public EditorTabWidget(@Assisted EditorPartPresenter relatedEditorPart,
                           @Assisted EditorPartStack relatedEditorPartStack,
                           PartStackUIResources resources,
                           EditorTabContextMenuFactory editorTabContextMenu,
                           final EventBus eventBus,
                           FileTypeRegistry fileTypeRegistry) {
        this.resources = resources;
        this.eventBus = eventBus;
        this.fileTypeRegistry = fileTypeRegistry;
        this.relatedEditorPart = relatedEditorPart;
        this.relatedEditorPartStack = relatedEditorPartStack;

        initWidget(UI_BINDER.createAndBindUi(this));

        this.editorTabContextMenu = editorTabContextMenu;
        this.file = relatedEditorPart.getEditorInput().getFile();
        this.icon = relatedEditorPart.getTitleImage();
        this.title.setText(relatedEditorPart.getTitle());
        this.id = title + UUID.uuid(4);

        iconPanel.add(getIcon());

        addDomHandler(this, ClickEvent.getType());
        addDomHandler(this, DoubleClickEvent.getType());
        addDomHandler(this, ContextMenuEvent.getType());

        eventBus.addHandler(ResourceChangedEvent.getType(), this);
        eventBus.addHandler(FileEvent.TYPE, this);

        closeButton.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(FileEvent.createCloseFileEvent(EditorTabWidget.this));
            }
        }, ClickEvent.getType());
    }

    /** {@inheritDoc} */
    @Override
    public Widget getIcon() {
        return new SVGImage(icon);
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getTitle() {
        return title.getText();
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public IsWidget getView() {
        return this.asWidget();
    }

    /** {@inheritDoc} */
    @Override
    public void update(@NotNull PartPresenter part) {
        title.setText(part.getTitle());

        if (part instanceof EditorPartPresenter) {
            file = ((EditorPartPresenter)part).getEditorInput().getFile();
            FileType fileType = fileTypeRegistry.getFileTypeByFile(file);
            icon = fileType.getImage();
            iconPanel.setWidget(getIcon());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void select() {
        /** Marks tab is focused */
        getElement().setAttribute("focused", "");
    }

    /** {@inheritDoc} */
    @Override
    public void unSelect() {
        /** Marks tab is not focused */
        getElement().removeAttribute("focused");
    }

    /** {@inheritDoc} */
    @Override
    public void setTabPosition(@NotNull TabPosition tabPosition) {
        throw new UnsupportedOperationException("This method doesn't allow in this class " + getClass());
    }

    /** {@inheritDoc} */
    @Override
    public void setErrorMark(boolean isVisible) {
        if (isVisible) {
            title.addStyleName(resources.partStackCss().lineError());
        } else {
            title.removeStyleName(resources.partStackCss().lineError());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setWarningMark(boolean isVisible) {
        if (isVisible) {
            title.addStyleName(resources.partStackCss().lineWarning());
        } else {
            title.removeStyleName(resources.partStackCss().lineWarning());
        }
    }

    @Override
    public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(@NotNull ClickEvent event) {
        if (NativeEvent.BUTTON_LEFT == event.getNativeButton()) {
            delegate.onTabClicked(this);
        } else if (NativeEvent.BUTTON_MIDDLE == event.getNativeButton()) {
            eventBus.fireEvent(FileEvent.createCloseFileEvent(this));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onContextMenu(ContextMenuEvent event) {
        //construct for each editor tab own context menu,
        //that will have store information about selected virtual file and pin state at first step
        //in future maybe we should create another mechanism to associate context menu with initial dto's
        editorTabContextMenu.newContextMenu(this, relatedEditorPart, relatedEditorPartStack)
                            .show(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
    }

    /** {@inheritDoc} */
    @Override
    public void onDoubleClick(@NotNull DoubleClickEvent event) {
        delegate.onTabDoubleClicked(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void setReadOnlyMark(boolean isVisible) {
        if (isVisible) {
            getElement().setAttribute("readonly", "");
        } else {
            getElement().removeAttribute("readonly");
        }
    }

    /** {@inheritDoc} */
    @Override
    public VirtualFile getFile() {
        return file;
    }

    @Override
    public void setFile(VirtualFile file) {
        this.file = file;
    }

    @Override
    public EditorPartPresenter getRelativeEditorPart() {
        return relatedEditorPart;
    }

    /** {@inheritDoc} */
    @Override
    public void setPinMark(boolean pinned) {
        this.pinned = pinned;

        if (pinned) {
            getElement().setAttribute("pinned", "");
        } else {
            getElement().removeAttribute("pinned");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPinned() {
        return pinned;
    }

    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();

        if (delta.getKind() == ADDED) {
            if (!delta.getResource().isFile() || (delta.getFlags() & (MOVED_FROM | MOVED_TO)) == 0) {
                return;
            }

            final Resource resource = delta.getResource();
            final Path movedFrom = delta.getFromPath();

            if (file.getLocation().equals(movedFrom)) {
                file = (VirtualFile)resource;
                title.setText(file.getDisplayName());
            }
        } else if (delta.getKind() == UPDATED) {
            if (!delta.getResource().isFile()) {
                return;
            }

            final Resource resource = delta.getResource();

            if (file.getLocation().equals(resource.getLocation())) {
                file = (VirtualFile)resource;

                title.setText(file.getDisplayName());
            }
        }
    }

    @Override
    public void onFileOperation(FileEvent event) {
        if (event.getOperationType() == CLOSE && this.equals(event.getEditorTab())) {
            delegate.onTabClose(this);
        }
    }

}
