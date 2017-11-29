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
package org.eclipse.che.plugin.optimized.testing.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.spi.TestResult;
import org.arquillian.smart.testing.spi.TestResultParser;

public class CheTestResultParser implements TestResultParser {

  @Override
  public Set<TestResult> parse(InputStream reportInputStream) {
    return new BufferedReader(new InputStreamReader(reportInputStream))
        .lines()
        .map(failedTest -> new TestResult(failedTest, TestResult.Result.FAILURE))
        .collect(Collectors.toSet());
  }

  @Override
  public String type() {
    return "che";
  }
}
