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
package org.eclipse.che.plugin.svn.shared;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ListResponse {

  /**
   * ************************************************************************
   *
   * <p>Subversion command
   *
   * <p>************************************************************************
   */
  String getCommand();

  void setCommand(@NotNull final String command);

  ListResponse withCommand(@NotNull final String command);

  /**
   * ************************************************************************
   *
   * <p>Execution output
   *
   * <p>************************************************************************
   */
  List<String> getOutput();

  void setOutput(@NotNull final List<String> output);

  ListResponse withOutput(@NotNull final List<String> output);

  /**
   * ************************************************************************
   *
   * <p>Error output
   *
   * <p>************************************************************************
   */
  List<String> getErrorOutput();

  void setErrorOutput(List<String> errorOutput);

  ListResponse withErrorOutput(List<String> errorOutput);
}
