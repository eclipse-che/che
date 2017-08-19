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
package org.eclipse.che.api.project.server.importer;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import org.testng.annotations.Test;

/**
 * Unit tests for the {@link BaseProjectImportOutputLineConsumer}.
 *
 * @author Vlad Zhukovskyi
 */
public class BaseProjectImportOutputLineConsumerTest {

  @Test
  public void shouldSendOutputLine() throws IOException {
    new BaseProjectImportOutputLineConsumer("project", 100) {
      @Override
      protected void sendOutputLine(String outputLine) {
        assertEquals(outputLine, "message");
      }
    }.sendOutputLine("message");
  }
}
