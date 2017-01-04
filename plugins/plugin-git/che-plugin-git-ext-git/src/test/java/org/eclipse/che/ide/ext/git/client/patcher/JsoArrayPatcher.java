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
package org.eclipse.che.ide.ext.git.client.patcher;

import com.googlecode.gwt.test.patchers.PatchClass;
import com.googlecode.gwt.test.patchers.PatchMethod;

import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Patcher for JsoArray class. Replace native method into Array.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@PatchClass(List.class)
public class JsoArrayPatcher {

    /** Patch create method. */
    @PatchMethod(override = true)
    public static <T> List<T> create() {
        return mock(List.class);
    }

    /** Patch add method. */
    @PatchMethod
    public static <T> void add(List array, T value) {
        // do nothing
    }
}