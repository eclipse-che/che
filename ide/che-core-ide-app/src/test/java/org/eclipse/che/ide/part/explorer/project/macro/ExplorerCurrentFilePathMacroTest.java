/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
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
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for the {@link ExplorerCurrentFilePathMacro}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class ExplorerCurrentFilePathMacroTest extends AbstractExplorerMacroTest {

  private ExplorerCurrentFilePathMacro provider;

  @Before
  public void init() throws Exception {
    super.init();
    provider =
        new ExplorerCurrentFilePathMacro(
            projectExplorer, promiseProvider, appContext, localizationConstants);
  }

  @Test
  public void testGetKey() throws Exception {
    assertSame(provider.getName(), ExplorerCurrentFilePathMacro.KEY);
  }

  @Test
  public void getValue() throws Exception {
    initWithOneFile();

    provider.expand();

    verify(promiseProvider).resolve(eq(Path.valueOf(PROJECTS_ROOT).append(FILE_1_PATH).toString()));
  }

  @Test
  public void getMultipleValues() throws Exception {
    initWithTwoFiles();

    provider.expand();

    verify(promiseProvider)
        .resolve(
            eq(
                Joiner.on(", ")
                    .join(
                        Path.valueOf(PROJECTS_ROOT).append(FILE_1_PATH).toString(),
                        Path.valueOf(PROJECTS_ROOT).append(FILE_2_PATH).toString())));
  }

  @Test
  public void getEmptyValues() throws Exception {
    initWithNoFiles();

    provider.expand();

    verify(promiseProvider).resolve(eq(""));
  }
}
