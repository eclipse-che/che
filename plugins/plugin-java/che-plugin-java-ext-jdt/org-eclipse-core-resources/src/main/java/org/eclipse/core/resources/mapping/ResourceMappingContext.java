/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.resources.mapping;

/**
 * A resource mapping context is provided to a resource mapping when traversing the resources of the
 * mapping. The type of context may determine what resources are included in the traversals of a
 * mapping.
 *
 * <p>There are currently two resource mapping contexts: the local mapping context (represented by
 * the singleton {@link #LOCAL_CONTEXT}), and {@link RemoteResourceMappingContext}. Implementors of
 * {@link org.eclipse.core.resources.mapping.ResourceMapping} should not assume that these are the
 * only valid contexts (in order to allow future extensibility). Therefore, if the provided context
 * is not of one of the above mentioned types, the implementor can assume that the context is a
 * local context.
 *
 * <p>This class may be subclassed by clients; this class is not intended to be instantiated
 * directly.
 *
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * @see RemoteResourceMappingContext
 * @since 3.2
 */
public class ResourceMappingContext {

  /**
   * This resource mapping context is used to indicate that the operation that is requesting the
   * traversals is performing a local operation. Because the operation is local, the resource
   * mapping is free to be as precise as desired about what resources make up the mapping without
   * concern for performing optimized remote operations.
   */
  public static final ResourceMappingContext LOCAL_CONTEXT = new ResourceMappingContext();
}
