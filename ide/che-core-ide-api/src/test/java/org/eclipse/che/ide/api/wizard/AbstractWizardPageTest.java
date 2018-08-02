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
package org.eclipse.che.ide.api.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Testing {@link AbstractWizardPage}.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractWizardPageTest {
  private AbstractWizardPage<String> wizardPage;

  @Before
  public void setUp() {
    wizardPage = new DummyWizardPage();
  }

  @Test
  public void shouldInitPage() throws Exception {
    String dataObject = "dataObject";
    wizardPage.init(dataObject);
    assertEquals(dataObject, wizardPage.dataObject);
  }

  @Test
  public void shouldSetContext() throws Exception {
    Map<String, String> context = new HashMap<>();
    wizardPage.setContext(context);
    assertEquals(context, wizardPage.context);
  }

  @Test
  public void shouldSetUpdateDelegate() throws Exception {
    Wizard.UpdateDelegate updateDelegate = mock(Wizard.UpdateDelegate.class);
    wizardPage.setUpdateDelegate(updateDelegate);
    assertEquals(updateDelegate, wizardPage.updateDelegate);
  }

  @Test
  public void shouldNotSkipped() throws Exception {
    assertFalse(wizardPage.canSkip());
  }

  @Test
  public void shouldBeCompleted() throws Exception {
    assertTrue(wizardPage.isCompleted());
  }

  private class DummyWizardPage extends AbstractWizardPage<String> {
    @Override
    public void go(AcceptsOneWidget container) {
      // do nothing
    }
  }
}
