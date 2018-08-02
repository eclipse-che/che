/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.imageviewer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorAgent.OpenEditorCallback;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.editor.events.FileEvent.FileEventHandler;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Is used for displaying images in editor area.
 *
 * @author Ann Shumilova
 */
public class ImageViewer extends AbstractEditorPresenter implements FileEventHandler {

  private ImageViewerResources resources;
  private CoreLocalizationConstant constant;
  private DialogFactory dialogFactory;
  private WorkspaceAgent workspaceAgent;
  private AgentURLModifier urlModifier;
  private ScrollPanel editorView;

  @Inject
  public ImageViewer(
      ImageViewerResources resources,
      CoreLocalizationConstant constant,
      DialogFactory dialogFactory,
      EventBus eventBus,
      WorkspaceAgent workspaceAgent,
      AgentURLModifier urlModifier) {
    this.resources = resources;
    this.constant = constant;
    this.dialogFactory = dialogFactory;
    this.workspaceAgent = workspaceAgent;
    this.urlModifier = urlModifier;

    resources.imageViewerCss().ensureInjected();

    eventBus.addHandler(FileEvent.TYPE, this);
  }

  /** {@inheritDoc} */
  @Override
  public void doSave() {}

  @Override
  public void doSave(AsyncCallback<EditorInput> callback) {}

  /** {@inheritDoc} */
  @Override
  public void doSaveAs() {}

  /** {@inheritDoc} */
  @Override
  public void activate() {}

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getTitle() {
    return input.getName();
  }

  /** {@inheritDoc} */
  @Override
  public SVGResource getTitleImage() {
    return input.getSVGResource();
  }

  /** {@inheritDoc} */
  @Override
  public String getTitleToolTip() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void onClose(@NotNull final AsyncCallback<Void> callback) {
    if (isDirty()) {
      dialogFactory
          .createConfirmDialog(
              constant.askWindowCloseTitle(),
              constant.messagesSaveChanges(getEditorInput().getName()),
              new ConfirmCallback() {
                @Override
                public void accepted() {
                  doSave();
                  handleClose();
                  callback.onSuccess(null);
                }
              },
              new CancelCallback() {
                @Override
                public void cancelled() {
                  handleClose();
                  callback.onSuccess(null);
                }
              })
          .show();
    } else {
      handleClose();
      callback.onSuccess(null);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    VerticalPanel panel = new VerticalPanel();
    panel.setSize("100%", "100%");
    panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    panel.add(getImage());
    editorView = new ScrollPanel(panel);
    editorView.getElement().getFirstChildElement().getStyle().setHeight(100, Unit.PCT);
    container.setWidget(editorView);
  }

  /**
   * Image to display file with image type.
   *
   * @return {@link Image}
   */
  private Image getImage() {
    String contentLink = urlModifier.modify(input.getFile().getContentUrl());
    Image image = (contentLink != null) ? new Image(contentLink) : new Image();
    image.setStyleName(resources.imageViewerCss().imageViewer());
    return image;
  }

  /** {@inheritDoc} */
  @Override
  protected void initializeEditor(OpenEditorCallback callback) {
    callback.onEditorOpened(this);
  }

  /** {@inheritDoc} */
  @Override
  public void close(final boolean save) {
    // nothing to do
  }

  /** {@inheritDoc} */
  @Override
  public IsWidget getView() {
    return editorView;
  }

  /** {@inheritDoc} */
  @Override
  public void onFileOperation(FileEvent event) {
    if (event.getOperationType() != FileEvent.FileOperation.CLOSE) {
      return;
    }

    final Path eventFilePath = event.getFile().getLocation();
    final Path filePath = input.getFile().getLocation();
    if (filePath.equals(eventFilePath)) {
      workspaceAgent.removePart(this);
    }
  }
}
