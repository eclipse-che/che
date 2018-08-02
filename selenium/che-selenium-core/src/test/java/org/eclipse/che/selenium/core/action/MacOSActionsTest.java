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
package org.eclipse.che.selenium.core.action;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.HasInputDevices;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Unit tests for the {@link MacOSActions}.
 *
 * @author Vlad Zhukovskyi
 */
@Listeners(value = MockitoTestNGListener.class)
public class MacOSActionsTest {

  private WebDriver webDriver;

  @BeforeMethod
  public void setUp() throws Exception {
    webDriver =
        Mockito.mock(
            WebDriver.class, Mockito.withSettings().extraInterfaces(HasInputDevices.class));
  }

  @Test
  public void testShouldReplaceEndCharSequence() throws Exception {
    MacOSActions actions = new MacOSActions(webDriver);

    final CharSequence[] charSequences = actions.modifyCharSequence(Keys.ESCAPE, Keys.END);

    assertNotNull(charSequences);
    assertEquals(charSequences.length, 2);

    assertEquals(charSequences[0], Keys.ESCAPE);
    assertEquals(charSequences[1], Keys.chord(Keys.COMMAND, Keys.RIGHT));
  }

  @Test
  public void testShouldReplaceHomeCharSequence() throws Exception {
    MacOSActions actions = new MacOSActions(webDriver);

    final CharSequence[] charSequences = actions.modifyCharSequence(Keys.ESCAPE, Keys.HOME);

    assertNotNull(charSequences);
    assertEquals(charSequences.length, 2);

    assertEquals(charSequences[0], Keys.ESCAPE);
    assertEquals(charSequences[1], Keys.chord(Keys.COMMAND, Keys.LEFT));
  }

  @Test
  public void testShouldReplacePageDownCharSequence() throws Exception {
    MacOSActions actions = new MacOSActions(webDriver);

    final CharSequence[] charSequences = actions.modifyCharSequence(Keys.ESCAPE, Keys.PAGE_DOWN);

    assertNotNull(charSequences);
    assertEquals(charSequences.length, 2);

    assertEquals(charSequences[0], Keys.ESCAPE);
    assertEquals(charSequences[1], Keys.chord(Keys.COMMAND, Keys.DOWN));
  }

  @Test
  public void testShouldReplacePageUpCharSequence() throws Exception {
    MacOSActions actions = new MacOSActions(webDriver);

    final CharSequence[] charSequences = actions.modifyCharSequence(Keys.ESCAPE, Keys.PAGE_UP);

    assertNotNull(charSequences);
    assertEquals(charSequences.length, 2);

    assertEquals(charSequences[0], Keys.ESCAPE);
    assertEquals(charSequences[1], Keys.chord(Keys.COMMAND, Keys.UP));
  }

  @Test
  public void testShouldNotReplaceAnyCharSequence() throws Exception {
    MacOSActions actions = new MacOSActions(webDriver);

    final CharSequence[] charSequences = actions.modifyCharSequence(Keys.ESCAPE, Keys.ENTER);

    assertNotNull(charSequences);
    assertEquals(charSequences.length, 2);

    assertEquals(charSequences[0], Keys.ESCAPE);
    assertEquals(charSequences[1], Keys.ENTER);
  }
}
