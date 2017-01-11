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
package org.eclipse.che.plugin.machine.ssh.jsch;

import com.jcraft.jsch.UserInfo;

/**
 * Implementation of {@link UserInfo}.
 *
 * @author Alexander Garagatyi
 */
public class JschUserInfoImpl implements UserInfo {
    private final String  password;
    private final boolean promptPassword;
    private final String  passphrase;
    private final boolean promptPassphrase;
    private final boolean promptYesNo;

    private JschUserInfoImpl(String password,
                             boolean promptPassword,
                             String passphrase,
                             boolean promptPassphrase,
                             boolean promptYesNo) {
        this.password = password;
        this.promptPassword = promptPassword;
        this.passphrase = passphrase;
        this.promptPassphrase = promptPassphrase;
        this.promptYesNo = promptYesNo;
    }

    public static JschUserInfoImplBuilder builder() {
        return new JschUserInfoImplBuilder();
    }

    @Override
    public String getPassphrase() {
        return passphrase;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean promptPassword(String message) {
        return promptPassword;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return promptPassphrase;
    }

    @Override
    public boolean promptYesNo(String message) {
        return promptYesNo;
    }

    @Override
    public void showMessage(String message) {}

    public static class JschUserInfoImplBuilder {
        private String  password;
        private String  passphrase;
        private boolean promptPassword;
        private boolean promptPassphrase;
        private boolean promptYesNo;

        private JschUserInfoImplBuilder() {}

        public JschUserInfoImpl build() {
            return new JschUserInfoImpl(password, promptPassword, passphrase, promptPassphrase, promptYesNo);
        }

        public JschUserInfoImplBuilder password(String password) {
            this.password = password;
            return this;
        }

        public JschUserInfoImplBuilder passphrase(String passphrase) {
            this.passphrase = passphrase;
            return this;
        }

        public JschUserInfoImplBuilder promptPassword(boolean promptPassword) {
            this.promptPassword = promptPassword;
            return this;
        }

        public JschUserInfoImplBuilder promptPassphrase(boolean promptPassphrase) {
            this.promptPassphrase = promptPassphrase;
            return this;
        }

        public JschUserInfoImplBuilder promptYesNo(boolean promptYesNo) {
            this.promptYesNo = promptYesNo;
            return this;
        }
    }
}
