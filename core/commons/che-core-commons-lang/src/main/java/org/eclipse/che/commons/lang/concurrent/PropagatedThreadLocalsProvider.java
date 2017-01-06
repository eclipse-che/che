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
package org.eclipse.che.commons.lang.concurrent;

/**
 * Helps to identify classes which need propagating ThreadLocal variables with ThreadLocalPropagateContext. Useful to avoid manual
 * registration of ThreadLocal in ThreadLocalPropagateContext.
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
