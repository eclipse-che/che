/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Serge Beauchamp (Freescale
 * Semiconductor) - [229633] Project Path Variable Support
 * *****************************************************************************
 */
package org.eclipse.core.resources;

import java.net.URI;
import org.eclipse.core.runtime.*;

/**
 * Manages a collection of path variables and resolves paths containing a variable reference.
 *
 * <p>A path variable is a pair of non-null elements (name,value) where name is a case-sensitive
 * string (containing only letters, digits and the underscore character, and not starting with a
 * digit), and value is an absolute <code>IPath</code> object.
 *
 * <p>Path variables allow for the creation of relative paths whose exact location in the file
 * system depends on the value of a variable. A variable reference may only appear as the first
 * segment of a relative path.
 *
 * @see org.eclipse.core.runtime.IPath
 * @since 2.1
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IPathVariableManager {

  /**
   * Converts an absolute path to path relative to some defined variable. For example, converts
   * "C:/foo/bar.txt" into "FOO/bar.txt", granted that the path variable "FOO" value is "C:/foo".
   *
   * <p>The "force" argument will cause an intermediate path variable to be created if the given
   * path can be relative only to a parent of an existing path variable. For example, if the path
   * "C:/other/file.txt" is to be converted and no path variables point to "C:/" or "C:/other" but
   * "FOO" points to "C:/foo", an intermediate "OTHER" variable will be created relative to "FOO"
   * containing the value "${PARENT-1-FOO}" so that the final path returned will be
   * "OTHER/file.txt".
   *
   * <p>The argument "variableHint" can be used to specify the name of the path variable to make the
   * provided path relative to.
   *
   * @param path The absolute path to be converted
   * @param force indicates whether intermediate path variables should be created if the path is
   *     relative only to a parent of an existing path variable.
   * @param variableHint The name of the variable to which the path should be made relative to, or
   *     <code>null</code> for the nearest one.
   * @return The converted path
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>The variable name is not valid
   *     </ul>
   *
   * @since 3.6
   */
  public URI convertToRelative(URI path, boolean force, String variableHint) throws CoreException;

  /**
   * Sets the path variable with the given name to be the specified value. Depending on the value
   * given and if the variable is currently defined or not, there are several possible outcomes for
   * this operation:
   *
   * <p>
   *
   * <ul>
   *   <li>A new variable will be created, if there is no variable defined with the given name, and
   *       the given value is not <code>null</code>.
   *   <li>The referred variable's value will be changed, if it already exists and the given value
   *       is not <code>null</code>.
   *   <li>The referred variable will be removed, if a variable with the given name is currently
   *       defined and the given value is <code>null</code>.
   *   <li>The call will be ignored, if a variable with the given name is not currently defined and
   *       the given value is <code>null</code>, or if it is defined but the given value is equal to
   *       its current value.
   * </ul>
   *
   * <p>If a variable is effectively changed, created or removed by a call to this method,
   * notification will be sent to all registered listeners.
   *
   * @param name the name of the variable
   * @param value the value for the variable (may be <code>null</code>)
   * @exception CoreException if this method fails. Reasons include:
   * @deprecated use setValue(String, URI) instead.
   *     <ul>
   *       <li>The variable name is not valid
   *       <li>The variable value is relative
   *     </ul>
   */
  @Deprecated
  public void setValue(String name, IPath value) throws CoreException;

  /**
   * Sets the path variable with the given name to be the specified value. Depending on the value
   * given and if the variable is currently defined or not, there are several possible outcomes for
   * this operation:
   *
   * <p>
   *
   * <ul>
   *   <li>A new variable will be created, if there is no variable defined with the given name, and
   *       the given value is not <code>null</code>.
   *   <li>The referred variable's value will be changed, if it already exists and the given value
   *       is not <code>null</code>.
   *   <li>The referred variable will be removed, if a variable with the given name is currently
   *       defined and the given value is <code>null</code>.
   *   <li>The call will be ignored, if a variable with the given name is not currently defined and
   *       the given value is <code>null</code>, or if it is defined but the given value is equal to
   *       its current value.
   * </ul>
   *
   * <p>If a variable is effectively changed, created or removed by a call to this method,
   * notification will be sent to all registered listeners.
   *
   * @param name the name of the variable
   * @param value the value for the variable (may be <code>null</code>)
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>The variable name is not valid
   *       <li>The variable value is relative
   *     </ul>
   *
   * @since 3.6
   */
  public void setURIValue(String name, URI value) throws CoreException;

  /**
   * Returns the value of the path variable with the given name. If there is no variable defined
   * with the given name, returns <code>null</code>.
   *
   * @param name the name of the variable to return the value for
   * @return the value for the variable, or <code>null</code> if there is no variable defined with
   *     the given name
   * @deprecated use getURIValue(String) instead.
   */
  @Deprecated
  public IPath getValue(String name);

  /**
   * Returns the value of the path variable with the given name. If there is no variable defined
   * with the given name, returns <code>null</code>.
   *
   * @param name the name of the variable to return the value for
   * @return the value for the variable, or <code>null</code> if there is no variable defined with
   *     the given name
   * @since 3.6
   */
  public URI getURIValue(String name);

  /**
   * Returns an array containing all defined path variable names.
   *
   * @return an array containing all defined path variable names
   */
  public String[] getPathVariableNames();

  //	Should be added for 3.6
  //	public String[] getPathVariableNames(String name);

  /**
   * Registers the given listener to receive notification of changes to path variables. The listener
   * will be notified whenever a variable has been added, removed or had its value changed. Has no
   * effect if an identical path variable change listener is already registered.
   *
   * @param listener the listener
   * @see IPathVariableChangeListener
   */
  public void addChangeListener(IPathVariableChangeListener listener);

  /**
   * Removes the given path variable change listener from the listeners list. Has no effect if an
   * identical listener is not registered.
   *
   * @param listener the listener
   * @see IPathVariableChangeListener
   */
  public void removeChangeListener(IPathVariableChangeListener listener);

  /**
   * Resolves a relative <code>URI</code> object potentially containing a variable reference as its
   * first segment, replacing the variable reference (if any) with the variable's value (which is a
   * concrete absolute URI). If the given URI is absolute or has a non- <code>null</code> device
   * then no variable substitution is done and that URI is returned as is. If the given URI is
   * relative and has a <code>null</code> device, but the first segment does not correspond to a
   * defined variable, then the URI is returned as is.
   *
   * <p>If the given URI is <code>null</code> then <code>null</code> will be returned. In all other
   * cases the result will be non-<code>null</code>.
   *
   * @param uri the URI to be resolved
   * @return the resolved URI or <code>null</code>
   * @since 3.2
   */
  public URI resolveURI(URI uri);

  /**
   * Resolves a relative <code>IPath</code> object potentially containing a variable reference as
   * its first segment, replacing the variable reference (if any) with the variable's value (which
   * is a concrete absolute path). If the given path is absolute or has a non- <code>null</code>
   * device then no variable substitution is done and that path is returned as is. If the given path
   * is relative and has a <code>null</code> device, but the first segment does not correspond to a
   * defined variable, then the path is returned as is.
   *
   * <p>If the given path is <code>null</code> then <code>null</code> will be returned. In all other
   * cases the result will be non-<code>null</code>.
   *
   * <p>For example, consider the following collection of path variables:
   *
   * <ul>
   *   <li>TEMP = c:/temp
   *   <li>BACKUP = /tmp/backup
   * </ul>
   *
   * <p>The following paths would be resolved as:
   *
   * <p>c:/bin => c:/bin
   *
   * <p>c:TEMP => c:TEMP
   *
   * <p>/TEMP => /TEMP
   *
   * <p>TEMP => c:/temp
   *
   * <p>TEMP/foo => c:/temp/foo
   *
   * <p>BACKUP => /tmp/backup
   *
   * <p>BACKUP/bar.txt => /tmp/backup/bar.txt
   *
   * <p>SOMEPATH/foo => SOMEPATH/foo
   *
   * @param path the path to be resolved
   * @return the resolved path or <code>null</code>
   * @deprecated use resolveURI(URI) instead.
   */
  @Deprecated
  public IPath resolvePath(IPath path);

  /**
   * Returns <code>true</code> if the given variable is defined and <code>false</code> otherwise.
   * Returns <code>false</code> if the given name is not a valid path variable name.
   *
   * @param name the variable's name
   * @return <code>true</code> if the variable exists, <code>false</code> otherwise
   */
  public boolean isDefined(String name);

  /**
   * Returns whether a variable is user defined or not.
   *
   * @return true if the path is user defined.
   * @since 3.6
   */
  public boolean isUserDefined(String name);

  /**
   * Validates the given name as the name for a path variable. A valid path variable name is made
   * exclusively of letters, digits and the underscore character, and does not start with a digit.
   *
   * @param name a possibly valid path variable name
   * @return a status object with code <code>IStatus.OK</code> if the given name is a valid path
   *     variable name, otherwise a status object indicating what is wrong with the string
   * @see IStatus#OK
   */
  public IStatus validateName(String name);

  /**
   * Validates the given path as the value for a path variable. A path variable value must be a
   * valid path that is absolute.
   *
   * @param path a possibly valid path variable value
   * @return a status object with code <code>IStatus.OK</code> if the given path is a valid path
   *     variable value, otherwise a status object indicating what is wrong with the value
   * @see IPath#isValidPath(String)
   * @see IStatus#OK
   */
  public IStatus validateValue(IPath path);

  /**
   * Validates the given path as the value for a path variable. A path variable value must be a
   * valid path that is absolute.
   *
   * @param path a possibly valid path variable value
   * @return a status object with code {@link IStatus#OK} if the given path is a valid path variable
   *     value, otherwise a status object indicating what is wrong with the value
   * @see IPath#isValidPath(String)
   * @see IStatus#OK
   * @since 3.6
   */
  public IStatus validateValue(URI path);

  /**
   * Returns a variable relative path equivalent to an absolute path for a file or folder in the
   * file system, according to the variables defined in this project PathVariableManager. The file
   * or folder need not to exist.
   *
   * @param location a path in the local file system
   * @return the corresponding variable relative path, or <code>null</code> if no such path is
   *     available
   * @since 3.6
   */
  public URI getVariableRelativePathLocation(URI location);

  /**
   * Converts the internal format of the linked resource location if the PARENT variables is used.
   * For example, if the value is "${PARENT-2-VAR}\foo", the converted result is "${VAR}\..\..\foo".
   *
   * @param value the value encoded using OS string (as returned from Path.toOSString())
   * @param locationFormat indicates whether the value contains a string that is stored in the
   *     linked resource location rather than in the path variable value
   * @return the converted path variable value
   * @since 3.6
   */
  public String convertToUserEditableFormat(String value, boolean locationFormat);

  /**
   * Converts the user editable format to the internal format. For example, if the value is
   * "${VAR}\..\..\foo", the converted result is "${PARENT-2-VAR}\foo". If the string is not
   * directly convertible to a ${PARENT-COUNT-VAR} syntax (for example, the editable string
   * "${FOO}bar\..\..\"), intermediate path variables will be created.
   *
   * @param value the value encoded using OS string (as returned from Path.toOSString())
   * @param locationFormat indicates whether the value contains a string that is stored in the
   *     linked resource location rather than in the path variable value
   * @return the converted path variable value
   * @since 3.6
   */
  public String convertFromUserEditableFormat(String value, boolean locationFormat);
}
