/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation James Blackburn (Broadcom
 * Corp.) - ongoing development
 * *****************************************************************************
 */
package org.eclipse.core.resources.mapping;

import java.util.ArrayList;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * A resource mapping supports the transformation of an application model object into its underlying
 * file system resources. It provides the bridge between a logical element and the physical
 * resource(s) into which it is stored but does not provide more comprehensive model access or
 * manipulations.
 *
 * <p>Mappings provide two means of model traversal. The {@link #accept} method can be used to visit
 * the resources that constitute the model object. Alternatively, a set or traversals can be
 * obtained by calling {@link #getTraversals}. A traversal contains a set of resources and a depth.
 * This allows clients (such a repository providers) to do optimal traversals of the resources
 * w.r.t. the operation that is being performed on the model object.
 *
 * <p>This class may be subclassed by clients.
 *
 * @see IResource
 * @see ResourceTraversal
 * @since 3.2
 */
public abstract class ResourceMapping extends PlatformObject {

  /**
   * Accepts the given visitor for all existing resources in this mapping. The visitor's {@link
   * IResourceVisitor#visit} method is called for each accessible resource in this mapping.
   *
   * @param context the traversal context
   * @param visitor the visitor
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>The visitor failed with this exception.
   *       <li>The traversals for this mapping could not be obtained.
   *     </ul>
   */
  public void accept(
      ResourceMappingContext context, IResourceVisitor visitor, IProgressMonitor monitor)
      throws CoreException {
    ResourceTraversal[] traversals = getTraversals(context, monitor);
    for (int i = 0; i < traversals.length; i++) traversals[i].accept(visitor);
  }

  /**
   * Return whether this resource mapping contains all the resources of the given mapping.
   *
   * <p>This method always returns <code>false</code> when the given resource mapping's model
   * provider id does not match that the of the receiver.
   *
   * @param mapping the given resource mapping
   * @return <code>true</code> if this mapping contains all the resources of the given mapping, and
   *     <code>false</code> otherwise.
   */
  public boolean contains(ResourceMapping mapping) {
    return false;
  }

  /**
   * Override equals to compare the model objects of the mapping in order to determine equality.
   *
   * @param obj the object to compare
   * @return <code>true</code> if the receiver is equal to the given object, and <code>false</code>
   *     otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj instanceof ResourceMapping) {
      ResourceMapping other = (ResourceMapping) obj;
      return other.getModelObject().equals(getModelObject());
    }
    return false;
  }

  /**
   * Returns all markers of the specified type on the resources in this mapping. If <code>
   * includeSubtypes</code> is <code>false</code>, only markers whose type exactly matches the given
   * type are returned. Returns an empty array if there are no matching markers.
   *
   * @param type the type of marker to consider, or <code>null</code> to indicate all types
   * @param includeSubtypes whether or not to consider sub-types of the given type
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @return an array of markers
   * @exception CoreException if this method fails.
   */
  public IMarker[] findMarkers(String type, boolean includeSubtypes, IProgressMonitor monitor)
      throws CoreException {
    final ResourceTraversal[] traversals =
        getTraversals(ResourceMappingContext.LOCAL_CONTEXT, monitor);
    ArrayList<IMarker> result = new ArrayList<IMarker>();
    for (int i = 0; i < traversals.length; i++)
      traversals[i].doFindMarkers(result, type, includeSubtypes);
    return result.toArray(new IMarker[result.size()]);
  }

  /**
   * Returns the application model element associated with this resource mapping.
   *
   * @return the application model element associated with this resource mapping.
   */
  public abstract Object getModelObject();

  /**
   * Return the model provider for the model object of this resource mapping. The model provider is
   * obtained using the id returned from <code>getModelProviderId()</code>.
   *
   * @return the model provider
   */
  public final ModelProvider getModelProvider() {
    //		try {
    //			return ModelProviderManager.getDefault().getModelProvider(getModelProviderId());
    //		} catch (CoreException e) {
    //			throw new IllegalStateException(e.getMessage());
    //		}
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the id of the model provider that generated this resource mapping.
   *
   * @return the model provider id
   */
  public abstract String getModelProviderId();

  /**
   * Returns the projects that contain the resources that constitute this application model.
   *
   * @return the projects
   */
  public abstract IProject[] getProjects();

  /**
   * Returns one or more traversals that can be used to access all the physical resources that
   * constitute the logical resource. A traversal is simply a set of resources and the depth to
   * which they are to be traversed. This method returns an array of traversals in order to provide
   * flexibility in describing the traversals that constitute a model element.
   *
   * <p>Subclasses should, when possible, include all resources that are or may be members of the
   * model element. For instance, a model element should return the same list of resources
   * regardless of the existence of the files on the file system. For example, if a logical resource
   * called "form" maps to "/p1/form.xml" and "/p1/form.java" then whether form.xml or form.java
   * existed, they should be returned by this method.
   *
   * <p>In some cases, it may not be possible for a model element to know all the resources that may
   * constitute the element without accessing the state of the model element in another location
   * (e.g. a repository). This method is provided with a context which, when provided, gives access
   * to the members of corresponding remote containers and the contents of corresponding remote
   * files. This gives the model element the opportunity to deduce what additional resources should
   * be included in the traversal.
   *
   * @param context gives access to the state of remote resources that correspond to local resources
   *     for the purpose of determining traversals that adequately cover the model element resources
   *     given the state of the model element in another location. This parameter may be <code>null
   *     </code>, in which case the implementor can assume that only the local resources are of
   *     interest to the client.
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @return a set of traversals that cover the resources that constitute the model element
   * @exception CoreException if the traversals could not be obtained.
   */
  public abstract ResourceTraversal[] getTraversals(
      ResourceMappingContext context, IProgressMonitor monitor) throws CoreException;

  /** Override hashCode to use the model object. */
  @Override
  public int hashCode() {
    return getModelObject().hashCode();
  }
}
