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
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO that represents creation refactoring change.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface ChangeCreationResult {

  /**
   * @return true if wizard can show preview page or finish refactoring. if false wizard must show
   *     error page.
   */
  boolean isCanShowPreviewPage();

  void setCanShowPreviewPage(boolean preview);

  /** @return status of creation refactoring change. */
  RefactoringStatus getStatus();

  void setStatus(RefactoringStatus status);
}
