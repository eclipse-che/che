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
package org.eclipse.che.ide.upload.folder;

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
 * The implementation of {@link UploadFolderFromZipView}.
 *
 * @author Roman Nikitenko.
 */
public class UploadFolderFromZipViewImpl extends Window implements UploadFolderFromZipView {

  public interface UploadFolderFromZipViewBinder
      extends UiBinder<Widget, UploadFolderFromZipViewImpl> {}

  private final AgentURLModifier agentURLModifier;

  Button btnCancel;
  Button btnUpload;
  FileUpload file;
  ActionDelegate delegate;
  CoreLocalizationConstant constant;

  @UiField FormPanel submitForm;
  @UiField CheckBox overwrite;
  @UiField CheckBox skipFirstLevel;
  @UiField FlowPanel uploadPanel;

  /** Create view. */
  @Inject
  public UploadFolderFromZipViewImpl(
      UploadFolderFromZipViewBinder uploadFileViewBinder,
      CoreLocalizationConstant locale,
      org.eclipse.che.ide.Resources resources,
      AgentURLModifier agentURLModifier) {
    this.constant = locale;
    this.setTitle(locale.uploadZipFolderTitle());
    setWidget(uploadFileViewBinder.createAndBindUi(this));
    bind();

    btnCancel =
        addFooterButton(
            locale.cancel(), "file-uploadFolder-cancel", event -> delegate.onCancelClicked());
    btnUpload =
        addFooterButton(
            locale.uploadButton(),
            "file-uploadFolder-upload",
            event -> delegate.onUploadClicked(),
            true);

    btnUpload.addStyleName(resources.buttonLoaderCss().buttonLoader());

    this.agentURLModifier = agentURLModifier;
  }

  /** Bind handlers. */
  private void bind() {
    submitForm.addSubmitCompleteHandler(event -> delegate.onSubmitComplete(event.getResults()));
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    addFileUploadForm();
    show();
  }

  /** {@inheritDoc} */
  @Override
  public void closeDialog() {
    hide();
    btnUpload.setEnabled(false);
    overwrite.setValue(false);
    skipFirstLevel.setValue(false);
  }

  @Override
  protected void onHide() {
    uploadPanel.remove(file);
  }

  @Override
  public void setLoaderVisibility(boolean isVisible) {
    if (isVisible) {
      btnUpload.setHTML("<i></i>");
      btnUpload.setEnabled(false);
    } else {
      btnUpload.setText(constant.uploadButton());
      btnUpload.setEnabled(true);
    }
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
    skipFirstLevel.setFormValue(skipFirstLevel.getValue().toString());
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

  private void addFileUploadForm() {
    file = new FileUpload();
    file.setHeight("22px");
    file.setWidth("100%");
    file.setName("file");
    file.ensureDebugId("file-uploadFile-ChooseFile");
    file.addChangeHandler(event -> delegate.onFileNameChanged());
    uploadPanel.insert(file, 0);
  }
}
