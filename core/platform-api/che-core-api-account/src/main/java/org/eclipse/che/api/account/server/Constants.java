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
package org.eclipse.che.api.account.server;

/**
 * Constants for Account API
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
public final class Constants {

    public static final String LINK_REL_CREATE_ACCOUNT        = "create";
    public static final String LINK_REL_GET_ACCOUNT_BY_ID     = "get by id";
    public static final String LINK_REL_GET_ACCOUNT_BY_NAME   = "get by name";
    public static final String LINK_REL_UPDATE_ACCOUNT        = "update";
    public static final String LINK_REL_REMOVE_ACCOUNT        = "remove";
    public static final String LINK_REL_GET_MEMBERS           = "members";
    public static final String LINK_REL_ADD_MEMBER            = "add member";
    public static final String LINK_REL_REMOVE_MEMBER         = "remove member";
    public static final String LINK_REL_GET_ACCOUNTS          = "get accounts";
    public static final String LINK_REL_ADD_ATTRIBUTE         = "add attribute";
    public static final String LINK_REL_REMOVE_ATTRIBUTE      = "remove attribute";
    public static final int    ID_LENGTH                      = 16;
    public static final String RESOURCES_LOCKED_PROPERTY      = "codenvy:resources_locked";
    public static final String PAYMENT_LOCKED_PROPERTY        = "codenvy:payment_locked";

    private Constants() {}
}
