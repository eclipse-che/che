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
package org.eclipse.che.plugin.svn.server.credentials;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.che.api.crypt.server.EncryptException;
import org.eclipse.che.api.crypt.server.EncryptTextModule;
import org.eclipse.che.api.crypt.server.EncryptTextService;
import org.eclipse.che.api.crypt.server.EncryptTextServiceRegistry;
import org.eclipse.che.api.crypt.shared.EncryptResult;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.svn.shared.credentials.RepositoryCredentials;
import org.eclipse.che.plugin.svn.shared.credentials.UserCredentialStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencesCredentialsProvider implements CredentialsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PreferencesCredentialsProvider.class);

    private static final String SVN_CREDENTIALS = "svn_credentials";

    private final EncryptTextServiceRegistry encryptTextServiceRegistry;

    private final CurrentUserPreferencesAccess preferencesAccess;

    @Inject
    public PreferencesCredentialsProvider(final CurrentUserPreferencesAccess preferencesAccess,
                                          final EncryptTextServiceRegistry encryptTextServiceRegistry) {
        this.encryptTextServiceRegistry = encryptTextServiceRegistry;
        this.preferencesAccess = preferencesAccess;
    }

    @Override
    public Credentials getCredentials(final String repositoryUrl) throws CredentialsException {
        LOG.debug("getCredentials called for " + repositoryUrl);
        String serializedCredentials;
        try {
            serializedCredentials = this.preferencesAccess.getPreference(SVN_CREDENTIALS);
        } catch (final PreferencesAccessException e) {
            throw new CredentialsException(e);
        }
        LOG.debug("getCredentials - {} read: {}", SVN_CREDENTIALS, serializedCredentials);
        if (serializedCredentials == null) {
            return null;
        }
        final DtoFactory dtoFactory = DtoFactory.getInstance();
        final UserCredentialStore userCredentials = dtoFactory.createDtoFromJson(serializedCredentials, UserCredentialStore.class);
        if (userCredentials == null || userCredentials.getRepositoriesCredentials() == null) {
            return null;
        }
        LOG.debug("getCredentials - user credentials found with keys:{}",
                  Arrays.toString(userCredentials.getRepositoriesCredentials().keySet().toArray()));
        final RepositoryCredentials repositoryCreds = userCredentials.getRepositoriesCredentials().get(repositoryUrl);
        if (repositoryCreds == null) {
            LOG.debug("getCredentials - no credentials found for " + repositoryUrl);
            return null;
        }
        LOG.debug("getCredentials - repository credentials found for " + repositoryUrl);
        String username = "";
        if (repositoryCreds.getUsername() != null) {
            username = repositoryCreds.getUsername();
        }
        LOG.debug("getCredentials - got username:" + username);
        final int schemeVersion = repositoryCreds.getEncryptionSchemeVersion();
        final EncryptTextService encryptService = this.encryptTextServiceRegistry.getService(schemeVersion);
        if (encryptService == null) {
            throw new CredentialsException("No encryption service matches the specified one");
        }
        final String encryptedPassword = repositoryCreds.getEncryptedPassword();
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            LOG.debug("getCredentials - got empty password");
            return new Credentials(username, encryptedPassword);
        }
        final EncryptResult toDecrypt = new EncryptResult(encryptedPassword, repositoryCreds.getInitVector());
        String password;
        try {
            password = encryptService.decryptText(toDecrypt);
        } catch (final EncryptException e) {
            LOG.warn("getCredentials - decrypt password failed");
            throw new CredentialsException(e);
        }
        LOG.debug("getCredentials: found {}, ####", username);
        return new Credentials(username, password);
    }

    @Override
    public void storeCredential(final String repositoryUrl, final Credentials credentials) throws CredentialsException {
        LOG.debug("storeCredentials called for " + repositoryUrl);
        if (credentials == null) {
            return;
        }

        final DtoFactory dtoFactory = DtoFactory.getInstance();

        String oldSerializedCredentials;
        try {
            oldSerializedCredentials = this.preferencesAccess.getPreference(SVN_CREDENTIALS);
        } catch (final PreferencesAccessException e) {
            throw new CredentialsException(e);
        }

        // analyze the structure and build what's missing
        UserCredentialStore userCredentials;
        if (oldSerializedCredentials != null) {
            userCredentials = dtoFactory.createDtoFromJson(oldSerializedCredentials, UserCredentialStore.class);
        } else {
            userCredentials = dtoFactory.createDto(UserCredentialStore.class);
        }
        Map<String, RepositoryCredentials> repositoriesCredentials = userCredentials.getRepositoriesCredentials();
        if (repositoriesCredentials == null) {
            repositoriesCredentials = new HashMap<>();
            userCredentials.setRepositoriesCredentials(repositoriesCredentials);
        }
        RepositoryCredentials repositoryCreds = repositoriesCredentials.get(repositoryUrl);
        if (repositoryCreds == null) {
            repositoryCreds = dtoFactory.createDto(RepositoryCredentials.class);
            repositoriesCredentials.put(repositoryUrl, repositoryCreds);
        }

        // set the values
        repositoryCreds.setUsername(credentials.getUsername());

        final EncryptTextService encryptService = this.encryptTextServiceRegistry.getPreferredService();
        if (encryptService == null) {
            throw new CredentialsException("No encryption service matches the specified one");
        }
        EncryptResult encryptResult;
        try {
            encryptResult = encryptService.encryptText(credentials.getPassword());
        } catch (final EncryptException e) {
            throw new CredentialsException(e);
        }
        repositoryCreds.setEncryptedPassword(encryptResult.getCipherText());
        repositoryCreds.setInitVector(encryptResult.getInitVector());
        repositoryCreds.setEncryptionSchemeVersion(encryptService.getSchemeVersion());

        // store the new version in the preferences
        final String newSerializedCredentials = dtoFactory.toJson(userCredentials);

        try {
            this.preferencesAccess.updatePreference(SVN_CREDENTIALS, newSerializedCredentials);
            LOG.debug("storeCredentials done " + newSerializedCredentials);
        } catch (final PreferencesAccessException e) {
            throw new CredentialsException(e);
        }
    }

}
