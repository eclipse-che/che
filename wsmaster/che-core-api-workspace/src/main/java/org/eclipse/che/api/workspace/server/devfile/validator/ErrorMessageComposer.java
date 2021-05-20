/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile.validator;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.List;
import org.leadpony.justify.api.Problem;

/**
 * Helps to convert json schema validation result list of {@link Problem} into an error message
 * string.
 */
public class ErrorMessageComposer {

  /**
   * Parses {@link Problem} list into an error string. Each problem is recursively parsed to extract
   * nested errors if any.
   *
   * @param validationErrors Schema validation problems list
   * @return composite error string
   */
  public String extractMessages(List<Problem> validationErrors, StringBuilder messageBuilder) {
    for (Problem problem : validationErrors) {
      int branchCount = problem.countBranches();
      if (branchCount == 0) {
        messageBuilder.append(getMessage(problem));
      } else {
        messageBuilder.append(problem.getMessage()).append(": [");
        for (int i = 0; i < branchCount; i++) {
          extractMessages(problem.getBranch(i), messageBuilder);
        }
        messageBuilder.append("]");
      }
    }
    return messageBuilder.toString();
  }

  private String getMessage(Problem problem) {
    StringBuilder messageBuilder = new StringBuilder();
    if (!isNullOrEmpty(problem.getPointer())) {
      messageBuilder.append("(").append(problem.getPointer()).append("):");
    }
    messageBuilder.append(problem.getMessage());
    return messageBuilder.toString();
  }
}
