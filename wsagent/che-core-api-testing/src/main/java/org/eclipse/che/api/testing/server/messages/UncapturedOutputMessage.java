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
package org.eclipse.che.api.testing.server.messages;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.testing.shared.messages.TestingMessageNames;
import org.eclipse.che.commons.lang.execution.ProcessOutputType;

/** Simple test message which contains text and type. */
public class UncapturedOutputMessage extends ServerTestingMessage {
  public UncapturedOutputMessage(String text, ProcessOutputType outputType) {
    super(TestingMessageNames.UNCAPTURED_OUTPUT);
    Map<String, String> attributes = new HashMap<>();
    attributes.put("output", text);
    attributes.put("outputType", outputType.toString());
    setAttributes(attributes);
  }
}
