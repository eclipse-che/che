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
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
@Listeners(value = MockitoTestNGListener.class)
public class LimitedSizeLineConsumerWrapperTest {

  private static final String LINE_2 = "line2";
  private static final String LINE_1 = "line1";
  private static final List<String> LIST_WITH_NULL = new ArrayList<>(asList(new String[] {null}));
  private static final int NEGATIVE_TEXT_SIZE_LIMIT = -1;

  @Test(dataProvider = "testLimitedData")
  public void shouldWriteAndClearLimitedDataCorrectly(
      List<String> consumingLines,
      int limit,
      List<String> expectedLimitedLines,
      String expectedLimitedText)
      throws IOException {
    // given
    ListLineConsumer testConsumer = new ListLineConsumer();
    LimitedSizeLineConsumerWrapper testConsumerWrapper =
        new LimitedSizeLineConsumerWrapper(testConsumer, limit);

    // when
    for (String line : consumingLines) {
      testConsumerWrapper.writeLine(line);
    }

    // then
    assertEquals(testConsumer.getLines(), expectedLimitedLines);
    assertEquals(testConsumer.getText(), expectedLimitedText);
  }

  @DataProvider
  public Object[][] testLimitedData() {
    return new Object[][] {
      {LIST_WITH_NULL, 8, EMPTY_LIST, ""},
      {EMPTY_LIST, 8, EMPTY_LIST, ""},
      {ImmutableList.of("line1", "line2"), 0, EMPTY_LIST, ""},
      {ImmutableList.of("line1", "line2"), 5, ImmutableList.of(LINE_1), LINE_1},
      {
        ImmutableList.of("line1", "line2"),
        8,
        ImmutableList.of(LINE_1, LINE_2.substring(0, 3)),
        LINE_1 + "\n" + LINE_2.substring(0, 3)
      }
    };
  }

  @Test(
    expectedExceptions = IllegalArgumentException.class,
    expectedExceptionsMessageRegExp = NEGATIVE_TEXT_SIZE_LIMIT + ""
  )
  public void shouldThrowExceptionIfTextSizeLimitIsNegative() {
    // when
    new LimitedSizeLineConsumerWrapper(new AbstractLineConsumer() {}, NEGATIVE_TEXT_SIZE_LIMIT);
  }

  @Test
  public void shouldCloseLineConsumer() throws IOException {
    // given
    LineConsumer mockConsumer = Mockito.mock(LineConsumer.class);

    // when
    new LimitedSizeLineConsumerWrapper(mockConsumer, 123).close();

    // then
    verify(mockConsumer).close();
  }
}
