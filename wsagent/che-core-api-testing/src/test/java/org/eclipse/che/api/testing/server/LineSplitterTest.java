/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.testing.server;

import static org.eclipse.che.commons.lang.execution.ProcessOutputType.STDOUT;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.eclipse.che.api.testing.server.framework.LineSplitter;
import org.eclipse.che.api.testing.server.framework.LineSplitter.Consumer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Testing splitting of test's output messages {@link LineSplitter}.
 *
 * @author Valeriy Svydenko
 */
@Listeners(MockitoTestNGListener.class)
public class LineSplitterTest {

  @Mock private Consumer consumer;
  private LineSplitter splitter;

  @BeforeMethod
  public void setUp() throws Exception {
    splitter = new LineSplitter(consumer);
  }

  @Test
  public void singleTestOutputShouldBeConsumed() throws Exception {
    String out = "@@<{test's output message}>";

    splitter.process(out, STDOUT);

    verify(consumer).consume(out, STDOUT);
  }

  @Test
  public void messageWithoutEndShouldNotBeConsumed() throws Exception {
    String out = "@@<{test's output message";

    splitter.process(out, STDOUT);

    verify(consumer, never()).consume(anyString(), eq(STDOUT));
  }

  @Test
  public void messageWithNextLineSymbolShouldBeConsumed() throws Exception {
    String out = "@@<{test's output message\n";

    splitter.process(out, STDOUT);

    verify(consumer).consume(out, STDOUT);
  }

  @Test
  public void manyMessagesShouldBeConsumedAsOne() throws Exception {
    String out1 = "@@<{first message";
    String out2 = "second message}>";

    splitter.process(out1, STDOUT);
    splitter.process(out2, STDOUT);

    verify(consumer).consume(out1 + out2, STDOUT);
  }

  @Test
  public void manyMessagesShouldBeConsumedAsMany() throws Exception {
    String out1 = "@@<{first message}>";
    String out2 = "@@<{second message}>";

    splitter.process(out1, STDOUT);
    splitter.process(out2, STDOUT);

    verify(consumer).consume(out1, STDOUT);
    verify(consumer).consume(out2, STDOUT);
  }
}
