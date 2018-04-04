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
package org.eclipse.che.api.project.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface TreeElement {
  ItemReference getNode();

  void setNode(ItemReference node);

  TreeElement withNode(ItemReference node);

  List<TreeElement> getChildren();

  void setChildren(List<TreeElement> children);

  TreeElement withChildren(List<TreeElement> children);
}
