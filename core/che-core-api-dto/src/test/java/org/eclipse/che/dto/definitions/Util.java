/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.dto.definitions;

/**
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public class Util {
  public static String addPrefix(DtoWithDelegate dto, String prefix) {
    return prefix + dto.getFirstName();
  }

  public static String getFullName(DtoWithDelegate dto) {
    return dto.getFirstName() + dto.getLastName();
  }
}
