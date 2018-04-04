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
