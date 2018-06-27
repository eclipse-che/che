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

  @Test
  public void getEventTimestampFromDate() throws ParseException {
    String timestamp = "2018-05-15T16:17:54Z";
    Date date = PodEvents.convertEventTimestampToDate(timestamp);
    String timestampFromDate = PodEvents.convertDateToEventTimestamp(date);
    Date dateAfterParsingTimestamp = PodEvents.convertEventTimestampToDate(timestampFromDate);
    Assert.assertEquals(date, dateAfterParsingTimestamp);
  }
}
