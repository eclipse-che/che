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
package org.eclipse.che.ide.api.extension;

/**
 * Extension Definition annotation. Used to mark class as Extension and declare it's description
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
public @interface Extension {

  /** @return Extension version */
  String version() default "1.0";

  /** @return Extension title */
  String title();

  /** @return Extension brief description */
  String description() default "";
}
