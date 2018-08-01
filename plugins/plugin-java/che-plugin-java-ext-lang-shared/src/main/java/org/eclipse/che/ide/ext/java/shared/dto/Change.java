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

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents the information about the proposal change.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@DTO
public interface Change {

  /** Returns the offset of the change. */
  int getOffset();

  void setOffset(int offset);

  Change withOffset(int offset);

  /** Returns length of the text change. */
  int getLength();

  void setLength(int length);

  Change withLength(int length);

  /** Returns text of the change. */
  String getText();

  void setText(String text);

  Change withText(String text);
}
