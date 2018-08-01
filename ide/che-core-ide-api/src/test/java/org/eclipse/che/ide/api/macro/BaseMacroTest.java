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
package org.eclipse.che.ide.api.macro;

import static org.junit.Assert.assertSame;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for the {@link BaseMacro}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class BaseMacroTest {

  public static final String NAME = "name";
  public static final String VALUE = "value";
  public static final String DESCRIPTION = "description";

  private BaseMacro macro;

  @Before
  public void init() throws Exception {
    macro = new BaseMacro(NAME, VALUE, DESCRIPTION);
  }

  @Test
  public void getKey() throws Exception {
    assertSame(macro.getName(), NAME);
  }

  @Test
  public void getValue() throws Exception {
    macro
        .expand()
        .then(
            value -> {
              assertSame(value, VALUE);
            });
  }

  @Test
  public void getDescription() throws Exception {
    assertSame(macro.getDescription(), DESCRIPTION);
  }
}
