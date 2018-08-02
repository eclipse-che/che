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
package org.eclipse.che.ide.ext.java.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/** @author Evgen Vidolob */
@DTO
public interface ReconcileResult {

  List<Problem> getProblems();

  void setProblems(List<Problem> problems);

  ReconcileResult withProblems(List<Problem> problems);

  List<HighlightedPosition> getHighlightedPositions();

  void setHighlightedPositions(List<HighlightedPosition> positions);

  ReconcileResult withHighlightedPositions(List<HighlightedPosition> positions);

  String getFileLocation();

  void setFileLocation(String path);

  ReconcileResult withFileLocation(String path);
}
