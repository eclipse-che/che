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
