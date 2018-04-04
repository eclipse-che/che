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

import org.testng.Assert;
import org.testng.annotations.Test;

/** @author andrew00x */
public class RateExceedDetectorTest {
  @Test
  public void testExceedRate() throws Exception {
    RateExceedDetector rd = new RateExceedDetector(1); // 1 per second
    Assert.assertFalse(rd.updateAndCheckRate());
    Thread.sleep(500);
    Assert.assertTrue(rd.updateAndCheckRate());
  }

  @Test
  public void testStayUnderLimit() throws Exception {
    RateExceedDetector rd = new RateExceedDetector(3); // 3 per second
    Assert.assertFalse(rd.updateAndCheckRate());
    Thread.sleep(400);
    Assert.assertFalse(rd.updateAndCheckRate());
  }

  @Test
  public void testComplex() throws Exception {
    RateExceedDetector rd = new RateExceedDetector(3); // 3 per second
    Assert.assertFalse(rd.updateAndCheckRate());
    Thread.sleep(200);
    Assert.assertTrue(rd.updateAndCheckRate());
    Thread.sleep(500);
    Assert.assertFalse(rd.updateAndCheckRate());
  }
}
