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

/** Test of UUID generator */
public class UUIDTest {

  @Test
  public void testUUIDLength() {
    String uuid = UUID.uuid(15);
    Assert.assertEquals(15, uuid.length());
  }

  @Test
  public void testUUIDRadix() {
    String uuid1 = UUID.uuid(6, 2);
    Assert.assertTrue(uuid1.matches("[0-1]{6}"));
    String uuid2 = UUID.uuid(8, 10);
    Assert.assertTrue(uuid2.matches("[0-9]{8}"));
  }

  @Test
  public void testUUIDRFC4122() {
    String uuid = UUID.uuid();
    Assert.assertTrue(
        uuid.matches("^[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$"));
  }
}
