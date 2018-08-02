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
package org.eclipse.che.ide.part.explorer.project.macro;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.google.common.base.Joiner;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for the {@link ExplorerCurrentFileNameMacro}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class ExplorerCurrentFileNameMacroTest extends AbstractExplorerMacroTest {

  private ExplorerCurrentFileNameMacro provider;

  @Before
  public void init() throws Exception {
    super.init();
    provider =
        new ExplorerCurrentFileNameMacro(projectExplorer, promiseProvider, localizationConstants);
  }

  @Test
  public void testGetKey() throws Exception {
    assertSame(provider.getName(), ExplorerCurrentFileNameMacro.KEY);
  }

  @Test
  public void getValue() throws Exception {
    initWithOneFile();

    provider.expand();

    verify(promiseProvider).resolve(eq(FILE_1_NAME));
  }

  @Test
  public void getMultipleValues() throws Exception {
    initWithTwoFiles();

    provider.expand();

    verify(promiseProvider).resolve(eq(Joiner.on(", ").join(FILE_1_NAME, FILE_2_NAME)));
  }

  @Test
  public void getEmptyValues() throws Exception {
    initWithNoFiles();

    provider.expand();

    verify(promiseProvider).resolve(eq(""));
  }
}
