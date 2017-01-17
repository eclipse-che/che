/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.jdb.server.jdi;

import java.util.Comparator;

/**
 * @author Anatolii Bazko
 */
public class JdiFieldComparator implements Comparator<JdiField> {

    @Override
    public int compare(JdiField o1, JdiField o2) {
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
