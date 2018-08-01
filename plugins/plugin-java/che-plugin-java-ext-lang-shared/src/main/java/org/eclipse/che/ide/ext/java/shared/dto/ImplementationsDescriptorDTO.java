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
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;

/**
 * DTO represents the information about implementing members.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface ImplementationsDescriptorDTO {

  /** Returns name of implemented member. */
  String getMemberName();

  void setMemberName(String memberName);

  ImplementationsDescriptorDTO withMemberName(String memberName);

  /** Returns all implementations. */
  List<Type> getImplementations();

  void setImplementations(List<Type> implementations);

  ImplementationsDescriptorDTO withImplementations(List<Type> implementations);
}
