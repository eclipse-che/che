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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents edited region of the changed file.
 *
 * @author Igor Vinokur
 */
@DTO
public interface EditedRegion {

  /** First line of the edited region. */
  int getBeginLine();

  void setBeginLine(int startLine);

  EditedRegion withBeginLine(int startLine);

  /** Last line of the edited region. */
  int getEndLine();

  void setEndLine(int endLine);

  EditedRegion withEndLine(int endLine);

  /** Status of the edition e.g. insertion, modification, deletion. */
  EditedRegionType getType();

  void setType(EditedRegionType type);

  EditedRegion withType(EditedRegionType type);
}
