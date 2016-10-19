/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.macro;

import java.util.List;
import java.util.Set;

/**
 * Registry for {@link Macro}s.
 *
 * @author Artem Zatsarynnyi
 * @see Macro
 */
public interface MacroRegistry {

    /** Register set of macros. */
    void register(Set<Macro> macros);

    /** Unregister the given macro. */
    void unregister(Macro macro);

    /** Returns the names of all registered {@link Macro}s. */
    Set<String> getNames();

    /** Returns {@link Macro} by it's name. */
    Macro getMacro(String name);

    /** Returns all registered {@link Macro}s. */
    List<Macro> getMacros();
}
