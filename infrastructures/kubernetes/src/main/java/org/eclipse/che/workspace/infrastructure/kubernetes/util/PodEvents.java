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

import com.google.common.base.Strings;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;

/**
 * Helps to simplify the interaction with the {@link PodEvent}.
 *
 * @author Ilya Buziuk
 */
public final class PodEvents {
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

  private PodEvents() {}

  /**
   * Converts the time of {@link PodEvent} e.g. '2018-05-15T16:17:54Z' to the {@link Date} format
   */
  public static Date convertEventTimestampToDate(String timestamp) throws ParseException {
    if (Strings.isNullOrEmpty(timestamp)) {
      throw new IllegalArgumentException("Pod event timestamp can not be blank");
    }
    return dateFormat.parse(timestamp);
  }

  /** Converts the {@link Date} to {@link PodEvent} timestamp format e.g. '2018-05-15T16:17:54Z' */
  public static String convertDateToEventTimestamp(Date date) {
    return dateFormat.format(date);
  }
}
