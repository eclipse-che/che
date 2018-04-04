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
package org.eclipse.che.ide.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test of the bytes converter
 *
 * @author Florent Benoit
 */
public class BytesTest {

  @Test
  public void convert0MiBValue() {
    String newSize = Bytes.toHumanSize("0MiB");
    Assert.assertEquals("0B", newSize);
  }

  @Test
  public void convert16MiBValue() {
    String newSize = Bytes.toHumanSize("16MiB");
    Assert.assertEquals("16MiB", newSize);
  }

  @Test
  public void convert100MBValue() {
    String newSize = Bytes.toHumanSize("100MB");
    Assert.assertEquals("100MB", newSize);
  }

  @Test
  public void convert200MBValue() {
    String newSize = Bytes.toHumanSize("200MB");
    Assert.assertEquals("200MB", newSize);
  }

  @Test
  public void convert500MBValue() {
    String newSize = Bytes.toHumanSize("500MB");
    Assert.assertEquals("500MB", newSize);
  }

  @Test
  public void convert1024MiBValue() {
    String newSize = Bytes.toHumanSize("1024MiB");
    Assert.assertEquals("1GiB", newSize);
  }

  @Test
  public void convert512MiBValue() {
    String newSize = Bytes.toHumanSize("512MiB");
    Assert.assertEquals("512MiB", newSize);
  }

  @Test
  public void convert256MiBValue() {
    String newSize = Bytes.toHumanSize("256MiB");
    Assert.assertEquals("256MiB", newSize);
  }

  @Test
  public void convert1000MBValue() {
    String newSize = Bytes.toHumanSize("1000MB");
    Assert.assertEquals("1GB", newSize);
  }

  @Test
  public void convert4096MiBValue() {
    String newSize = Bytes.toHumanSize("4096MiB");
    Assert.assertEquals("4GiB", newSize);
  }

  @Test
  public void convert1536MiBValue() {
    String newSize = Bytes.toHumanSize("1536MiB");
    Assert.assertEquals("1.5GiB", newSize);
  }

  @Test
  public void convert1500MBValue() {
    String newSize = Bytes.toHumanSize("1500MB");
    Assert.assertEquals("1.5GB", newSize);
  }

  @Test
  public void convert1500000MBValue() {
    String newSize = Bytes.toHumanSize("1500000MB");
    Assert.assertEquals("1.5TB", newSize);
  }

  @Test
  public void convert1572864MiBValue() {
    String newSize = Bytes.toHumanSize("1572864MiB");
    Assert.assertEquals("1.5TiB", newSize);
  }

  @Test
  public void convert1204GiBValue() {
    String newSize = Bytes.toHumanSize("1024GiB");
    Assert.assertEquals("1TiB", newSize);
  }

  @Test
  public void convert2TiBValue() {
    String newSize = Bytes.toHumanSize("2048GiB");
    Assert.assertEquals("2TiB", newSize);
  }

  @Test
  public void convert2TBValue() {
    String newSize = Bytes.toHumanSize("2000GB");
    Assert.assertEquals("2TB", newSize);
  }

  @Test
  public void convert1PiBValue() {
    String newSize = Bytes.toHumanSize("1125899906842624B");
    Assert.assertEquals("1PiB", newSize);
  }

  @Test
  public void checkSplitUnit() {
    Pair<Double, Bytes.Unit> value = Bytes.splitValueAndUnit("150MB");
    Assert.assertEquals(Double.valueOf(150), value.getFirst());
    Assert.assertEquals(Bytes.Unit.MB, value.getSecond());
  }

  @Test
  public void checkSplitFloatUnit() {
    Pair<Double, Bytes.Unit> value = Bytes.splitValueAndUnit("1.5GB");
    Assert.assertEquals(Double.valueOf(1.5), value.getFirst());
    Assert.assertEquals(Bytes.Unit.GB, value.getSecond());
  }

  @Test
  public void checkSplitGiBUnit() {
    Pair<Double, Bytes.Unit> value = Bytes.splitValueAndUnit("1500GiB");
    Assert.assertEquals(Double.valueOf(1500), value.getFirst());
    Assert.assertEquals(Bytes.Unit.GiB, value.getSecond());
  }

  @Test
  public void checkSplitUnitSpace() {
    Pair<Double, Bytes.Unit> value = Bytes.splitValueAndUnit(" 150 MB");
    Assert.assertEquals(Double.valueOf(150), value.getFirst());
    Assert.assertEquals(Bytes.Unit.MB, value.getSecond());
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkSplitInvalidUnit() {
    Bytes.splitValueAndUnit("150");
    Assert.fail("Should have failed");
  }

  @Test
  public void convert1024MiBToBytes() {
    double result = Bytes.convertToBytes(new Pair<Double, Bytes.Unit>(1024d, Bytes.Unit.MiB));
    Assert.assertEquals(1024 * 1024 * 1024, result, 0);
  }

  @Test
  public void convert1000MBToBytes() {
    double result = Bytes.convertToBytes(new Pair<Double, Bytes.Unit>(1000d, Bytes.Unit.MB));
    Assert.assertEquals(1e9, result, 0);
  }

  @Test
  public void convert4096MiBToBytes() {
    double result = Bytes.convertToBytes(new Pair<Double, Bytes.Unit>(4096d, Bytes.Unit.MiB));
    Assert.assertEquals(4d * 1024 * 1024 * 1024, result, 0);
  }
}
