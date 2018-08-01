/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
