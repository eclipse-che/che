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
package org.eclipse.che.ide.command.editor.page.project;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ProjectSwitcher}. */
@RunWith(GwtMockitoTestRunner.class)
public class ProjectSwitcherTest {

  private static final String PROJECT_NAME = "p1";

  private ProjectSwitcher switcher;

  @Before
  public void setUp() throws Exception {
    switcher = new ProjectSwitcher(PROJECT_NAME);
  }

  @Test
  public void shouldSetLabel() throws Exception {
    verify(switcher.label).setText(PROJECT_NAME);
  }

  @Test
  public void shouldReturnValue() throws Exception {
    switcher.getValue();

    verify(switcher.switcher).getValue();
  }

  @Test
  public void shouldSetValue() throws Exception {
    switcher.setValue(true);

    verify(switcher.switcher).setValue(Boolean.TRUE);
  }

  @Test
  public void shouldSetValueAndFireEvents() throws Exception {
    switcher.setValue(true, true);

    verify(switcher.switcher).setValue(Boolean.TRUE, true);
  }

  @Test
  public void shouldAddValueChangeHandler() throws Exception {
    ValueChangeHandler valueChangeHandler = mock(ValueChangeHandler.class);
    switcher.addValueChangeHandler(valueChangeHandler);

    verify(switcher.switcher).addValueChangeHandler(valueChangeHandler);
  }
}
