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
package org.eclipse.che.commons.test.db;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.io.Resources;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.persistence.exceptions.ExceptionHandler;
import org.testng.annotations.Test;

/**
 * Tests {@link PersistTestModuleBuilder}.
 *
 * @author Yevhenii Voevodin
 */
public class PersistTestModuleBuilderTest {

  @Test
  public void generatesPersistenceXml() throws Exception {
    Path path =
        new PersistTestModuleBuilder()
            .setDriver("org.h2.Driver")
            .addEntityClass(MyEntity1.class)
            .addEntityClass(
                "org.eclipse.che.commons.test.db.PersistTestModuleBuilderTest$MyEntity2")
            .setUrl("jdbc:h2:mem:test")
            .setUser("username")
            .setPassword("secret")
            .setLogLevel("FINE")
            .setPersistenceUnit("test-unit")
            .setExceptionHandler(MyExceptionHandler.class)
            .setProperty("custom-property", "value")
            .savePersistenceXml();

    URL url =
        Thread.currentThread()
            .getContextClassLoader()
            .getResource("org/eclipse/che/commons/test/db/test-persistence-1.xml");
    assertNotNull(url);
    assertEquals(new String(Files.readAllBytes(path), UTF_8), Resources.toString(url, UTF_8));
  }

  private static class MyEntity1 {}

  private static class MyExceptionHandler implements ExceptionHandler {
    @Override
    public Object handleException(RuntimeException e) {
      throw new RuntimeException();
    }
  }
}
