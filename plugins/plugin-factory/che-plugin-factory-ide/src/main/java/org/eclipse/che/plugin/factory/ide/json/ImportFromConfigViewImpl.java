/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.factory.ide.json;


import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.factory.ide.FactoryLocalizationConstant;

/**
 * The implementation of {@link ImportFromConfigView}.
 *
 * @author Sergii Leschenko
 */
public class ImportFromConfigViewImpl extends Window implements ImportFromConfigView {

    private static final int MAX_FILE_SIZE = 3;//Mb

    public interface ImportFromConfigViewBinder extends UiBinder<Widget, ImportFromConfigViewImpl> {

    }

    @UiField
    FormPanel uploadForm;
    @UiField
    Label     errorMessage;
    FileUpload fileUpload;

    private ActionDelegate delegate;

    private String fileContent;

    private final Button buttonImport;

    @Inject
    public ImportFromConfigViewImpl(ImportFromConfigViewBinder importFromConfigViewBinder,
                                    FactoryLocalizationConstant locale) {
        this.setTitle(locale.importFromConfigurationTitle());
        setWidget(importFromConfigViewBinder.createAndBindUi(this));

        Button btnCancel = createButton(locale.cancelButton(), "import-from-config-btn-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        buttonImport = createButton(locale.importButton(), "import-from-config-btn-import", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onImportClicked();
            }
        });
        addButtonToFooter(buttonImport);
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        errorMessage.setText("");
        fileContent = null;
        fileUpload = new FileUpload();
        fileUpload.setHeight("22px");
        fileUpload.setWidth("100%");
        fileUpload.setName("file");
        fileUpload.ensureDebugId("import-from-config-ChooseFile");
        addHandler(fileUpload.getElement());

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                buttonImport.setEnabled(fileUpload.getFilename() != null);
            }
        });

        uploadForm.add(fileUpload);

        this.show();
    }

    /** {@inheritDoc} */
    @Override
    public void closeDialog() {
        hide();
        onClose();
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

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        uploadForm.remove(fileUpload);
        fileUpload = null;
    }

    private native void addHandler(Element element) /*-{
        var instance = this;

        function readFileContent(evt) {
            // Check for the various File API support.
            if (!window.File || !window.FileReader || !window.FileList || !window.Blob) {
                instance.@com.codenvy.ide.factory.client.json.ImportFromConfigViewImpl::onError(Ljava/lang/String;)
                ('The File APIs are not fully supported in this browser.');
                return;
            }

            var selectedFile = evt.target.files[0];

            var max_size = @com.codenvy.ide.factory.client.json.ImportFromConfigViewImpl::MAX_FILE_SIZE;

            if (selectedFile.size > max_size * 100000) {
                instance.@org.eclipse.che.plugin.factory.ide.json.ImportFromConfigViewImpl::resetUploadFileField()();
                instance.@com.codenvy.ide.factory.client.json.ImportFromConfigViewImpl::setErrorMessageOnForm(Ljava/lang/String;)
                ('File size exceeds the limit ' + max_size + 'mb');
                return;
            }

            var reader = new FileReader();
            reader.onload = function () {
                //reseting error message
                instance.@com.codenvy.ide.factory.client.json.ImportFromConfigViewImpl::setErrorMessageOnForm(Ljava/lang/String;)('');
                //getting file's content
                instance.@com.codenvy.ide.factory.client.json.ImportFromConfigViewImpl::fileContent = reader.result;
            };

            reader.onerror = function (event) {
                instance.@com.codenvy.ide.factory.client.json.ImportFromConfigViewImpl::onError(Ljava/lang/String;)
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

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                buttonImport.setEnabled(fileUpload.getFilename() != null);
            }
        });
        uploadForm.add(fileUpload);
    }

    private void setErrorMessageOnForm(String msg) {
        errorMessage.setText(msg);
    }

    private void onError(String message) {
        delegate.onErrorReadingFile(message);
    }
}
