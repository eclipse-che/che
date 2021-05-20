/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SerializableConverterTest {

  private final SerializableConverter converter = new SerializableConverter();

  @DataProvider
  public static Object[][] SerializableProvider() {
    return new Object[][] {
      {"foo"},
      {"bar"},
      {true},
      {false},
      {Integer.MAX_VALUE},
      {0},
      {Integer.MIN_VALUE},
      {"{\"java.home\": \"/home/user/jdk11\", \"java.jdt.ls.vmargs\": \"-Xmx1G\"}"},
      {new String[] {"single"}},
      {new String[] {"--enable-all", "--new"}},
      {new int[] {213, 456, 459}},
      {new boolean[] {true, false, false}}
    };
  }

  @Test(dataProvider = "SerializableProvider")
  public void testConvertToDatabaseColumnAndBack(Serializable initialObj) {
    String res = converter.convertToDatabaseColumn(initialObj);
    Serializable backObj = converter.convertToEntityAttribute(res);
    assertEquals(initialObj, backObj);
  }
}
