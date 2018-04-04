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
package org.eclipse.che.api.project.server;

import java.io.IOException;
import java.util.function.Supplier;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.core.util.LineConsumer;

public interface ProjectImporter {

  enum SourceCategory {
    VCS("Version control system"),
    ARCHIVE("Archive");

    private final String value;

    SourceCategory(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /** @return unique id of importer e.g git, zip */
  String getId();

  /**
   * @return {@code true} if this importer uses only internal and not accessible for users call
   *     otherwise {@code false}
   */
  boolean isInternal();

  /**
   * @return {@link String} importer's source category (example: version control system, archive)
   */
  SourceCategory getSourceCategory();

  /** @return human readable description about this importer */
  String getDescription();

  /**
   * Imports source from the given {@code location} to the specified folder.
   *
   * @param dst base project folder
   * @param src the object which contains information about source(location, parameters, type etc.)
   * @throws ForbiddenException if some operations in {@code location} are forbidden, e.g. current
   *     user doesn't have write permissions to the {@code location}
   * @throws ConflictException if import causes any conflicts, e.g. if import operation causes name
   *     conflicts in {@code location}
   * @throws UnauthorizedException if user isn't authorized to access to access {@code location}
   * @throws IOException if any i/o errors occur, e.g. when try to access {@code location}
   * @throws ServerException if import causes some errors that should be treated as internal errors
   */
  void doImport(SourceStorage src, String dst)
      throws ForbiddenException, ConflictException, UnauthorizedException, IOException,
          ServerException, NotFoundException;

  /**
   * Imports source from the given {@code location} to the specified folder.
   *
   * @param dst base project folder
   * @param src the object which contains information about source(location, parameters, type etc.)
   * @param supplier output string consumer factory to get the import process output. For instance,
   *     Git command output for the Git importer
   * @throws ForbiddenException if some operations in {@code baseFolder} are forbidden, e.g. current
   *     user doesn't have write permissions to the {@code baseFolder}
   * @throws ConflictException if import causes any conflicts, e.g. if import operation causes name
   *     conflicts in {@code baseFolder}
   * @throws UnauthorizedException if user isn't authorized to access to access {@code location}
   * @throws IOException if any i/o errors occur, e.g. when try to access {@code location}
   * @throws ServerException if import causes some errors that should be treated as internal errors
   */
  void doImport(SourceStorage src, String dst, Supplier<LineConsumer> supplier)
      throws ForbiddenException, ConflictException, UnauthorizedException, IOException,
          ServerException, NotFoundException;
}
