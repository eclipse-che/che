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
package org.eclipse.che.ide.upload.file;

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

    this.setTitle(locale.uploadFileTitle());
    setWidget(uploadFileViewBinder.createAndBindUi(this));
    bind();

    btnCancel =
        addFooterButton(
            locale.cancel(), "file-uploadFile-cancel", event -> delegate.onCancelClicked());

    btnUpload =
        addFooterButton(
            locale.uploadButton(),
            "file-uploadFile-upload",
            event -> delegate.onUploadClicked(),
            true);
    this.agentURLModifier = agentURLModifier;
  }

  /** Bind handlers. */
  private void bind() {
    submitForm.addSubmitCompleteHandler(event -> delegate.onSubmitComplete(event.getResults()));
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    show();
  }

  @Override
  protected void onShow() {
    addFile();
  }

  /** {@inheritDoc} */
  @Override
  public void closeDialog() {
    hide();
  }

  @Override
  protected void onHide() {
    btnUpload.setEnabled(false);
    overwrite.setValue(false);
    uploadPanel.remove(file);
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

  private void addFile() {
    file = new FileUpload();
    file.setHeight("22px");
    file.setWidth("100%");
    file.setName("file");
    file.ensureDebugId("file-uploadFile-ChooseFile");
    file.addChangeHandler(event -> delegate.onFileNameChanged());
    uploadPanel.insert(file, 0);
  }
}
