/**
 * ***************************************************************************** Copyright (c)
 * 2012-2018 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.resources;

/**
 * A description of a file info matcher.
 *
 * @since 3.6
 */
public final class FileInfoMatcherDescription {

  private String id;

  private Object arguments;

  public FileInfoMatcherDescription(String id, Object arguments) {
    super();
    this.id = id;
    this.arguments = arguments;
  }

  public Object getArguments() {
    return arguments;
  }

  public String getId() {
    return id;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    FileInfoMatcherDescription other = (FileInfoMatcherDescription) obj;
    if (arguments == null) {
      if (other.arguments != null) return false;
    } else if (!arguments.equals(other.arguments)) return false;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    return true;
  }
}
