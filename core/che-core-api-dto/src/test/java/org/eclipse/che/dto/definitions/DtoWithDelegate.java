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
package org.eclipse.che.dto.definitions;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.DelegateRule;
import org.eclipse.che.dto.shared.DelegateTo;

/**
 * @author andrew00x
 * @author Alexander Garagatyi
 */
@DTO
public interface DtoWithDelegate extends TestInterface {
  String getFirstName();

  void setFirstName(String firstName);

  DtoWithDelegate withFirstName(String firstName);

  String getLastName();

  void setLastName(String lastName);

  DtoWithDelegate withLastName(String lastName);

  @DelegateTo(
    client = @DelegateRule(type = Util.class, method = "addPrefix"),
    server = @DelegateRule(type = Util.class, method = "addPrefix")
  )
  String nameWithPrefix(String prefix);

  //    @Override
  //    @DelegateTo(client = @DelegateRule(type = Util.class, method = "getFullName"),
  //                server = @DelegateRule(type = Util.class, method = "getFullName"))
  //    String getFullName();
}
