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
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * A <code>RefactoringResult</code> object represents the result of a refactoring operation. It
 * manages a list of <code>ChangeInfo</code> objects. Each <code>ChangeInfo</code> object describes
 * one change that was applied.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface RefactoringResult extends RefactoringStatus {
  /** @return list of the changes which were applied. */
  List<ChangeInfo> getChanges();

  void setChanges(List<ChangeInfo> changes);
}
