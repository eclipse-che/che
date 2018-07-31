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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import java.text.ParseException;
import java.util.Date;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests {@link PodEvents}.
 *
 * @author Ilya Buziuk
 */
public class PodEventsTest {

  @Test
  public void eventDateShouldBeBeforeCurrentDate() throws ParseException {
    String eventTime = "2018-05-15T16:17:54Z";
    Date eventDate = PodEvents.convertEventTimestampToDate(eventTime);
    Assert.assertTrue(eventDate.before(new Date()));
  }

  @Test(expectedExceptions = ParseException.class)
  public void throwsParseExceptionWhenDateFormatIsInvalid() throws ParseException {
    String eventTime = "2018-05-15T16:143435Z";
    PodEvents.convertEventTimestampToDate(eventTime);
  }

  @Test(expectedExceptions = ParseException.class)
  public void throwsIllegalArgumentExceptionWhenDateIs12DotSring() throws ParseException {
    String eventTime = "12.";
    PodEvents.convertEventTimestampToDate(eventTime);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void throwsIllegalArgumentExceptionWhenDateIsNull() throws ParseException {
    String eventTime = null;
    PodEvents.convertEventTimestampToDate(eventTime);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void throwsIllegalArgumentExceptionWhenDateIsEmptyString() throws ParseException {
    String eventTime = "";
    PodEvents.convertEventTimestampToDate(eventTime);
  }

  @Test
  public void getEventTimestampFromDate() throws ParseException {
    String timestamp = "2018-05-15T16:17:54Z";
    Date date = PodEvents.convertEventTimestampToDate(timestamp);
    String timestampFromDate = PodEvents.convertDateToEventTimestamp(date);
    Date dateAfterParsingTimestamp = PodEvents.convertEventTimestampToDate(timestampFromDate);
    Assert.assertEquals(date, dateAfterParsingTimestamp);
  }
}
