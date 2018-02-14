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
package org.eclipse.che.api.core.util;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
@Listeners(value = MockitoTestNGListener.class)
public class ListLineConsumerTest {

  private static final String LINE_2 = "line2";
  private static final String LINE_1 = "line1";
  private static final List<String> LIST_WITH_NULL = new ArrayList<>(asList(new String[] {null}));

  @Test(dataProvider = "testData")
  public void shouldWriteAndClearLinesCorrectly(
      List<String> consumingLines, List<String> expectedLines, String expectedText) {
    // given
    ListLineConsumer testConsumer = new ListLineConsumer();

    // when
    consumingLines.forEach(testConsumer::writeLine);

    // then
    assertEquals(testConsumer.getLines(), expectedLines);
    assertEquals(testConsumer.getText(), expectedText);

    // when
    testConsumer.clear();

    // then
    assertEquals(testConsumer.getLines(), EMPTY_LIST);
    assertEquals(testConsumer.getText(), "");
  }

  @DataProvider
  public Object[][] testData() {
    return new Object[][] {
      {LIST_WITH_NULL, LIST_WITH_NULL, "null"},
      {EMPTY_LIST, EMPTY_LIST, ""},
      {ImmutableList.of("line1", "line2"), ImmutableList.of(LINE_1, LINE_2), LINE_1 + "\n" + LINE_2}
    };
  }
}
