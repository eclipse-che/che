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
package org.eclipse.che.ide.orion.compare;

import com.google.inject.Singleton;

import org.eclipse.che.ide.orion.compare.jso.CompareConfigJs;
import org.eclipse.che.ide.orion.compare.jso.FileOptionsJs;

/**
 * Implementation for {@link CompareFactory}.
 * This implementation creates JSO objects.
 *
 * @author Evgen Vidolob
 */
@Singleton
class CompareFactoryImpl implements CompareFactory {


    @Override
    public FileOptions createFieOptions() {
        return FileOptionsJs.createObject().<FileOptionsJs>cast();
    }

    @Override
    public CompareConfig createCompareConfig() {
        return CompareConfigJs.createObject().<CompareConfigJs>cast();
    }
}
