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
package org.eclipse.che.ide.ext.java.client.diagnostics;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorOpenedEventHandler;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;

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
      final EventBus eventBus,
      final DtoBuildHelper buildHelper,
      final JavaLanguageExtensionServiceClient service) {
    eventBus.addHandler(
        EditorOpenedEvent.TYPE,
        new EditorOpenedEventHandler() {
          @Override
          public void onEditorOpened(EditorOpenedEvent event) {
            String uri = buildHelper.getUri(event.getFile());
            if (!uri.endsWith(POM_FILE)) {
              return;
            }
            new Timer() {
              @Override
              public void run() {
                processEditorOpened(uri);
              }
            }.schedule(300);
          }

          private void processEditorOpened(String uri) {
            service.reComputePomDiagnostics(uri);
          }
        });
  }
}
