/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.eclipse.che.selenium.core.user.TestUser;

/**
 * To instantiate {@link TestWorkspace} in test classes with none default parameters.
 *
 * @see TestWorkspaceProvider#createWorkspace(TestUser, int, WorkspaceTemplate, boolean)
 * @author Anatolii Bazko
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface InjectTestWorkspace {
  /**
   * Workspace memory in GB. If the value is less or equal to zero then the default value will be
   * used.
   */
  int memoryGb() default -1;

  /** Workspace template to create workspace base upon. */
  WorkspaceTemplate template() default WorkspaceTemplate.DEFAULT;

  /** The workspace owner. If value is empty then default user will be used. */
  String user() default "";

  /** Should we start workspace just after creation. */
  boolean startAfterCreation() default true;
}
