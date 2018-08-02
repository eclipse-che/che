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
package org.eclipse.che.plugin.jdb.server.model;

import java.util.Comparator;
import org.eclipse.che.api.debug.shared.model.Field;

/** @author Anatolii Bazko */
public class JdbFieldComparator implements Comparator<Field> {

  @Override
  public int compare(Field o1, Field o2) {
    final boolean thisStatic = o1.isIsStatic();
    final boolean thatStatic = o2.isIsStatic();
    if (thisStatic && !thatStatic) {
      return -1;
    }
    if (!thisStatic && thatStatic) {
      return 1;
    }
    final String thisName = o1.getName();
    final String thatName = o2.getName();
    return thisName.compareTo(thatName);
  }
}
