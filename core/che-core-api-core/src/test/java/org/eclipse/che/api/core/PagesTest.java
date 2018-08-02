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
package org.eclipse.che.api.core;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Tests {@link Pages}.
 *
 * @author Yevhenii Voevodin
 */
public class PagesTest {

  private TestPagesSupplier testSource;

  @BeforeSuite
  private void setUp() {
    String[] strings = new String[10];
    for (int i = 0; i < strings.length; i++) {
      strings[i] = "test-string-" + i;
    }
    testSource = new TestPagesSupplier(strings);
  }

  @Test
  public void eagerlyStreamsAllElements() {
    List<String> result = Pages.stream(testSource::getStrings, 2).collect(Collectors.toList());

    assertEquals(result, testSource.strings);
  }

  @Test
  public void eagerlyIteratesAllElements() throws Exception {
    ArrayList<String> result = Lists.newArrayList(Pages.iterate(testSource::getStrings, 2));

    assertEquals(result, testSource.strings);
  }

  @Test
  public void lazyStreamsAllElements() {
    List<String> result =
        Pages.streamLazily(testSource::getStrings, 2).collect(Collectors.toList());

    assertEquals(result, testSource.strings);
  }

  @Test
  public void lazyIteratesAllElements() {
    ArrayList<String> result = Lists.newArrayList(Pages.iterateLazily(testSource::getStrings, 2));

    assertEquals(result, testSource.strings);
  }

  @Test
  public void lazyStreamingDoesNotPollNextPageUntilNeeded() {
    TestPagesSupplier src = spy(new TestPagesSupplier("string1", "string2", "string3"));

    assertTrue(Pages.streamLazily(src::getStrings, 1).anyMatch(s -> s.equals("string2")));

    verify(src, times(2)).getStrings(anyInt(), anyLong());
    verify(src).getStrings(1, 0);
    verify(src).getStrings(1, 1);
  }

  @Test
  public void lazyIteratingDoesNotPollNextPageUntilNeeded() {
    TestPagesSupplier src = spy(new TestPagesSupplier("string1", "string2", "string3"));

    Iterator<String> it = Pages.iterateLazily(src::getStrings, 1).iterator();
    it.next();
    it.next();

    verify(src, times(2)).getStrings(anyInt(), anyLong());
    verify(src).getStrings(1, 0);
    verify(src).getStrings(1, 1);
  }

  @Test
  public void returnsEmptyStreamWhenFetchingEagerly() {
    Stream<String> stream = Pages.stream(new TestPagesSupplier()::getStrings);

    assertFalse(stream.findAny().isPresent());
  }

  @Test
  public void returnsIterableWithNoElementsWhileFetchingEagerly() {
    Iterator<String> it = Pages.iterate(new TestPagesSupplier()::getStrings).iterator();

    assertFalse(it.hasNext());
  }

  @Test
  public void returnsEmptyStreamWhenFetchingLazily() {
    Stream<String> stream = Pages.streamLazily(new TestPagesSupplier()::getStrings);

    assertFalse(stream.findAny().isPresent());
  }

  @Test
  public void returnsIterableWithNoeElementsWhileFetchingLazily() {
    Iterator<String> it = Pages.iterateLazily(new TestPagesSupplier()::getStrings).iterator();

    assertFalse(it.hasNext());
  }

  private static class TestPagesSupplier {

    private final List<String> strings;

    private TestPagesSupplier(String... strings) {
      this.strings = Arrays.asList(strings);
    }

    public Page<String> getStrings(int max, long skip) {
      List<String> items = strings.stream().skip(skip).limit(max).collect(Collectors.toList());
      return new Page<>(items, skip, max, strings.size());
    }
  }
}
