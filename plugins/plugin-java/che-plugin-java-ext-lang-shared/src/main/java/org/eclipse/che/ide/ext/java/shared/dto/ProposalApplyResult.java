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
package org.eclipse.che.ide.ext.java.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;

/**
 * A <code>ProposalApplyResult</code> object represents the result of a applying proposal operation.
 * It manages a list of <code>Change</code> objects and information about applied proposal. Each
 * <code>ChangeInfo</code> object describes one change that was applied.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@DTO
public interface ProposalApplyResult {
  /**
   * @return the change which was applied, can be <code>null</code> in rare cases if creation of the
   *     change failed
   */
  ChangeInfo getChangeInfo();

  void setChangeInfo(ChangeInfo changeInfo);

  List<Change> getChanges();

  void setChanges(List<Change> changes);

  Region getSelection();

  void setSelection(Region region);

  LinkedModeModel getLinkedModeModel();

  void setLinkedModeModel(LinkedModeModel model);
}
