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
package org.eclipse.che.api.workspace.server.adapter;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import java.util.Set;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

/**
 * Adapts an old format of {@link Workspace#getConfig()} to a new one.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceMessageBodyAdapter extends WorkspaceConfigMessageBodyAdapter {

  @Override
  public Set<Class<?>> getTriggers() {
    return ImmutableSet.of(Workspace.class, WorkspaceDto.class);
  }

  @Override
  protected JsonObject getWorkspaceConfigObj(JsonObject root) {
    return root.getAsJsonObject("config");
  }
}
