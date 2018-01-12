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
package org.eclipse.che.plugin.java.server.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.internal.ui.refactoring.PreviewNode;

/**
 * Helps to convert to DTOs related to refactoring.
 *
 * @author Valeriy Svydenko
 */
public class DtoConverter {

  /**
   * Converts {@link org.eclipse.ltk.core.refactoring.RefactoringStatus} to {@link
   * RefactoringStatus}.
   */
  public static RefactoringStatus toRefactoringStatusDto(
      org.eclipse.ltk.core.refactoring.RefactoringStatus refactoringStatus) {
    RefactoringStatus status = DtoFactory.newDto(RefactoringStatus.class);
    convertRefactoringStatus(status, refactoringStatus);
    return status;
  }

  /**
   * Converts {@link org.eclipse.ltk.core.refactoring.RefactoringStatus} to {@link
   * RefactoringResult}.
   */
  public static RefactoringResult toRefactoringResultDto(
      org.eclipse.ltk.core.refactoring.RefactoringStatus refactoringStatus) {
    RefactoringResult result = DtoFactory.newDto(RefactoringResult.class);
    convertRefactoringStatus(result, refactoringStatus);
    return result;
  }

  /** Converts {@link PreviewNode} to {@link RefactoringPreview}. */
  public static RefactoringPreview toRefactoringPreview(PreviewNode node) {
    RefactoringPreview dto = DtoFactory.newDto(RefactoringPreview.class);
    dto.setId(node.getId());
    dto.setText(node.getText());
    dto.setImage(node.getImageDescriptor().getImage());
    dto.setEnabled(true);
    PreviewNode[] children = node.getChildren();
    if (children != null && children.length > 0) {
      List<RefactoringPreview> list = new ArrayList<>(children.length);
      for (PreviewNode child : children) {
        list.add(toRefactoringPreview(child));
      }
      dto.setChildrens(list);
    }
    return dto;
  }

  private static void convertRefactoringStatus(
      RefactoringStatus dtoStatus,
      org.eclipse.ltk.core.refactoring.RefactoringStatus refactoringStatus) {
    dtoStatus.setSeverity(refactoringStatus.getSeverity());
    List<RefactoringStatusEntry> entryList =
        Arrays.stream(refactoringStatus.getEntries())
            .map(
                refactoringStatusEntry -> {
                  RefactoringStatusEntry entry = DtoFactory.newDto(RefactoringStatusEntry.class);
                  entry.setSeverity(refactoringStatusEntry.getSeverity());
                  entry.setMessage(refactoringStatusEntry.getMessage());
                  return entry;
                })
            .collect(Collectors.toList());
    dtoStatus.setEntries(entryList);
  }
}
