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
package org.eclipse.che.ide.ext.java.client.action;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.organizeimports.OrganizeImportsPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Tests for {@link OrganizeImportsAction}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class OrganizeImportsActionTest {
  @Mock private OrganizeImportsPresenter organizeImportsPresenter;
  @Mock private EditorAgent editorAgent;
  @Mock private ActionEvent actionEvent;

  @Mock private TextEditor editor;

  @Mock(answer = Answers.RETURNS_MOCKS)
  private JavaLocalizationConstant locale;

  @InjectMocks OrganizeImportsAction action;

  @Before
  public void setUp() throws Exception {
    when(editorAgent.getActiveEditor()).thenReturn(editor);
  }

  @Test
  public void prepareAction() throws Exception {
    verify(locale).organizeImportsName();
    verify(locale).organizeImportsDescription();
  }

  @Test
  public void actionShouldBePerformed() {
    action.actionPerformed(actionEvent);

    verify(organizeImportsPresenter).organizeImports(editor);
  }

  @Test
  public void actionShouldBePerformedAsProposal() {
    action.performAsProposal();

    verify(organizeImportsPresenter).organizeImports(editor);
  }
}
