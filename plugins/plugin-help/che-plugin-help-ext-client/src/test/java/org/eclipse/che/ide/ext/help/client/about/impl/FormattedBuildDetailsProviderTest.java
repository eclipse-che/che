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
package org.eclipse.che.ide.ext.help.client.about.impl;

import org.eclipse.che.ide.ext.help.client.about.impl.FormattedBuildDetailsProvider.SummaryTableFormatter;
import org.junit.Assert;
import org.junit.Test;

public class FormattedBuildDetailsProviderTest {

  @Test
  public void testSummaryTableFormatter() {
    SummaryTableFormatter formatter = new SummaryTableFormatter();

    formatter.addRow("a", "1000");
    formatter.addRow("10000", "b");

    String expected = "a      1000  \n" + "10000  b     \n";
    String actual = formatter.toString();

    Assert.assertEquals(expected, actual);
  }
}
