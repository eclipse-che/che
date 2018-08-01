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

/** @author Evgen Vidolob */
@DTO
public interface ProposalPresentation {

  String getImage();

  void setImage(String image);

  String getDisplayString();

  void setDisplayString(String displayString);

  boolean isAutoInsertable();

  void setAutoInsertable(boolean insertable);

  int getIndex();

  void setIndex(int index);

  String getActionId();

  void setActionId(String actionId);
}
