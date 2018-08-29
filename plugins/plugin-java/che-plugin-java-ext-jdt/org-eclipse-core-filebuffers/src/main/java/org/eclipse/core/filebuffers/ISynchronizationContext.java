/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.filebuffers;

/**
 * A file buffer manager (see {@link org.eclipse.core.filebuffers.IFileBufferManager}) uses an
 * <code>ISynchronizationContext</code> in order to execute commands encapsulated as {@link
 * Runnable}. The synchronization context executes the <code>Runnables</code> according to a
 * specific synchronization/execution policy. This could be that the given <code>Runnable</code> is
 * executed in a specific thread or environment or adhere to specific timing constraints. The
 * concrete characteristics of the policy is to be specified by the context implementer.
 *
 * <p>This interface can be implemented by clients. Clients use {@link
 * org.eclipse.core.filebuffers.IFileBufferManager#setSynchronizationContext(ISynchronizationContext)}
 * to install a particular synchronization context with a file buffer manager.
 *
 * @since 3.0
 */
public interface ISynchronizationContext {

  /**
   * Executes the given runnable according to the specified synchronization/execution policy.
   *
   * @param runnable the runnable to be executed
   */
  void run(Runnable runnable);
}
