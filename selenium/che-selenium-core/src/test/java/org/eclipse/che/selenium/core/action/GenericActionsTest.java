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
 * Unit tests for the {@link GenericActions}.
 *
 * @author Vlad Zhukovskyi
 */
@Listeners(value = MockitoTestNGListener.class)
public class GenericActionsTest {

  private WebDriver webDriver;

  @BeforeMethod
  public void setUp() throws Exception {
    webDriver =
        Mockito.mock(
            WebDriver.class, Mockito.withSettings().extraInterfaces(HasInputDevices.class));
  }

  @Test
  public void testShouldReturnSameCharSequence() throws Exception {
    GenericActions actions = new GenericActions(webDriver);

    final CharSequence[] charSequences =
        actions.modifyCharSequence(Keys.END, Keys.HOME, Keys.PAGE_DOWN, Keys.PAGE_UP);

    assertNotNull(charSequences);
    assertEquals(charSequences.length, 4);

    assertEquals(charSequences[0], Keys.END);
    assertEquals(charSequences[1], Keys.HOME);
    assertEquals(charSequences[2], Keys.PAGE_DOWN);
    assertEquals(charSequences[3], Keys.PAGE_UP);
  }
}
