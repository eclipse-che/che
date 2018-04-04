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
package org.eclipse.che.api.editor.server.impl;

import org.eclipse.che.api.project.shared.dto.EditorChangesDto;

/**
 * Notifies about changes of editor working copy. The event is used by {@link
 * EditorWorkingCopyManager} when working copy is changed to notify interested consumers about it.
 *
 * @author Roman Nikitenko
 */
public class EditorWorkingCopyUpdatedEvent {
  private final String endpointId;
  private final EditorChangesDto textChange;

  /** Creates event which contains info about changes of editor working copy */
  EditorWorkingCopyUpdatedEvent(String endpointId, EditorChangesDto textChange) {
    this.endpointId = endpointId;
    this.textChange = textChange;
  }

  public String getEndpointId() {
    return endpointId;
  }

  /** Returns changes of editor working copy. */
  public EditorChangesDto getChanges() {
    return textChange;
  }
}
