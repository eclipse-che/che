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
package org.eclipse.che.api.workspace.server.devfile;

import static org.testng.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.testng.annotations.Test;

public class DevfileVersionTest {
  private final DevfileVersionDetector devfileVersionDetector = new DevfileVersionDetector();

  @Test(expectedExceptions = DevfileException.class)
  public void shouldThrowExceptionWhenEmptyDevfile() throws DevfileException {
    JsonNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfileVersionDetector.devfileVersion(devfile);
  }

  @Test
  public void shouldReturnApiVersion() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfile.put("apiVersion", "1.1.1");
    assertEquals(devfileVersionDetector.devfileVersion(devfile), "1.1.1");
  }

  @Test
  public void shouldReturnSchemaVersion() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfile.put("schemaVersion", "1.1.1");
    assertEquals(devfileVersionDetector.devfileVersion(devfile), "1.1.1");
  }

  @Test
  public void shouldReturnApiVersionWhenBothDefined() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfile.put("apiVersion", "1");
    devfile.put("schemaVersion", "2");
    assertEquals(devfileVersionDetector.devfileVersion(devfile), "1");
  }

  @Test
  public void shouldReturnMainVersionFromSchemaVersion() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfile.put("schemaVersion", "10.1.1");
    assertEquals(devfileVersionDetector.devfileMajorVersion(devfile), 10);
  }

  @Test
  public void shouldReturnMainVersionFromApiVersion() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfile.put("apiVersion", "11.1.1");
    assertEquals(devfileVersionDetector.devfileMajorVersion(devfile), 11);
  }

  @Test(expectedExceptions = DevfileException.class)
  public void shouldThrowExceptionWhenVersionNotDefined() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfileVersionDetector.devfileMajorVersion(devfile);
  }

  @Test
  public void shouldReturnMajorVersionWhenIsNumberString() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfile.put("apiVersion", "2");
    assertEquals(devfileVersionDetector.devfileMajorVersion(devfile), 2);
  }

  @Test
  public void shouldReturnMajorVersionWhenIsNumber() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfile.put("apiVersion", 2);
    assertEquals(devfileVersionDetector.devfileMajorVersion(devfile), 2);
  }

  @Test(expectedExceptions = DevfileException.class)
  public void shouldThrowExceptionWhenVersionIsNotNumber() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfile.put("apiVersion", "a");
    devfileVersionDetector.devfileMajorVersion(devfile);
  }

  @Test(expectedExceptions = DevfileException.class)
  public void shouldThrowExceptionWhenVersionIsNotSemverNumber() throws DevfileException {
    ObjectNode devfile = new ObjectNode(JsonNodeFactory.instance);
    devfile.put("apiVersion", "a.a");
    devfileVersionDetector.devfileMajorVersion(devfile);
  }
}
