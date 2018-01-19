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
package org.eclipse.che.ide.ext.java.client.diagnostics;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorOpenedEventHandler;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.resource.Path;

/**
 * Asks JDT.LS to get diagnostics for opened pom.xml file.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class PomDiagnosticsRequestor {

  private static final String POM_FILE = "pom.xml";

  @Inject
  public PomDiagnosticsRequestor(
      final EventBus eventBus, final JavaLanguageExtensionServiceClient service) {
    eventBus.addHandler(
        EditorOpenedEvent.TYPE,
        new EditorOpenedEventHandler() {
          @Override
          public void onEditorOpened(EditorOpenedEvent event) {
            Path path = event.getFile().getLocation();
            if (!POM_FILE.equals(path.lastSegment())) {
              return;
            }
            new Timer() {
              @Override
              public void run() {
                processEditorOpened(path);
              }
            }.schedule(300);
          }

          private void processEditorOpened(Path path) {
            service.reComputePomDiagnostics(path.toString());
          }
        });
  }
}
