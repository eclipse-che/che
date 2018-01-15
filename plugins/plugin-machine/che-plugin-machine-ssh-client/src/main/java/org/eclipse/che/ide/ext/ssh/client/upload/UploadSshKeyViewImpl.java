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
package org.eclipse.che.ide.ext.ssh.client.upload;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ext.ssh.client.SshLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link UploadSshKeyView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class UploadSshKeyViewImpl extends Window implements UploadSshKeyView {
  interface UploadSshKeyViewImplUiBinder extends UiBinder<Widget, UploadSshKeyViewImpl> {}

  private static UploadSshKeyViewImplUiBinder ourUiBinder =
      GWT.create(UploadSshKeyViewImplUiBinder.class);

  @UiField Label message;
  @UiField TextBox host;
  @UiField FormPanel uploadForm;

  @UiField(provided = true)
  final SshLocalizationConstant locale;

  @UiField FileUpload file;
  Button btnCancel;
  Button btnUpload;

  private ActionDelegate delegate;

  @Inject
  public UploadSshKeyViewImpl(SshLocalizationConstant locale, CoreLocalizationConstant coreLocale) {
    this.locale = locale;

    Widget widget = ourUiBinder.createAndBindUi(this);

    this.setTitle(locale.uploadSshKeyViewTitle());
    this.setWidget(widget);

    uploadForm.addSubmitCompleteHandler(
        new FormPanel.SubmitCompleteHandler() {
          @Override
          public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
            delegate.onSubmitComplete(event.getResults());
          }
        });

    file.addChangeHandler(
        new ChangeHandler() {
          @Override
          public void onChange(ChangeEvent event) {
            delegate.onFileNameChanged();
          }
        });

    btnCancel =
        createButton(
            coreLocale.cancelButton(),
            "sshKeys-cancel",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
              }
            });
    addButtonToFooter(btnCancel);

    btnUpload =
        createButton(
            coreLocale.uploadButton(),
            "sshKeys-upload",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onUploadClicked();
              }
            });
    addButtonToFooter(btnUpload);
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getHost() {
    return host.getText();
  }

  /** {@inheritDoc} */
  @Override
  public void setHost(@NotNull String host) {
    this.host.setText(host);
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getFileName() {
    return file.getFilename();
  }

  /** {@inheritDoc} */
  @Override
  public void setEnabledUploadButton(boolean enabled) {
    btnUpload.setEnabled(enabled);
  }

  /** {@inheritDoc} */
  @Override
  public void setMessage(@NotNull String message) {
    this.message.setText(message);
  }

  /** {@inheritDoc} */
  @Override
  public void setEncoding(@NotNull String encodingType) {
    uploadForm.setEncoding(encodingType);
  }

  /** {@inheritDoc} */
  @Override
  public void setAction(@NotNull String url) {
    uploadForm.setAction(url);
    uploadForm.setMethod(FormPanel.METHOD_POST);
  }

  /** {@inheritDoc} */
  @Override
  public void submit() {
    uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
    uploadForm.submit();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    uploadForm.reset();
    this.show();
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    this.hide();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  protected void onClose() {
    hide();
  }
}
