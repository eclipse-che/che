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
package org.eclipse.che.api.workspace.server.spi.environment;

import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Recipe of {@link Environment} with content either provided by {@link Recipe#getContent()} or
 * downloaded from {@link Recipe#getLocation()}.
 *
 * @author Alexander Garagatyi
 * @author gazarenkov
 * @author Sergii Leshchenko
 */
public class InternalRecipe {
  private final String type;
  private final String contentType;
  private final String content;

  InternalRecipe(String type, String contentType, String content) {
    this.type = type;
    this.contentType = contentType;
    this.content = content;
  }

  /** Type of the recipe. It is mandatory. */
  public String getType() {
    return type;
  }

  /** Content type. It is optional. */
  @Nullable
  public String getContentType() {
    return contentType;
  }

  /** The context of the recipe. It is mandatory. */
  public String getContent() {
    return content;
  }
}
