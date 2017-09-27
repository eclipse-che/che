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
package org.eclipse.che.api.git.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/** @author Anatolii Bazko */
@DTO
public interface RevertResult {

  List<String> getConflicts();

  void setConflicts(List<String> conflicts);

  RevertResult withConflicts(List<String> conflicts);

  List<String> getRevertedCommits();

  void setRevertedCommits(List<String> revertedCommits);

  RevertResult withRevertedCommits(List<String> revertedCommits);

  String getNewHead();

  void setNewHead(String newHead);

  RevertResult withNewHead(String newHead);
}
