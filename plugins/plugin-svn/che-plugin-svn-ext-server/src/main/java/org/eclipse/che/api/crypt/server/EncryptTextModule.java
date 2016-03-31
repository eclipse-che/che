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
package org.eclipse.che.api.crypt.server;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import org.eclipse.che.inject.DynaModule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

@DynaModule
public class EncryptTextModule extends AbstractModule {

    /**
     * The binding name for the crypto preference list.
     */
    public static final String SCHEME_PREFERENCE_ORDER_NAME = "crypt_scheme_pref_order";

    // This could be made configurable with a property
    /**
     * The order in which cipher/algorithm should be considered for encryption.
     */
    private static List<Integer> SCHEME_PREFERENCE_ORDER = Arrays.asList(AES256WithSHA1EncryptTextService.SCHEME_VERSION,
                                                                         AES128WithSHA1EncryptTextService.SCHEME_VERSION,
                                                                         NoCryptTextService.SCHEME_VERSION);

    @Override
    protected void configure() {
        bind(EncryptTextServiceRegistry.class).to(EncryptTextServiceRegistryImpl.class).in(Singleton.class);

        final Multibinder<EncryptTextService> encryptMultibinder = Multibinder.newSetBinder(binder(), EncryptTextService.class);
        encryptMultibinder.addBinding().to(NoCryptTextService.class);
        encryptMultibinder.addBinding().to(AES256WithSHA1EncryptTextService.class);
        encryptMultibinder.addBinding().to(AES128WithSHA1EncryptTextService.class);
    }


    @Provides
    @Named(SCHEME_PREFERENCE_ORDER_NAME)
    List<Integer> getPreferredEncryptScheme() {
        return SCHEME_PREFERENCE_ORDER;
    }
}
