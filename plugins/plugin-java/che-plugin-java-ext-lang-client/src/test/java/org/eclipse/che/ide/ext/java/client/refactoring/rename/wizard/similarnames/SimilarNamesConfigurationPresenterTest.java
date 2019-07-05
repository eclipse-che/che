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
    presenter.getMatchStrategy();

    verify(view).getMatchStrategy();
  }
}
