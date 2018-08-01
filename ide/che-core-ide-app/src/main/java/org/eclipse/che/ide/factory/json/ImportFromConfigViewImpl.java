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
package org.eclipse.che.ide.factory.json;

import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link ImportFromConfigView}.
 *
 * @author Sergii Leschenko
 */
public class ImportFromConfigViewImpl extends Window implements ImportFromConfigView {

  @SuppressWarnings("unused") // used in native js
  private static final int MAX_FILE_SIZE_MB = 3;

  public interface ImportFromConfigViewBinder extends UiBinder<Widget, ImportFromConfigViewImpl> {}

  @UiField FormPanel uploadForm;
  @UiField Label errorMessage;
  private FileUpload fileUpload;

  private ActionDelegate delegate;

  private String fileContent;

  private final Button buttonImport;

  @Inject
  public ImportFromConfigViewImpl(
      ImportFromConfigViewBinder importFromConfigViewBinder, CoreLocalizationConstant locale) {
    this.setTitle(locale.importFromConfigurationTitle());
    setWidget(importFromConfigViewBinder.createAndBindUi(this));

    addFooterButton(
        locale.cancelButton(),
        "import-from-config-btn-cancel",
        event -> delegate.onCancelClicked());

    buttonImport =
        addFooterButton(
            locale.importButton(),
            "import-from-config-btn-import",
            event -> delegate.onImportClicked(),
            true);

    fileUpload = new FileUpload();
    fileUpload.setHeight("22px");
    fileUpload.setWidth("100%");
    fileUpload.setName("file");
    fileUpload.ensureDebugId("import-from-config-ChooseFile");
    addHandler(fileUpload.getElement());

    fileUpload.addChangeHandler(event -> buttonImport.setEnabled(fileUpload.getFilename() != null));

    uploadForm.add(fileUpload);
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    errorMessage.setText("");
    fileContent = null;

    this.show();
  }

  /** {@inheritDoc} */
  @Override
  public void closeDialog() {
    hide();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setEnabledImportButton(boolean enabled) {
    buttonImport.setEnabled(enabled);
  }

  @Override
  public String getFileContent() {
    return fileContent;
  }

  private native void addHandler(Element element) /*-{
        var instance = this;

        function readFileContent(evt) {
            // Check for the various File API support.
            if (!window.File || !window.FileReader || !window.FileList || !window.Blob) {
                instance.@org.eclipse.che.ide.factory.json.ImportFromConfigViewImpl::onError(Ljava/lang/String;)
                ('The File APIs are not fully supported in this browser.');
                return;
            }

            var selectedFile = evt.target.files[0];

            var max_size = @org.eclipse.che.ide.factory.json.ImportFromConfigViewImpl::MAX_FILE_SIZE_MB;

            if (selectedFile.size > max_size * 100000) {
                instance.@org.eclipse.che.ide.factory.json.ImportFromConfigViewImpl::resetUploadFileField()();
                instance.@org.eclipse.che.ide.factory.json.ImportFromConfigViewImpl::setErrorMessageOnForm(Ljava/lang/String;)
                ('File size exceeds the limit ' + max_size + 'mb');
                return;
            }

            var reader = new FileReader();
            reader.onload = function () {
                //reseting error message
                instance.@org.eclipse.che.ide.factory.json.ImportFromConfigViewImpl::setErrorMessageOnForm(Ljava/lang/String;)('');
                //getting file's content
                instance.@org.eclipse.che.ide.factory.json.ImportFromConfigViewImpl::fileContent = reader.result;
            };

            reader.onerror = function (event) {
                instance.@org.eclipse.che.ide.factory.json.ImportFromConfigViewImpl::onError(Ljava/lang/String;)
                ('Error reading config file ' + event.target.error.code);
            };

            reader.readAsText(selectedFile);
        }

        element.addEventListener('change', readFileContent, false);
    }-*/;

  private void resetUploadFileField() {
    uploadForm.remove(fileUpload);
    fileUpload = new FileUpload();
    fileUpload.setHeight("22px");
    fileUpload.setWidth("100%");
    fileUpload.setName("file");
    fileUpload.ensureDebugId("import-from-config-ChooseFile");
    addHandler(fileUpload.getElement());

    fileUpload.addChangeHandler(event -> buttonImport.setEnabled(fileUpload.getFilename() != null));
    uploadForm.add(fileUpload);
  }

  private void setErrorMessageOnForm(String msg) {
    errorMessage.setText(msg);
  }

  private void onError(String message) {
    delegate.onErrorReadingFile(message);
  }
}
