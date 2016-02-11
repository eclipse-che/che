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
package org.eclipse.che.commons.env;

import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.commons.user.User;

/**
 * <p>Defines a component that holds variables of type {@link ThreadLocal}
 * whose value is required by the component to work normally and cannot be recovered.
 * This component is mainly used when we want to do a task asynchronously, in that case
 * to ensure that the task will be executed in the same conditions as if it would be
 * executed synchronously we need to transfer the thread context from the original
 * thread to the executor thread.</p>
 */
public class EnvironmentContext {

    /** ThreadLocal keeper for EnvironmentContext. */
    private static ThreadLocal<EnvironmentContext> current = new ThreadLocal<EnvironmentContext>() {
        @Override
        protected EnvironmentContext initialValue() {
            return new EnvironmentContext();
        }
    };

    static {
        ThreadLocalPropagateContext.addThreadLocal(current);
    }

    public static EnvironmentContext getCurrent() {
        return current.get();
    }

    public static void setCurrent(EnvironmentContext environment) {
        current.set(environment);
    }

    public static void reset() {
        current.remove();
    }


    private User user;

    private String workspaceName;

    private String workspaceId;

    private boolean workspaceTemporary;

    private String accountId;

    public EnvironmentContext() {
    }

    public EnvironmentContext(EnvironmentContext other) {
        setUser(other.getUser());
        setWorkspaceName(other.getWorkspaceName());
        setWorkspaceId(other.getWorkspaceId());
        setAccountId(other.getAccountId());
        setWorkspaceTemporary(other.isWorkspaceTemporary());
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isWorkspaceTemporary() {
        return workspaceTemporary;
    }

    public void setWorkspaceTemporary(boolean workspaceTemporary) {
        this.workspaceTemporary = workspaceTemporary;
    }
}
