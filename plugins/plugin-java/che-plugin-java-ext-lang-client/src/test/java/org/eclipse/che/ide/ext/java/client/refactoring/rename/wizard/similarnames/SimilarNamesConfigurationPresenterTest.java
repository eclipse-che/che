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
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames;

import static org.mockito.Mockito.verify;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(GwtMockitoTestRunner.class)
public class SimilarNamesConfigurationPresenterTest {
  @Mock private SimilarNamesConfigurationView view;

  private SimilarNamesConfigurationPresenter presenter;

  @Before
  public void setUp() throws Exception {
    presenter = new SimilarNamesConfigurationPresenter(view);
  }

  @Test
  public void windowShouldBeShow() throws Exception {
    presenter.show();

    verify(view).showDialog();
  }

  @Test
  public void valueOfStrategyShouldBeReturned() throws Exception {
    presenter.getMachStrategy();

    verify(view).getMachStrategy();
  }
}
