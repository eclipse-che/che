/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.upload.file;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link UploadFileView}.
 *
 * @author Roman Nikitenko.
 */
public class UploadFileViewImpl extends Window implements UploadFileView {

  public interface UploadFileViewBinder extends UiBinder<Widget, UploadFileViewImpl> {}

  private final AgentURLModifier agentURLModifier;

  Button btnCancel;
  Button btnUpload;

  @UiField FormPanel submitForm;
  @UiField CheckBox overwrite;
  @UiField FlowPanel uploadPanel;

  FileUpload file;
  ActionDelegate delegate;

  /** Create view. */
  @Inject
  public UploadFileViewImpl(
      UploadFileViewBinder uploadFileViewBinder,
      CoreLocalizationConstant locale,
      AgentURLModifier agentURLModifier) {

    this.agentURLModifier = agentURLModifier;
    setTitle(locale.uploadFileTitle());
    setWidget(uploadFileViewBinder.createAndBindUi(this));
    bind();

    btnUpload =
        createButton(
            locale.uploadButton(),
            "file-uploadFile-upload",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onUploadClicked();
              }
            });

    btnUpload.addStyleName(resources.windowCss().primaryButton());
    btnUpload.addStyleName(resources.windowCss().buttonAlignRight());
    addButtonToFooter(btnUpload);

    btnCancel =
        createButton(
            locale.cancel(),
            "file-uploadFile-cancel",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
              }
            });
    btnCancel.addStyleName(resources.windowCss().buttonAlignRight());
    addButtonToFooter(btnCancel);
  }

  /** Bind handlers. */
  private void bind() {
    submitForm.addSubmitCompleteHandler(
        new FormPanel.SubmitCompleteHandler() {
          @Override
          public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
            delegate.onSubmitComplete(event.getResults());
          }
        });
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    addFile();
    this.show();
  }

  /** {@inheritDoc} */
  @Override
  public void closeDialog() {
    this.hide();
    this.onClose();
    btnUpload.setEnabled(false);
    overwrite.setValue(false);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void setEnabledUploadButton(boolean enabled) {
    btnUpload.setEnabled(enabled);
  }

  /** {@inheritDoc} */
  @Override
  public void setEncoding(@NotNull String encodingType) {
    submitForm.setEncoding(encodingType);
  }

  /** {@inheritDoc} */
  @Override
  public void setAction(@NotNull String url) {
    submitForm.setAction(agentURLModifier.modify(url));
    submitForm.setMethod(FormPanel.METHOD_POST);
  }

  /** {@inheritDoc} */
  @Override
  public void submit() {
    overwrite.setFormValue(overwrite.getValue().toString());
    submitForm.submit();
    btnUpload.setEnabled(false);
  }

  /** {@inheritDoc} */
  @Override
  @NotNull
  public String getFileName() {
    String fileName = file.getFilename();
    if (fileName.contains("/")) {
      return fileName.substring(fileName.lastIndexOf("/") + 1);
    }
    if (fileName.contains("\\")) {
      return fileName.substring(fileName.lastIndexOf("\\") + 1);
    }
    return fileName;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isOverwriteFileSelected() {
    return overwrite.getValue();
  }

  /** {@inheritDoc} */
  @Override
  protected void onClose() {
    uploadPanel.remove(file);
    super.onClose();
  }

  private void addFile() {
    file = new FileUpload();
    file.setHeight("22px");
    file.setWidth("100%");
    file.setName("file");
    file.ensureDebugId("file-uploadFile-ChooseFile");
    file.addChangeHandler(
        new ChangeHandler() {
          @Override
          public void onChange(ChangeEvent event) {
            delegate.onFileNameChanged();
          }
        });
    uploadPanel.insert(file, 0);
  }
}
