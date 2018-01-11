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

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwtmockito.GwtMockito;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing {@link UploadFileViewImpl} functionality.
 *
 * @author Roman Nikitenko.
 */
@RunWith(GwtMockitoTestRunner.class)
public class UploadFileViewImplTest {

  private UploadFileViewImpl view;

  UploadFileViewImpl.UploadFileViewBinder binder;
  CoreLocalizationConstant locale;
  AgentURLModifier agentURLModifier;

  @Before
  public void setup() {
    GwtMockito.initMocks(this);
    binder = GWT.create(UploadFileViewImpl.UploadFileViewBinder.class);
    locale = GWT.create(CoreLocalizationConstant.class);
    agentURLModifier = GWT.create(AgentURLModifier.class);
    UploadFilePresenter presenter = mock(UploadFilePresenter.class);
    view = new UploadFileViewImpl(binder, locale, agentURLModifier);
    view.setDelegate(presenter);
  }

  @Test
  public void getFileNameShouldBeExecuted() {
    view.file = mock(FileUpload.class);
    when(view.file.getFilename()).thenReturn("fileName");

    view.getFileName();

    verify(view.file).getFilename();
  }

  @Test
  public void submitShouldBeExecuted() {
    view.submitForm = mock(FormPanel.class);

    view.submit();

    verify(view.submitForm).submit();
  }

  @Test
  public void setActionShouldBeExecuted() {
    view.submitForm = mock(FormPanel.class);
    final String url = "url";
    when(agentURLModifier.modify(url)).thenReturn(url);
    view.setAction(url);

    verify(view.submitForm).setAction(eq(url));
    verify(view.submitForm).setMethod(eq(FormPanel.METHOD_POST));
  }

  @Test
  public void setEncodingShouldBeExecuted() {
    view.submitForm = mock(FormPanel.class);

    view.setEncoding(FormPanel.ENCODING_MULTIPART);

    verify(view.submitForm).setEncoding(eq(FormPanel.ENCODING_MULTIPART));
  }

  @Test
  public void setEnabledUploadButtonShouldBeExecuted() {
    view.btnUpload = mock(Button.class);

    view.setEnabledUploadButton(true);
    verify(view.btnUpload).setEnabled(eq(true));
  }

  @Test
  public void closeShouldBeExecuted() {
    view.uploadPanel = mock(FlowPanel.class);
    view.file = mock(FileUpload.class);
    view.overwrite = mock(CheckBox.class);
    view.btnUpload = mock(Button.class);

    view.closeDialog();

    verify(view.uploadPanel).remove((FileUpload) anyObject());
    verify(view.btnUpload).setEnabled(eq(false));
    verify(view.overwrite).setValue(eq(false));
  }
}
