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
package org.eclipse.che.commons.tracing;

import io.opentracing.tag.IntTag;
import org.eclipse.che.commons.annotation.Traced;

public class AnnotationAwareIntTag extends IntTag {

  public AnnotationAwareIntTag(String key) {
    super(key);
  }

  public void set(Integer value) {
    Traced.Tags.add(getKey(), value);
  }
}
