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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;

/**
 * //
 *
 * @author Vitalii Parfonov
 */
public class BaseProjectTypeTest {

    public class PrimaryType extends ProjectTypeDef {

        public final static String PRIMARY_ID = "primaryId";

        public final static String PRIMARY_NAME = "primaryName";

        public PrimaryType() {
            this(PRIMARY_ID, PRIMARY_NAME);
        }

        public PrimaryType(String id, String displayName) {
            super(id, displayName, true, false, true);
        }
    }

    public class PersistedMixin extends ProjectTypeDef {

        public final static String PERSISTED_MIXIN_ID             = "persistedMixinId";
        public final static String PERSISTED_MIXIN_NAME           = "persistedMixinName";
        public final static String PERSISTED_MIXIN_ATTRIBUTE_NAME = "persistedMixinAttributeName";
        public final static String PERSISTED_MIXIN_ATTRIBUTE_ID   = PERSISTED_MIXIN_ID + ":" + PERSISTED_MIXIN_ATTRIBUTE_NAME;

        public PersistedMixin() {
            this(PERSISTED_MIXIN_ID, PERSISTED_MIXIN_NAME);
        }

        public PersistedMixin(String id, String displayName) {
            super(id, displayName, false, true, true);
            addConstantDefinition(PERSISTED_MIXIN_ATTRIBUTE_NAME, "", "");
        }
    }

    public class NotPersistedMixin extends ProjectTypeDef {

        public final static String NOT_PERSISTED_MIXIN_ID             = "notPersistedMixinId";
        public final static String NOT_PERSISTED_MIXIN_NAME           = "notPersistedMixinName";
        public final static String NOT_PERSISTED_MIXIN_ATTRIBUTE_NAME = "notPersistedMixinAttributeName";
        public final static String NOT_PERSISTED_MIXIN_ATTRIBUTE_ID   = NOT_PERSISTED_MIXIN_ID + ":" + NOT_PERSISTED_MIXIN_ATTRIBUTE_NAME;

        public NotPersistedMixin() {
            this(NOT_PERSISTED_MIXIN_ID, NOT_PERSISTED_MIXIN_NAME);
        }

        public NotPersistedMixin(String id, String displayName) {
            super(id, displayName, false, true, false);
            addConstantDefinition(NOT_PERSISTED_MIXIN_ATTRIBUTE_NAME, "", "");
        }
    }
}
