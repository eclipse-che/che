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
package org.eclipse.che.ide.api.machine;

import java.util.List;
import java.util.Set;

/**
 * Registry for {@link CommandPropertyValueProvider}s.
 *
 * @author Artem Zatsarynnyi
 * @see CommandPropertyValueProvider
 */
public interface CommandPropertyValueProviderRegistry {

    /** Register set of property value providers. */
    void register(Set<CommandPropertyValueProvider> valueProviders);

    /** Unregister specific property value provider. */
    void unregister(CommandPropertyValueProvider valueProvider);

    /** Returns keys of all registered {@link CommandPropertyValueProvider}s. */
    Set<String> getKeys();

    /** Returns {@link CommandPropertyValueProvider} by the given key. */
    CommandPropertyValueProvider getProvider(String key);

    /** Returns all registered {@link CommandPropertyValueProvider}s. */
    List<CommandPropertyValueProvider> getProviders();
}
