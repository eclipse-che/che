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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.jdt.ls.extension.api.MatchStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class SimilarNamesConfigurationViewImplTest {
  @Mock private JavaLocalizationConstant locale;

  private SimilarNamesConfigurationViewImpl view;

  @Before
  public void setUp() throws Exception {
    view = new SimilarNamesConfigurationViewImpl(locale);
  }

  @Test
  public void machStrategyShouldBeReturnExacValue() throws Exception {
    when(view.findExactNames.getValue()).thenReturn(true);
    verify(locale).renameSimilarNamesConfigurationTitle();

    assertEquals(MatchStrategy.EXACT, view.getMatchStrategy());
  }

  @Test
  public void machStrategyShouldBeReturnEmbeddedValue() throws Exception {
    when(view.findExactNames.getValue()).thenReturn(false);
    when(view.findEmbeddedNames.getValue()).thenReturn(true);

    assertEquals(MatchStrategy.EMBEDDED, view.getMatchStrategy());
  }

  @Test
  public void machStrategyShouldBeReturnSuffixValue() throws Exception {
    when(view.findExactNames.getValue()).thenReturn(false);
    when(view.findEmbeddedNames.getValue()).thenReturn(false);
    when(view.findNameSuffixes.getValue()).thenReturn(true);

    assertEquals(MatchStrategy.SUFFIX, view.getMatchStrategy());
  }
}
