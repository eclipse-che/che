/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.keybinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.api.keybinding.Scheme;
import org.eclipse.che.ide.api.keybinding.SchemeImpl;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a> */
@RunWith(GwtMockitoTestRunner.class)
public class KeyBindingManagerTest {

  protected KeyBindingManager keyManager;
  protected Scheme testScheme;

  @Before
  public void setUp() {
    keyManager = new KeyBindingManager(null);
    testScheme = new SchemeImpl("org.eclipse.che.test.scheme", "Sample Description");
  }

  @Test
  public void testSchemeRegistration() {
    assertNull(keyManager.getScheme(testScheme.getSchemeId()));
    keyManager.addScheme(testScheme);
    assertNotNull(keyManager.getScheme(testScheme.getSchemeId()));
  }

  @Test
  public void testGlobalSchemeFallback() {
    String actionId = "fallback";
    keyManager.getGlobal().addKey(mock(CharCodeWithModifiers.class), actionId);
    keyManager.addScheme(testScheme);
    assertTrue(keyManager.getGlobal().contains(actionId));

    // Assert Global scheme is responding to the action
    keyManager.setActive(keyManager.getGlobal().getSchemeId());
    assertNotNull(keyManager.getKeyBinding(actionId));

    // Set TestScheme as active, and assert action is still registered
    keyManager.setActive(testScheme.getSchemeId());
    assertFalse(testScheme.contains(actionId));
    assertNotNull(keyManager.getKeyBinding(actionId));
  }

  @Test
  public void testKeyBindingGetter() {
    assertEquals(2, keyManager.getSchemes().size());
    String actionId = "testAction1";
    assertNull(keyManager.getKeyBinding(actionId));

    testScheme.addKey(mock(CharCodeWithModifiers.class), actionId);
    // Action should not be handled yet - scheme not added / selected
    assertNull(keyManager.getKeyBinding(actionId));

    keyManager.addScheme(testScheme);
    // Action should not be handled yet - scheme not selected
    assertNull(keyManager.getKeyBinding(actionId));

    keyManager.setActive(testScheme.getSchemeId());
    assertNotNull(keyManager.getKeyBinding(actionId));
  }

  @Test
  public void testActionContainsCheck() {
    String actionId = "testAction2";
    assertFalse(testScheme.contains(actionId));
    testScheme.addKey(mock(CharCodeWithModifiers.class), actionId);
    assertTrue(testScheme.contains(actionId));
  }
}
