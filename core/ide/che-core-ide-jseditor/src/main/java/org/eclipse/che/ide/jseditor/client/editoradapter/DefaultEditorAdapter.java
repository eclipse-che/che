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
package org.eclipse.che.ide.jseditor.client.editoradapter;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorInitException;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.texteditor.UndoableEditor;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.jseditor.client.keymap.Keybinding;
import org.eclipse.che.ide.jseditor.client.text.LinearRange;
import org.eclipse.che.ide.jseditor.client.text.TextPosition;
import org.eclipse.che.ide.jseditor.client.text.TextRange;
import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link EditorAdapter}.
 */
public class DefaultEditorAdapter extends Composite implements EditorAdapter, FileEventHandler, RequiresResize, UndoableEditor {

    /** The UI binder instance. */
    private static final DefaultEditorAdapterUiBinder UIBINDER = GWT.create(DefaultEditorAdapterUiBinder.class);

    /** The text editor part of the nested presenter. */
    private ConfigurableTextEditor textEditor;

    /** The nested presenter. */
    private NestablePresenter nestedPresenter;

    /** The property listeners. */
    private List<PropertyListener> propertylisteners;

    /** The editor input. */
    private EditorInput input;

    /** The workspace agent. */
    private WorkspaceAgent workspaceAgent;

    /** The panel of the component. */
    @UiField
    SimplePanel panel = new SimplePanel();

    public DefaultEditorAdapter(EventBus eventBus, final WorkspaceAgent workspaceAgent) {
        initWidget(UIBINDER.createAndBindUi(this));

        eventBus.addHandler(FileEvent.TYPE, this);

        this.workspaceAgent = workspaceAgent;
    }

    @Override
    public void close(final boolean save) {
        this.textEditor.close(save);
    }

    @Override
    public boolean isEditable() {
        return this.textEditor.isEditable();
    }

    @Override
    public void doRevertToSaved() {
        this.textEditor.doRevertToSaved();
    }

    @Override
    public Document getDocument() {
        return this.textEditor.getDocument();
    }

    @Override
    public String getContentType() {
        return this.textEditor.getContentType();
    }

    @Override
    public TextRange getSelectedTextRange() {
        return this.textEditor.getSelectedTextRange();
    }

    @Override
    public LinearRange getSelectedLinearRange() {
        return this.textEditor.getSelectedLinearRange();
    }

    @Override
    public TextPosition getCursorPosition() {
        return this.textEditor.getCursorPosition();
    }

    @Override
    public int getCursorOffset() {
        return this.textEditor.getCursorOffset();
    }

    @Override
    public void showMessage(final String message) {
        this.textEditor.showMessage(message);
    }

    @Override
    public boolean isFocused() {
        return this.textEditor.isFocused();
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus() {
        textEditor.setFocus();
    }

    @Override
    public void init(final EditorInput input) throws EditorInitException {
        this.textEditor.init(input);
        this.input = input;
    }

    @Override
    public EditorInput getEditorInput() {
        return this.textEditor.getEditorInput();
    }

    @Override
    public void doSave() {
        this.textEditor.doSave();
    }

    @Override
    public void doSave(final AsyncCallback<EditorInput> callback) {
        this.textEditor.doSave(callback);
    }

    @Override
    public void doSaveAs() {
        this.textEditor.doSaveAs();
    }

    @Override
    public void onFileChanged() {
        this.textEditor.onFileChanged();
    }

    @Override
    public boolean isDirty() {
        return this.textEditor.isDirty();
    }

    @Override
    public void addCloseHandler(final EditorPartCloseHandler closeHandler) {
        this.textEditor.addCloseHandler(closeHandler);
    }

    @Override
    public void activate() {
        this.textEditor.activate();
    }

    @Override
    public String getTitle() {
        return this.textEditor.getTitle();
    }

    @Override
    public void addRule(@NotNull String perspectiveId) {
        throw new UnsupportedOperationException("The method isn't available in this class " + getClass());
    }

    @Override
    public List<String> getRules() {
        throw new UnsupportedOperationException("The method isn't available in this class " + getClass());
    }

    @Override
    public IsWidget getView() {
        return this;
    }

    @Override
    public ImageResource getTitleImage() {
        return this.textEditor.getTitleImage();
    }

    @Override
    public SVGResource getTitleSVGImage() {
        return this.textEditor.getTitleSVGImage();
    }

    @Override
    public SVGImage decorateIcon(final SVGImage svgImage) {
        return this.textEditor.decorateIcon(svgImage);
    }

    @Override
    public IsWidget getTitleWidget() {
        return this.textEditor.getTitleWidget();
    }

    @Override
    public int getUnreadNotificationsCount() {
        return 0;
    }

    @Override
    public String getTitleToolTip() {
        return this.textEditor.getTitleToolTip();
    }

    @Override
    public int getSize() {
        return this.textEditor.getSize();
    }

    @Override
    public void onOpen() {
        this.textEditor.onOpen();
    }

    @Override
    public void onClose(final AsyncCallback<Void> callback) {
        // the nested presenter is responsible for closing the editor _it owns_
        this.nestedPresenter.onClose(callback);
    }

    @Override
    public Selection<?> getSelection() {
        return this.textEditor.getSelection();
    }

    @Override
    public void addPropertyListener(final PropertyListener listener) {
        if (this.propertylisteners == null) {
            this.propertylisteners = new ArrayList<>();
            this.textEditor.addPropertyListener(new PropertyListener() {
                @Override
                public void propertyChanged(final PartPresenter source, final int propId) {
                    for (final PropertyListener listener : propertylisteners) {
                        listener.propertyChanged(DefaultEditorAdapter.this, propId);
                    }
                }
            });
        }
        this.propertylisteners.add(listener);
    }

    @Override
    public void removePropertyListener(final PropertyListener listener) {
        if (this.propertylisteners != null) {
            this.propertylisteners.remove(listener);
        }
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        this.nestedPresenter.go(this.panel);
        container.setWidget(this.panel);
    }

    @Override
    public void initialize(final TextEditorConfiguration configuration, final NotificationManager notificationManager) {
        this.textEditor.initialize(configuration, notificationManager);
    }

    @Override
    public TextEditorConfiguration getConfiguration() {
        return this.textEditor.getConfiguration();
    }

    @Override
    public ConfigurableTextEditor getTextEditor() {
        return this.textEditor;
    }

    @Override
    public void setPresenter(final NestablePresenter nestedPresenter) {
        this.nestedPresenter = nestedPresenter;
    }

    public void setTextEditor(final ConfigurableTextEditor configurable) {
        this.textEditor = configurable;
    }

    @Override
    public void onResize() {
        final Widget widget = this.panel.getWidget();
        if (widget instanceof RequiresResize) {
            ((RequiresResize)widget).onResize();
        }
    }

    @Override
    public void addKeybinding(final Keybinding keybinding) {
        this.textEditor.addKeybinding(keybinding);
    }

    @Override
    public HandlesUndoRedo getUndoRedo() {
        if (this.textEditor instanceof UndoableEditor) {
            return ((UndoableEditor)this.textEditor).getUndoRedo();
        } else {
            return new DummyHandlesUndoRedo();
        }
    }

    @Override
    public void onFileOperation(FileEvent event) {
        if (event.getOperationType() != FileEvent.FileOperation.CLOSE) {
            return;
        }

        final String eventFilePath = event.getFile().getPath();
        final String filePath = input.getFile().getPath();
        if (filePath.equals(eventFilePath)) {
            workspaceAgent.removePart(this);
        }
    }

    /** Interface for this component's UIBinder. */
    interface DefaultEditorAdapterUiBinder extends UiBinder<SimplePanel, Composite> {
    }

}
