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
package org.eclipse.che.api.editor.server.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.project.shared.dto.EditorChangesDto;

/**
 * Receives notifications about editor changes from client side.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorChangesTracker {
  private static final String INCOMING_METHOD = "track:editor-content-changes";

  private EditorWorkingCopyManager editorWorkingCopyManager;

  @Inject
  public EditorChangesTracker(EditorWorkingCopyManager editorWorkingCopyManager) {
    this.editorWorkingCopyManager = editorWorkingCopyManager;
  }

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(INCOMING_METHOD)
        .paramsAsDto(EditorChangesDto.class)
        .resultAsBoolean()
        .withBiFunction(
            (endpointId, changes) -> {
              editorWorkingCopyManager.onEditorContentUpdated(endpointId, changes);
              return true;
            });
  }
}
