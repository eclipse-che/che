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
package org.eclipse.che.commons.lang;

import java.text.DecimalFormat;
import org.testng.Assert;
import org.testng.annotations.Test;

/** @author andrew00x */
public class SizeTest {
  char sep = new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();

  @Test
  public void testToHumanSize() {
    Assert.assertEquals(Size.toHumanSize(1024), "1 kB");
    Assert.assertEquals(Size.toHumanSize(1000), "1000 B");
    Assert.assertEquals(Size.toHumanSize(1024 * 1024), "1 MB");
    Assert.assertEquals(Size.toHumanSize(5 * 1024 * 1024), "5 MB");
    Assert.assertEquals(Size.toHumanSize(5L * 1024 * 1024 * 1024), "5 GB");
    Assert.assertEquals(Size.toHumanSize(7539480), "7" + sep + "2 MB");
    Assert.assertEquals(Size.toHumanSize(10226124), "9" + sep + "8 MB");
  }

  @Test
  public void testParseToByte() {
    Assert.assertEquals(Size.parseSize("1 kB"), 1024);
    Assert.assertEquals(Size.parseSize("1000B"), 1000);
    Assert.assertEquals(Size.parseSize("1000"), 1000);
    Assert.assertEquals(Size.parseSize("1 MB"), 1024 * 1024);
    Assert.assertEquals(Size.parseSize("5 mb"), 5 * 1024 * 1024);
    Assert.assertEquals(Size.parseSize("9.8M"), Float.valueOf(9.8f * 1024 * 1024).longValue());
  }

  @Test
  public void testParseToMegabyte() {
    Assert.assertEquals(Size.parseSizeToMegabytes("10485760"), 10);
    Assert.assertEquals(Size.parseSizeToMegabytes("12 MB"), 12);
    Assert.assertEquals(Size.parseSizeToMegabytes("1GB"), 1024);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseErrorInvalidSuffix() {
    Size.parseSize("1 xx");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseErrorInvalidNumber() {
    Size.parseSize("1.x kB");
  }
}
