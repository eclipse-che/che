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

import com.google.common.annotations.Beta;
import io.opentracing.tag.IntTag;
import java.util.function.Supplier;
import org.eclipse.che.commons.annotation.Traced;

@Beta
public class AnnotationAwareIntTag extends IntTag {

  public AnnotationAwareIntTag(String key) {
    super(key);
  }

  public void set(Integer value) {
    set(() -> value);
  }

  public void set(Supplier<Integer> value) {
    Traced.Tags.addInteger(getKey(), value);
  }
}
