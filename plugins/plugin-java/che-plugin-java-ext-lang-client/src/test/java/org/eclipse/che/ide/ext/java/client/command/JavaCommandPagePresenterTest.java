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
package org.eclipse.che.ide.ext.java.client.command;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.ext.java.client.command.mainclass.SelectNodePresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Valeriy Svydenko */
@RunWith(MockitoJUnitRunner.class)
public class JavaCommandPagePresenterTest {

  private static final String MAIN_CLASS_PATH = "src/Main.java";
  private static final String COMMAND_LINE =
      "cd ${current.project.path} &&"
          + "javac -classpath ${project.java.classpath}"
          + "-sourcepath ${project.java.sourcepath} -d ${project"
          + ".java.output.dir} src/Main.java &&"
          + "java -classpath ${project.java.classpath}${project.java.output.dir} Main";

  @Mock private JavaCommandPageView view;
  @Mock private SelectNodePresenter selectNodePresenter;

  @Mock private CommandImpl command;
  @Mock private CommandPage.FieldStateActionDelegate fieldStateDelegate;

  @InjectMocks private JavaCommandPagePresenter presenter;

  @Before
  public void setUp() throws Exception {
    when(command.getCommandLine()).thenReturn(COMMAND_LINE);
  }

  @Test
  public void delegateShouldBeSet() throws Exception {
    verify(view).setDelegate(presenter);
  }

  @Test
  public void pageShouldBeInitialized() throws Exception {
    AcceptsOneWidget container = mock(AcceptsOneWidget.class);

    presenter.resetFrom(command);
    presenter.setFieldStateActionDelegate(fieldStateDelegate);
    presenter.go(container);

    verify(container).setWidget(view);
    verify(view).setMainClass(MAIN_CLASS_PATH);
    verify(view).setCommandLine(COMMAND_LINE);
    verify(fieldStateDelegate).updatePreviewURLState(false);
  }

  @Test
  public void selectedNodeWindowShouldBeShowed() throws Exception {
    presenter.onAddMainClassBtnClicked();

    verify(selectNodePresenter).show(presenter);
  }

  @Test
  public void pageIsNotDirty() throws Exception {
    presenter.resetFrom(command);
    assertFalse(presenter.isDirty());
  }
}
