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
package org.eclipse.che.plugin.testing.ide.messages;

import elemental.json.Json;
import elemental.json.JsonException;
import elemental.json.JsonObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.eclipse.che.api.testing.shared.Constants;
import org.eclipse.che.api.testing.shared.messages.TestingMessage;
import org.eclipse.che.api.testing.shared.messages.TestingMessageNames;

/** Data class represents testing messages. */
public class ClientTestingMessage implements TestingMessage {

  static final Map<String, Supplier<? extends ClientTestingMessage>> messageConstructors =
      new HashMap<>();

  static {
    messageConstructors.put(TestingMessageNames.BUILD_TREE_ENDED, BuildTreeEnded::new);
    messageConstructors.put(TestingMessageNames.MESSAGE, Message::new);
    messageConstructors.put(TestingMessageNames.ROOT_PRESENTATION, RootPresentationMessage::new);
    messageConstructors.put(TestingMessageNames.SUITE_TREE_ENDED, SuiteTreeEnded::new);
    messageConstructors.put(TestingMessageNames.SUITE_TREE_NODE, SuiteTreeNode::new);
    messageConstructors.put(TestingMessageNames.SUITE_TREE_STARTED, SuiteTreeStarted::new);
    messageConstructors.put(TestingMessageNames.TEST_COUNT, TestCount::new);
    messageConstructors.put(TestingMessageNames.TEST_FAILED, TestFailed::new);
    messageConstructors.put(TestingMessageNames.TEST_FINISHED, TestFinished::new);
    messageConstructors.put(TestingMessageNames.TEST_IGNORED, TestIgnored::new);
    messageConstructors.put(TestingMessageNames.TEST_REPORTER_ATTACHED, TestReporterAttached::new);
    messageConstructors.put(TestingMessageNames.TEST_STARTED, TestStarted::new);
    messageConstructors.put(TestingMessageNames.TEST_STD_ERR, TestStdErr::new);
    messageConstructors.put(TestingMessageNames.TEST_STD_OUT, TestStdOut::new);
    messageConstructors.put(TestingMessageNames.TEST_SUITE_FINISHED, TestSuiteFinished::new);
    messageConstructors.put(TestingMessageNames.TEST_SUITE_STARTED, TestSuiteStarted::new);
    messageConstructors.put(TestingMessageNames.UNCAPTURED_OUTPUT, UncapturedOutputMessage::new);
  }

  private String name;
  private Map<String, String> attributes = new HashMap<>();

  ClientTestingMessage() {}

  public static ClientTestingMessage parse(String json) {
    try {
      JsonObject jsonObject = Json.parse(json);
      String name = jsonObject.getString(Constants.NAME);
      Supplier<? extends ClientTestingMessage> supplier = messageConstructors.get(name);
      if (supplier == null) {
        supplier = ClientTestingMessage::new;
      }

      ClientTestingMessage message = supplier.get();
      message.init(name, jsonObject.getObject(Constants.ATTRIBUTES));

      return message;
    } catch (JsonException e) {
      return null;
    }
  }

  private void init(String name, JsonObject object) {
    this.name = name;
    for (String key : object.keys()) {
      attributes.put(key, object.getString(key));
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public void visit(TestingMessageVisitor visitor) {
    visitor.visitTestingMessage(this);
  }

  protected String getAttributeValue(String attribute) {
    return getAttributes().get(attribute);
  }
}
