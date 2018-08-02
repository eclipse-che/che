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
package org.eclipse.che.ide.part.editor;

import org.eclipse.che.ide.api.parts.PartStackView;

/**
 * Tab with error and warning marks
 *
 * @author Oleksii Orel
 */
public interface TabItemWithMarks extends PartStackView.TabItem {

  /** Add error mark for Tab title */
  void setErrorMark(boolean isVisible);

  /** Add warning mark for Tab title */
  void setWarningMark(boolean isVisible);
}
