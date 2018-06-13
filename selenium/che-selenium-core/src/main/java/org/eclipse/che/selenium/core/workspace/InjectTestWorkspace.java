/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.che.selenium.core.user.DefaultTestUser;

/**
 * To instantiate {@link TestWorkspace} in test classes with none default parameters.
 *
 * @see TestWorkspaceProvider#createWorkspace(DefaultTestUser, int, String, boolean)
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
  String template() default WorkspaceTemplate.DEFAULT;

  /** The workspace owner. If value is empty then default user will be used. */
  String user() default "";

  /** Should we start workspace just after creation. */
  boolean startAfterCreation() default true;
}
