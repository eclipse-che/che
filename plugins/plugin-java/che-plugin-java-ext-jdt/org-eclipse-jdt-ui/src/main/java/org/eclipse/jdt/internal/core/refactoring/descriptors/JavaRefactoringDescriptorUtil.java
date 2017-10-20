/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.core.refactoring.descriptors;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;

public class JavaRefactoringDescriptorUtil {

  private static final String LOWER_CASE_FALSE = Boolean.FALSE.toString().toLowerCase();
  private static final String LOWER_CASE_TRUE = Boolean.TRUE.toString().toLowerCase();

  /**
   * Converts the specified element to an input handle.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param element the element
   * @return a corresponding input handle. Note: if the given project is not the JavaElement's
   *     project, then the full handle is returned
   */
  public static String elementToHandle(final String project, final IJavaElement element) {
    final String handle = element.getHandleIdentifier();
    if (project != null && !(element instanceof IJavaProject)) {
      IJavaProject javaProject = element.getJavaProject();
      if (project.equals(javaProject.getElementName())) {
        final String id = javaProject.getHandleIdentifier();
        return handle.substring(id.length());
      }
    }
    return handle;
  }

  /**
   * Converts the specified resource to an input handle.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param resourcePath the resource
   * @return the input handle Note: if the given project is not the resource's project, then the
   *     full handle is returned
   */
  public static String resourcePathToHandle(final String project, final IPath resourcePath) {
    if (project != null && !"".equals(project) && resourcePath.segmentCount() != 1) // $NON-NLS-1$
    if (resourcePath.segment(0).equals(project)) {
        return resourcePath.removeFirstSegments(1).toPortableString();
      }
    return resourcePath.toPortableString();
  }

  /**
   * Converts an input handle back to the corresponding java element.
   *
   * @param owner the working copy owner
   * @param project the project, or <code>null</code> for the workspace
   * @param handle the input handle
   * @param check <code>true</code> to check for existence of the element, <code>false</code>
   *     otherwise
   * @return the corresponding java element, or <code>null</code> if no such element exists
   */
  public static IJavaElement handleToElement(
      final WorkingCopyOwner owner,
      final String project,
      final String handle,
      final boolean check) {
    IJavaElement element = null;
    if (owner != null) element = JavaCore.create(handle, owner);
    else element = JavaCore.create(handle);
    if (element == null && project != null) {
      final IJavaProject javaProject =
          JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProject(project);
      final String identifier = javaProject.getHandleIdentifier();
      if (owner != null) element = JavaCore.create(identifier + handle, owner);
      else element = JavaCore.create(identifier + handle);
    }
    if (check && element instanceof IMethod) {
      /*
       * Resolve the method based on simple names of parameter types
       * (to accommodate for different qualifications when refactoring is e.g.
       * recorded in source but applied on binary method):
       */
      final IMethod method = (IMethod) element;
      final IMethod[] methods = method.getDeclaringType().findMethods(method);
      if (methods != null && methods.length > 0) element = methods[0];
    }
    if (element != null && (!check || element.exists())) return element;
    return null;
  }

  /**
   * Converts an input handle with the given prefix back to the corresponding resource. WARNING:
   * this method resolves the handle in the current workspace, since the type of the resource
   * (file/folder) cannot be determined from the handle alone (path never has a trailing separator).
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param handle the input handle
   * @return the corresponding resource, or <code>null</code> if no such resource exists
   */
  public static IResource handleToResource(final String project, final String handle) {
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    if ("".equals(handle)) // $NON-NLS-1$
    return null;
    final IPath path = Path.fromPortableString(handle);
    if (path == null) return null;
    if (project != null && !"".equals(project)) // $NON-NLS-1$
    return root.getProject(project).findMember(path);
    return root.findMember(path);
  }

  /**
   * Converts an input handle with the given prefix back to the corresponding resource path.
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param handle the input handle
   * @return the corresponding resource path. Note: if the given handle is absolute, the project is
   *     not used to resolve.
   */
  public static IPath handleToResourcePath(final String project, final String handle) {
    final IPath path = Path.fromPortableString(handle);
    if (project != null && !"".equals(project) && !path.isAbsolute()) // $NON-NLS-1$
    return new Path(project).append(path).makeAbsolute();
    return path;
  }

  /**
   * Retrieves a {@link String} attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @param allowNull if <code>true</code> a <code>null</code> will be returned if the attribute
   *     does not exist
   * @return the value of the attribute
   * @throws IllegalArgumentException if the value of the attribute is not a {@link String} or
   *     allowNull is <code>false</code> and the attribute does not exist
   */
  public static String getString(Map map, String attribute, boolean allowNull)
      throws IllegalArgumentException {
    Object object = map.get(attribute);
    if (object == null) {
      if (allowNull) return null;
      throw new IllegalArgumentException(
          "The map does not contain the attribute '" + attribute + "'"); // $NON-NLS-1$//$NON-NLS-2$
    }
    if (object instanceof String) {
      String value = (String) object;
      return value;
    }
    throw new IllegalArgumentException(
        "The provided map does not contain a string for attribute '"
            + attribute
            + "'"); // $NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Retrieves a {@link String} attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @return the value of the attribute
   * @throws IllegalArgumentException if the value of the attribute is not a {@link String} or the
   *     attribute does not exist
   */
  public static String getString(Map map, String attribute) throws IllegalArgumentException {
    return getString(map, attribute, false);
  }

  /**
   * Retrieves an <code>String[]</code> attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param countAttribute the attribute that contains the number of elements
   * @param arrayAttribute the attribute name where the values are stored. The index starting from
   *     offset is appended to this
   * @param offset the starting index for arrayAttribute
   * @return the <code>String[]</code>
   * @throws IllegalArgumentException if any of the attribute does not exist or is not a number
   */
  public static String[] getStringArray(
      Map map, String countAttribute, String arrayAttribute, int offset)
      throws IllegalArgumentException {
    int count = getInt(map, countAttribute);
    String[] result = new String[count];
    for (int i = 0; i < count; i++) {
      result[i] = getString(map, getAttributeName(arrayAttribute, i + offset));
    }
    return result;
  }

  /**
   * Retrieves an <code>int</code> attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @return the value of the attribute
   * @throws IllegalArgumentException if the attribute does not exist or is not a number
   */
  public static int getInt(Map map, String attribute) throws IllegalArgumentException {
    String value = getString(map, attribute);
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "The attribute '"
              + attribute
              + "' does not contain a valid int '"
              + value
              + "'"); // $NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }
  }

  /**
   * Retrieves an <code>int</code> attribute from map. If the attribute does not exist it returns
   * the default value.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @param defaultValue the default value to use if the attribute does not exist
   * @return the <code>int</code> value of the attribute or the specified default value if the
   *     attribute does not exist
   * @throws IllegalArgumentException if the attribute exists but is not a number
   */
  public static int getInt(Map map, String attribute, int defaultValue)
      throws IllegalArgumentException {
    String value = getString(map, attribute, true);
    if (value == null) return defaultValue;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "The attribute '"
              + attribute
              + "' does not contain a valid int '"
              + value
              + "'"); // $NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }
  }

  /**
   * Retrieves an <code>int[]</code> attribute from map.
   *
   * @param countAttribute the attribute that contains the number of elements
   * @param arrayAttribute the attribute name where the values are stored. The index starting from
   *     '0' is appended to this
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @return the <code>int[]</code>
   * @throws IllegalArgumentException if any of the attribute does not exist or is not a number
   */
  public static int[] getIntArray(Map map, String countAttribute, String arrayAttribute)
      throws IllegalArgumentException {
    int count = getInt(map, countAttribute);
    int[] result = new int[count];
    for (int i = 0; i < count; i++) {
      result[i] = getInt(map, getAttributeName(arrayAttribute, i));
    }
    return result;
  }

  /**
   * Create the name for accessing the ith element of an attribute.
   *
   * @param attribute the base attribute
   * @param index the index that should be accessed
   * @return the attribute name for the ith element of an attribute
   */
  public static String getAttributeName(String attribute, int index) {
    return attribute + index;
  }

  /**
   * Retrieves an <code>{@link IJavaElement}</code> attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @param project the project for resolving the java element. Can be <code>null</code> for
   *     workspace
   * @return a {@link IJavaElement} or <code>null</code>
   * @throws IllegalArgumentException if the attribute does not exist or is not a java element
   * @see #handleToElement(WorkingCopyOwner, String, String, boolean)
   */
  public static IJavaElement getJavaElement(Map map, String attribute, String project)
      throws IllegalArgumentException {
    return getJavaElement(map, attribute, project, false);
  }

  /**
   * Retrieves an <code>{@link IJavaElement}</code> attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @param project the project for resolving the java element. Can be <code>null</code> for
   *     workspace
   * @param allowNull if <code>true</code> a <code>null</code> will be returned if the attribute
   *     does not exist
   * @return a {@link IJavaElement} or <code>null</code>
   * @throws IllegalArgumentException if the attribute does not existt
   * @see #handleToElement(WorkingCopyOwner, String, String, boolean)
   */
  public static IJavaElement getJavaElement(
      Map map, String attribute, String project, boolean allowNull)
      throws IllegalArgumentException {
    String handle = getString(map, attribute, allowNull);
    if (handle != null)
      return handleToElement(null, project, handle, false); // TODO: update Javadoc
    return null;
  }

  /**
   * Retrieves an <code>IJavaElement[]</code> attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param countAttribute the attribute that contains the number of elements. Can be <code>null
   *     </code> to indicate that no count attribute exists
   * @param arrayAttribute the attribute name where the values are stored. The index starting from
   *     offset is appended to this
   * @param offset the starting index for arrayAttribute
   * @param project the project for resolving the java element. Can be <code>null</code> for
   *     workspace
   * @param arrayClass the component type for the resulting array. The resulting array can then be
   *     safely casted to arrayClass[]
   * @return the <code>IJavaElement[]</code>
   * @throws IllegalArgumentException if any of the attribute does not exist or is not a number
   */
  public static IJavaElement[] getJavaElementArray(
      Map map,
      String countAttribute,
      String arrayAttribute,
      int offset,
      String project,
      Class arrayClass)
      throws IllegalArgumentException {
    if (countAttribute != null) {
      int count = getInt(map, countAttribute);
      IJavaElement[] result = (IJavaElement[]) Array.newInstance(arrayClass, count);
      for (int i = 0; i < count; i++) {
        result[i] = getJavaElement(map, getAttributeName(arrayAttribute, i + offset), project);
      }
      return result;
    } else {
      ArrayList result = new ArrayList();
      IJavaElement element = null;
      while ((element = getJavaElement(map, arrayAttribute, project, true)) != null) {
        result.add(element);
      }
      return (IJavaElement[])
          result.toArray((Object[]) Array.newInstance(arrayClass, result.size()));
    }
  }

  /**
   * Retrieves and resolves an <code>{@link IResource}</code> attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @param project the project for resolving the resource. Can be <code>null</code> for workspace
   * @return the <code>{@link IResource}</code>, or <code>null</code> if the resource does not exist
   * @throws IllegalArgumentException if the attribute does not exist
   * @see #handleToResource(String, String)
   */
  public static IPath getResourcePath(Map map, String attribute, String project)
      throws IllegalArgumentException {
    String handle = getString(map, attribute);
    return handleToResourcePath(project, handle);
  }

  /**
   * Retrieves an <code>IResource[]</code> attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param countAttribute the attribute that contains the number of elements
   * @param arrayAttribute the attribute name where the values are stored. The index starting from
   *     offset is appended to this
   * @param offset the starting index for arrayAttribute
   * @param project the project for resolving the java element. Can be <code>null</code> for
   *     workspace
   * @return the <code>IResource[]</code>
   * @throws IllegalArgumentException if any of the attribute does not exist or is not a number
   */
  public static IPath[] getResourcePathArray(
      Map map, String countAttribute, String arrayAttribute, int offset, String project)
      throws IllegalArgumentException {
    int count = getInt(map, countAttribute);
    IPath[] result = new IPath[count];
    for (int i = 0; i < count; i++) {
      result[i] = getResourcePath(map, getAttributeName(arrayAttribute, i + offset), project);
    }
    return result;
  }

  /**
   * Retrieves a <code>boolean[]</code> attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param countAttribute the attribute that contains the number of elements
   * @param arrayAttribute the attribute name where the values are stored. The index starting from
   *     '0' is appended to this
   * @param offset the starting index for arrayAttribute
   * @return the <code>boolean[]</code>
   * @throws IllegalArgumentException if any of the attribute does not exist or is not a boolean
   */
  public static boolean[] getBooleanArray(
      Map map, String countAttribute, String arrayAttribute, int offset)
      throws IllegalArgumentException {
    int count = getInt(map, countAttribute);
    boolean[] result = new boolean[count];
    for (int i = 0; i < count; i++) {
      result[i] = getBoolean(map, getAttributeName(arrayAttribute, i + offset));
    }
    return result;
  }

  /**
   * Retrieves a <code>boolean</code> attribute from map.
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @return the <code>boolean</code> value of the attribute
   * @throws IllegalArgumentException if the attribute does not exist or is not a boolean
   */
  public static boolean getBoolean(Map map, String attribute) throws IllegalArgumentException {
    String value = getString(map, attribute).toLowerCase();
    // Boolean.valueOf(value) does not complain about wrong values
    if (LOWER_CASE_TRUE.equals(value)) return true;
    if (LOWER_CASE_FALSE.equals(value)) return false;
    throw new IllegalArgumentException(
        "The attribute '"
            + attribute
            + "' does not contain a valid boolean: '"
            + value
            + "'"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @return <code>true</code> iff the map contains a boolean attribute for the key
   * @throws IllegalArgumentException if the attribute exists but is not a boolean
   */
  public static boolean hasBoolean(Map map, String attribute) throws IllegalArgumentException {
    String string = getString(map, attribute, true);
    if (string == null) return false;

    // Boolean.valueOf(value) does not complain about wrong values
    String value = string.toLowerCase();
    if (LOWER_CASE_TRUE.equals(value)) return true;

    if (LOWER_CASE_FALSE.equals(value)) return true;
    throw new IllegalArgumentException(
        "The attribute '"
            + attribute
            + "' does not contain a valid boolean: '"
            + value
            + "'"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * Retrieves a <code>boolean</code> attribute from map. If the attribute does not exist it returns
   * the default value
   *
   * @param map the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key in the map
   * @param defaultValue the default value to use if the attribute does not exist
   * @return the <code>boolean</code> value of the attribute or the specified default value if the
   *     attribute does not exist
   * @throws IllegalArgumentException if the attribute does not contain a valid value
   */
  public static boolean getBoolean(Map map, String attribute, boolean defaultValue)
      throws IllegalArgumentException {
    String value = getString(map, attribute, true);
    if (value == null) return defaultValue;
    value = value.toLowerCase();
    // Boolean.valueOf(value) does not complain about wrong values
    if (LOWER_CASE_TRUE.equals(value)) return true;
    if (LOWER_CASE_FALSE.equals(value)) return false;
    throw new IllegalArgumentException(
        "The attribute '"
            + attribute
            + "' does not contain a valid boolean: '"
            + value
            + "'"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * Inserts the <code>{@link IJavaElement}</code> into the map.
   *
   * @param arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key's name for the map
   * @param project the project of the element or <code>null</code>. Note: if the given project is
   *     not the JavaElement's project, then the full handle is stored
   * @param element the element to store
   * @throws IllegalArgumentException if the attribute name is invalid, or the element is <code>null
   *     </code>
   */
  public static void setJavaElement(
      Map arguments, String attribute, String project, IJavaElement element)
      throws IllegalArgumentException {
    if (element == null)
      throw new IllegalArgumentException(
          "The element for attribute '"
              + attribute
              + "' may not be null"); // $NON-NLS-1$ //$NON-NLS-2$
    setString(arguments, attribute, elementToHandle(project, element));
  }

  /**
   * Inserts the <code>{@link IPath}</code> into the map.
   *
   * @param arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key's name for the map
   * @param project the project of the element or <code>null</code>. Note: if the given project is
   *     not the resource's project, then the full handle is stored
   * @param resourcePath the resource to store
   * @throws IllegalArgumentException if the attribute name is invalid, or the resource is <code>
   *     null</code>
   */
  public static void setResourcePath(
      Map arguments, String attribute, String project, IPath resourcePath)
      throws IllegalArgumentException {
    if (resourcePath == null)
      throw new IllegalArgumentException(
          "The resource for attribute '"
              + attribute
              + "' may not be null"); // $NON-NLS-1$//$NON-NLS-2$
    setString(arguments, attribute, resourcePathToHandle(project, resourcePath));
  }

  /**
   * Inserts the <code>int</code> into the map.
   *
   * @param arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key's name for the map
   * @param value the <code>int</code> to store
   * @throws IllegalArgumentException if the attribute name is invalid
   */
  public static void setInt(Map arguments, String attribute, int value)
      throws IllegalArgumentException {
    setString(arguments, attribute, Integer.toString(value));
  }

  /**
   * Inserts the <code>boolean</code> into the map.
   *
   * @param arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key's name for the map
   * @param value the <code>boolean</code> to store
   * @throws IllegalArgumentException if the attribute name is invalid
   */
  public static void setBoolean(Map arguments, String attribute, boolean value)
      throws IllegalArgumentException {
    setString(arguments, attribute, value ? LOWER_CASE_TRUE : LOWER_CASE_FALSE);
  }

  /**
   * Inserts the booleans into the map.
   *
   * @param arguments arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param countAttribute the attribute where the number of resources will be stored. Can be <code>
   *     null</code> if no count attribute should be created
   * @param arrayAttribute the attribute where the resources will be stored
   * @param value the booleans to store
   * @param offset the offset to start at
   * @throws IllegalArgumentException if the attribute name is invalid or any of the booleans are
   *     null
   */
  public static void setBooleanArray(
      Map arguments, String countAttribute, String arrayAttribute, boolean[] value, int offset) {
    if (value == null)
      throw new IllegalArgumentException(
          "The values for arrayAttribute '"
              + arrayAttribute
              + "' may not be null"); // $NON-NLS-1$ //$NON-NLS-2$
    if (countAttribute != null) setInt(arguments, countAttribute, value.length);
    for (int i = 0; i < value.length; i++) {
      setBoolean(arguments, getAttributeName(arrayAttribute, i + offset), value[i]);
    }
  }

  /**
   * Inserts the <code>{@link String}</code> into the map.
   *
   * @param arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key's name for the map
   * @param value the <code>{@link String}</code> to store. If <code>null</code> no insertion is
   *     performed
   * @throws IllegalArgumentException if the attribute name is invalid
   */
  public static void setString(Map arguments, String attribute, String value)
      throws IllegalArgumentException {
    if (attribute == null || "".equals(attribute) || attribute.indexOf(' ') != -1) // $NON-NLS-1$
    throw new IllegalArgumentException(
          "Attribute '"
              + attribute
              + "' is not valid: '"
              + value
              + "'"); // $NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    if (value != null) arguments.put(attribute, value);
    else arguments.remove(attribute);
  }

  /**
   * Inserts the Strings into the map.
   *
   * @param arguments arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param countAttribute the attribute where the number of resources will be stored. Can be <code>
   *     null</code> if no count attribute should be created
   * @param arrayAttribute the attribute where the resources will be stored
   * @param value the strings to store
   * @param offset the offset to start at
   * @throws IllegalArgumentException if the attribute name is invalid or any of the strings are
   *     null
   */
  public static void setStringArray(
      Map arguments, String countAttribute, String arrayAttribute, String[] value, int offset) {
    if (value == null)
      throw new IllegalArgumentException(
          "The values for arrayAttribute '"
              + arrayAttribute
              + "' may not be null"); // $NON-NLS-1$ //$NON-NLS-2$
    if (countAttribute != null) setInt(arguments, countAttribute, value.length);
    for (int i = 0; i < value.length; i++) {
      String string = value[i];
      setString(arguments, getAttributeName(arrayAttribute, i + offset), string);
    }
  }

  /**
   * Inserts the selection into the map.
   *
   * @param arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param attribute the key's name for the map
   * @param offset the offset of the selection
   * @param length the length of the selection
   * @throws IllegalArgumentException if the attribute name is invalid
   */
  public static void setSelection(Map arguments, String attribute, int offset, int length)
      throws IllegalArgumentException {
    String value = Integer.toString(offset) + " " + Integer.toString(length); // $NON-NLS-1$
    setString(arguments, attribute, value);
  }

  /**
   * Inserts the resources into the map.
   *
   * @param arguments arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param countAttribute the attribute where the number of resources will be stored. Can be <code>
   *     null</code> if no count attribute should be created
   * @param arrayAttribute the attribute where the resources will be stored
   * @param project the project of the resources or <code>null</code>
   * @param resourcePaths the resource paths to store
   * @param offset the offset to start at
   * @throws IllegalArgumentException if the attribute name is invalid or any of the resources are
   *     null
   */
  public static void setResourcePathArray(
      Map arguments,
      String countAttribute,
      String arrayAttribute,
      String project,
      IPath[] resourcePaths,
      int offset)
      throws IllegalArgumentException {
    if (resourcePaths == null)
      throw new IllegalArgumentException(
          "The resources for arrayAttribute '"
              + arrayAttribute
              + "' may not be null"); // $NON-NLS-1$ //$NON-NLS-2$
    if (countAttribute != null) setInt(arguments, countAttribute, resourcePaths.length);
    for (int i = 0; i < resourcePaths.length; i++) {
      IPath resourcePath = resourcePaths[i];
      setResourcePath(
          arguments, getAttributeName(arrayAttribute, offset + i), project, resourcePath);
    }
  }

  /**
   * Inserts the resources into the map.
   *
   * @param arguments arguments the map with <code>&lt;String, String&gt;</code> mapping
   * @param countAttribute the attribute where the number of elements will be stored. Can be <code>
   *     null</code> if no count attribute should be created
   * @param arrayAttributePrefix the name prefix of the attributes where the elements will be stored
   * @param project the project of the elements or <code>null</code>
   * @param elements the elements to store
   * @param offset the offset to start at (usually 1)
   * @throws IllegalArgumentException if the attribute name is invalid or any of the elements are
   *     null
   */
  public static void setJavaElementArray(
      Map arguments,
      String countAttribute,
      String arrayAttributePrefix,
      String project,
      IJavaElement[] elements,
      int offset)
      throws IllegalArgumentException {
    if (elements == null)
      throw new IllegalArgumentException(
          "The elements for arrayAttribute '"
              + arrayAttributePrefix
              + "' may not be null"); // $NON-NLS-1$ //$NON-NLS-2$
    if (countAttribute != null) setInt(arguments, countAttribute, elements.length);
    for (int i = 0; i < elements.length; i++) {
      IJavaElement element = elements[i];
      setJavaElement(
          arguments, getAttributeName(arrayAttributePrefix, offset + i), project, element);
    }
  }
}
