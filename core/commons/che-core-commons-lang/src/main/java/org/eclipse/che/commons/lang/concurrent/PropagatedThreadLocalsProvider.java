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
package org.eclipse.che.commons.lang.concurrent;

/**
 * Helps to identify classes which need propagating ThreadLocal variables with
 * ThreadLocalPropagateContext. Useful to avoid manual registration of ThreadLocal in
 * ThreadLocalPropagateContext.
 *
 * <pre>
 * Set&lt;PropagatedThreadLocalsProvider&gt; ps = ... // Look up all implementations of PropagatedThreadLocalsProvider
 * for (PropagatedThreadLocalsProvider p : ps) {
 *     for (ThreadLocal tl : p.getThreadLocals()) {
 *         ThreadLocalPropagateContext.addThreadLocal(tl);
 *     }
 * }
 * </pre>
 *
 * @author andrew00x
 */
public interface PropagatedThreadLocalsProvider {
  ThreadLocal<?>[] getThreadLocals();
}
