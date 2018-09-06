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
package org.eclipse.che.plugin.jdb.server.util;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.languageserver.FindServer;
import org.eclipse.che.api.languageserver.LanguageServerInitializer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;
import org.mockito.Mockito;

/** @author Anatolii Bazko */
public class JavaLanguageServerExtensionStub extends JavaLanguageServerExtensionService {
  private static final String SRC = "/test/src/";

  public JavaLanguageServerExtensionStub() {
    super(
        mock(FindServer.class, Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)),
        mock(
            LanguageServerInitializer.class,
            Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)),
        mock(
            RequestHandlerConfigurator.class,
            Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)),
        mock(ProjectManager.class, Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)),
        mock(EventService.class, Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)));
  }

  @Override
  public String identifyFqnInResource(String target, int lineNumber) {
    int srcPos = target.indexOf(SRC);
    int beginIndex = srcPos + SRC.length();
    int endIndex = target.endsWith(".java") ? target.length() - 5 : target.length();

    return srcPos == -1 ? target : target.substring(beginIndex, endIndex).replace("/", ".");
  }

  @Override
  public Location findResourcesByFqn(String fqn, int lineNumber) {
    int innerClassStartPos = fqn.indexOf("$");
    if (innerClassStartPos != -1) {
      fqn = fqn.substring(0, innerClassStartPos);
    }

    if (fqn.startsWith("org.eclipse")) {
      return new LocationImpl(SRC + fqn.replace(".", "/") + ".java", lineNumber, "/test");
    } else {
      return new LocationImpl(fqn, lineNumber, "/test");
    }
  }
}
