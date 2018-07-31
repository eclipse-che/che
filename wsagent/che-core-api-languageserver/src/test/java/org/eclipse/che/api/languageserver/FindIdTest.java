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
package org.eclipse.che.api.languageserver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class FindIdTest {

  private static final String ID_1 = "id-1";
  private static final String ID_2 = "id-2";

  private RegistryContainer registryContainer;

  private FindId findId;

  @BeforeMethod
  public void setUp() {
    registryContainer = new RegistryContainer();
    findId = new FindId(registryContainer);
  }

  @Test
  public void shouldMatchPathWithSingleId() {
    Set<Pattern> patterns_1 = ImmutableSet.of(Pattern.compile(".*[/\\\\]+name\\.extension"));
    Set<Pattern> patterns_2 = ImmutableSet.of(Pattern.compile(".*[/\\\\]+name1\\.extension"));

    registryContainer.patternRegistry.add(ID_1, patterns_1);
    registryContainer.patternRegistry.add(ID_2, patterns_2);

    Set<String> ids = findId.byPath("/a/b/c/name.extension");

    assertEquals(ids, ImmutableSet.of(ID_1));
  }

  @Test
  public void shouldMatchPathWithTwoIds() {
    Set<Pattern> patterns_1 = ImmutableSet.of(Pattern.compile(".*[/\\\\]+name\\.extension"));
    Set<Pattern> patterns_2 = ImmutableSet.of(Pattern.compile(".*[/\\\\]+name\\.extension"));
    registryContainer.patternRegistry.add(ID_1, patterns_1);
    registryContainer.patternRegistry.add(ID_2, patterns_2);

    Set<String> ids = findId.byPath("/a/b/c/name.extension");

    assertEquals(ids, ImmutableSet.of(ID_1, ID_2));
  }

  @Test
  public void shouldNotMatchPath() {
    Set<Pattern> patterns_1 = ImmutableSet.of(Pattern.compile(".*[/\\\\]+name\\.extension"));
    registryContainer.patternRegistry.add(ID_1, patterns_1);

    Set<String> ids = findId.byPath("/a/b/c/notname.extension");

    assertTrue(ids.isEmpty());
  }
}
