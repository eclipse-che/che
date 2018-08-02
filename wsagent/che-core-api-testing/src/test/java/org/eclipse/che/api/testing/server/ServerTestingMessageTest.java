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
package org.eclipse.che.api.testing.server;

import org.eclipse.che.api.testing.server.messages.ServerTestingMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** */
public class ServerTestingMessageTest {

  @Test
  public void testMessageShouldUnescapeAttributes() throws Exception {
    String out =
        "@@<{\"name\":\"testFailed\", \"attributes\":{\"name\":\"Test.testMethod\", \"details\":\"java.lang.Exception: Test!n\tat org.testng.che.tests.ListeneterTest.testName(ListeneterTest.java:71)!n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)!n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)!n\", \"message\":\"Test\"}}>";
    ServerTestingMessage parse = ServerTestingMessage.parse(out);
    Assert.assertEquals(
        parse.asJsonString(),
        "{\"name\":\"testFailed\",\"attributes\":{\"name\":\"Test.testMethod\",\"details\":\"java.lang.Exception: Test\\n\\tat org.testng.che.tests.ListeneterTest.testName(ListeneterTest.java:71)\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\\n\",\"message\":\"Test\"}}");
  }
}
