/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.tutorial.wysiwyg.editor;

import javax.annotation.Nonnull;

import org.vectomatic.dom.svg.ui.SVGResource;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RichTextArea;

/** @author Evgen Vidolob */
public class WysiwygEditor extends AbstractEditorPresenter {

    private ProjectServiceClient projectServiceClient;
    private final DialogFactory dialogFactory;
    private RichTextArea textArea;

    public WysiwygEditor(ProjectServiceClient projectServiceClient, DialogFactory dialogFactory) {
        this.projectServiceClient = projectServiceClient;
        this.dialogFactory = dialogFactory;
    }

    /** {@inheritDoc} */
    @Override
    protected void initializeEditor() {
        // create editor
        textArea = new RichTextArea();

        projectServiceClient.getFileContent(input.getFile().getPath(), new AsyncRequestCallback<String>(new StringUnmarshaller()) {
            @Override
            protected void onSuccess(String result) {
                textArea.setHTML(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(WysiwygEditor.class, exception);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void doSave() {
    }

    @Override
    public void doSave(AsyncCallback<EditorInput> callback) {
    }

    /** {@inheritDoc} */
    @Override
    public void doSaveAs() {
    }

    /** {@inheritDoc} */
    @Override
    public void activate() {
        textArea.setFocus(true);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getTitle() {
        return "WYSIWYG Editor: " + input.getFile().getName();
    }

    /** {@inheritDoc} */
    @Override
    public ImageResource getTitleImage() {
        return input.getImageResource();
    }

    /** {@inheritDoc} */
    @Override
    public SVGResource getTitleSVGImage() {
        return input.getSVGResource();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitleToolTip() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void onClose(@Nonnull final AsyncCallback<Void> callback) {
        if (isDirty()) {
            dialogFactory.createConfirmDialog(
                                              "Close", "'" + getEditorInput().getName() + "' has been modified. Save changes?",
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
                                              }).show();
        } else {
            handleClose();
            callback.onSuccess(null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        textArea.setSize("100%", "100%");
        RichTextToolbar toolbar = new RichTextToolbar(textArea);

        DockLayoutPanel panel = new DockLayoutPanel(Style.Unit.PX);
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.addNorth(toolbar, 60);
        panel.add(textArea);

        // Add the components to a panel
        container.setWidget(panel);
    }

    @Override
    public void close(boolean save) {
    }
}
