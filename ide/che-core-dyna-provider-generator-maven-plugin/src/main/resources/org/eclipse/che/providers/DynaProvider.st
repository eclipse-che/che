/*
 * Copyright (c) 2012-2016 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package <packageName>;

import com.google.inject.Provider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.HashMap;

// File has been generated automatically by Eclipse Che Dyna provider generator plugin
@Singleton
@SuppressWarnings("unchecked")
public class <className> implements org.eclipse.che.providers.DynaProvider {

    private final Map\<String, Provider\<?>> providers = new HashMap\<>();

    @Override
    public \<T> Provider\<T> getProvider(String className) {
        return (Provider\<T>)providers.get(className);
    }

    @Inject
    public <className>(<if(!classes.empty)>
        <classes: {clazz | Provider\<<clazz.name>> <clazz.varName>};  separator=",\n"><endif>) {

       <classes : {clazz | providers.put("<clazz.name>", <clazz.varName>);}; separator="\n">
    }

}
