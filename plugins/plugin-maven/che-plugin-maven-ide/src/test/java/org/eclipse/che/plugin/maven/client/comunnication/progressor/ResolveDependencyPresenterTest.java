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
package org.eclipse.che.plugin.maven.client.comunnication.progressor;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Valeriy Svydenko */
@RunWith(MockitoJUnitRunner.class)
public class ResolveDependencyPresenterTest {
  private static final String TEXT = "to be or not to be";

  @Mock private ResolveDependencyView view;

  @InjectMocks private ResolveDependencyPresenter presenter;

  @Test
  public void viewShouldBeShowed() throws Exception {
    presenter.show();

    verify(view).showDialog();
  }

  @Test
  public void progressLabelShouldBeUpdated() throws Exception {
    presenter.setProgressLabel(TEXT);

    verify(view).setOperationLabel(TEXT);
  }

  @Test
  public void progressorShouldBeUpdated() throws Exception {
    presenter.updateProgressBar(5);

    verify(view).updateProgressBar(5);
  }

  @Test
  public void viewShouldBeHidden() throws Exception {
    presenter.hide();

    verify(view).close();
  }
}
