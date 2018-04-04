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
