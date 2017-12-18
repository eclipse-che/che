/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client;

import static org.testng.Assert.assertEquals;

import org.eclipse.che.infrastructure.docker.client.json.SystemInfo;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** @author andrew00x */
public class SystemInfoDriverStatusTest {
  private SystemInfo info;

  @BeforeTest
  public void initialize() {
    info = new SystemInfo();
    String[][] driverStatus = new String[4][2];
    driverStatus[0] = new String[] {"Data Space Total", "107.4 GB"};
    driverStatus[1] = new String[] {"Data Space Used", "957.6 MB"};
    driverStatus[2] = new String[] {"Metadata Space Total", "2.147 GB"};
    driverStatus[3] = new String[] {"Metadata Space Used", "1.749 MB"};
    info.setDriverStatus(driverStatus);
  }

  @Test
  public void testGetDataSpaceTotal() {
    assertEquals(info.dataSpaceTotal(), (long) (107.4f * (1024 * 1024 * 1024)));
  }

  @Test
  public void testGetDataSpaceUsed() {
    assertEquals(info.dataSpaceUsed(), (long) (957.6f * (1024 * 1024)));
  }

  @Test
  public void testGetMetaDataSpaceTotal() {
    assertEquals(info.metadataSpaceTotal(), (long) (2.147f * (1024 * 1024 * 1024)));
  }

  @Test
  public void testGetMetaDataSpaceUsed() {
    assertEquals(info.metadataSpaceUsed(), (long) (1.749f * (1024 * 1024)));
  }
}
