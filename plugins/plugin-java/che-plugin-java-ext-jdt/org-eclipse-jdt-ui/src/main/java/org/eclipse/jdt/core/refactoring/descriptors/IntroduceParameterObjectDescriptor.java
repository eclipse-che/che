/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.core.refactoring.descriptors;

import java.util.Map;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Refactoring descriptor for the introduce parameter object refactoring.
 *
 * <p>An instance of this refactoring descriptor may be obtained by calling {@link
 * RefactoringContribution#createDescriptor()} on a refactoring contribution requested by invoking
 * {@link RefactoringCore#getRefactoringContribution(String)} with the appropriate refactoring id.
 *
 * @since 1.2
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class IntroduceParameterObjectDescriptor extends JavaRefactoringDescriptor {

  /** Instances of Parameter are used to describe the position of parameter and fields. */
  public static class Parameter {

    private boolean fCreateField = false;

    private String fFieldName = null;

    private final int fIndex;

    /**
     * Creates a new parameter object. It is not recommended to call this constructor directly. Use
     * {@link IntroduceParameterObjectDescriptor#createParameters(IMethod)} instead.
     *
     * @param index the index of the parameter in the method
     */
    public Parameter(int index) {
      super();
      fIndex = index;
    }

    /**
     * The name of the field that will be created if {@link #isCreateField()} is <code>true</code>
     *
     * @return the field name
     * @see #isCreateField()
     * @see #setFieldName(String)
     */
    public String getFieldName() {
      return fFieldName;
    }

    /**
     * The index of the parameter in the original method signature. The parameter object has the
     * special index {@link IntroduceParameterObjectDescriptor#PARAMETER_OBJECT_IDX}. The position
     * in the new method signature depends on the position in the array passed to {@link
     * IntroduceParameterObjectDescriptor#setParameters(IntroduceParameterObjectDescriptor.Parameter[])}
     *
     * @return returns the index of the parameter in the original method signature or {@link
     *     IntroduceParameterObjectDescriptor#PARAMETER_OBJECT_IDX} for the parameter object
     * @see IntroduceParameterObjectDescriptor#PARAMETER_OBJECT
     * @see IntroduceParameterObjectDescriptor#PARAMETER_OBJECT_IDX
     * @see
     *     IntroduceParameterObjectDescriptor#setParameters(IntroduceParameterObjectDescriptor.Parameter[])
     */
    public int getIndex() {
      return fIndex;
    }

    /**
     * If <code>true</code> the parameter will be removed from the method's signature and will be
     * added to the parameter object. The default is <code>false</code>
     *
     * @return <code>true</code> if the parameter will be created as field, <code>false</code> if it
     *     will remain in the method
     */
    public boolean isCreateField() {
      return fCreateField;
    }

    /**
     * Sets whether the parameter will be removed from the method's signature or will be added to
     * the parameter object. The default is <code>false</code>. Changing the creatField property of
     * the parameter object will throw a {@link IllegalArgumentException}
     *
     * @param createField <code>true</code> if the parameter should be created as field, <code>false
     *     </code> if it will remain in the method
     */
    public void setCreateField(boolean createField) {
      if (fIndex == PARAMETER_OBJECT_IDX)
        throw new IllegalArgumentException(
            "You can not set create field for the parameter object"); // $NON-NLS-1$
      fCreateField = createField;
    }

    /**
     * Sets the name of the field that will be created in the parameter object if {@link
     * #isCreateField()} is <code>true</code>. Changing the fieldName of the parameter object will
     * throw a {@link IllegalArgumentException}
     *
     * @param fieldName the new name of the field. A <code>null</code> indicates that the field name
     *     should be automatically derived
     * @see #isCreateField()
     */
    public void setFieldName(String fieldName) {
      if (fIndex == PARAMETER_OBJECT_IDX)
        throw new IllegalArgumentException(
            "You can not set the field name of the parameter object"); // $NON-NLS-1$
      fFieldName = fieldName;
    }
  }

  private static final String PARAMETER_COUNT = "PARAMETER_COUNT"; // $NON-NLS-1$

  private static final String PARAMETER_IDX = "PARAMETER_IDX"; // $NON-NLS-1$

  private static final String PARAMETER_CREATE_FIELD = "PARAMETER_CREATE_FIELD"; // $NON-NLS-1$

  private static final String PARAMETER_FIELD_NAME = "PARAMETER_FIELD_NAME"; // $NON-NLS-1$

  private static final String CLASS_NAME = "class_name"; // $NON-NLS-1$

  private static final String DELEGATE = "delegate"; // $NON-NLS-1$

  private static final String DEPRECATE_DELEGATE = "deprecate_delegate"; // $NON-NLS-1$

  private static final String GETTERS = "getters"; // $NON-NLS-1$

  private static final String PACKAGE_NAME = "package_name"; // $NON-NLS-1$

  private static final String PARAMETER_NAME = "parameter_name"; // $NON-NLS-1$

  private static final String SETTERS = "setters"; // $NON-NLS-1$

  private static final String TOP_LEVEL = "top_level"; // $NON-NLS-1$

  /** The parameter index of the special parameter object. The value is "-1". */
  public static final int PARAMETER_OBJECT_IDX = -1;

  /** Singleton instance that represents the parameter object */
  public static final Parameter PARAMETER_OBJECT = new Parameter(PARAMETER_OBJECT_IDX);

  /**
   * Creates the parameters for this method. The first object is the parameter object. By default
   * all parameters are marked for field creation
   *
   * @param method derive parameter from this method
   * @return an array of parameter corresponding to the parameter declared in the method. The first
   *     object will be the parameter object. All parameter are marked for field creation
   */
  public static Parameter[] createParameters(IMethod method) {
    int length = method.getNumberOfParameters();
    Parameter[] result = new Parameter[length + 1];
    result[0] = PARAMETER_OBJECT;
    for (int i = 0; i < length; i++) {
      result[i + 1] = new Parameter(i);
      result[i + 1].setCreateField(true);
    }
    return result;
  }

  private String fClassName;

  private boolean fDelegate = false;

  private boolean fDeprecateDelegate = true;

  private boolean fGetters = false;

  private IMethod fMethod;

  private String fPackageName;

  private String fParameterName;

  private Parameter[] fParameters;

  private boolean fSetters = false;

  /** If <code>true</code> the class will be created as top level class. Default true */
  private boolean fTopLevel = true;

  /** Creates a new refactoring descriptor. */
  public IntroduceParameterObjectDescriptor() {
    super(IJavaRefactorings.INTRODUCE_PARAMETER_OBJECT);
  }

  /**
   * Creates a new refactoring descriptor.
   *
   * @param project the non-empty name of the project associated with this refactoring, or <code>
   *     null</code> for a workspace refactoring
   * @param description a non-empty human-readable description of the particular refactoring
   *     instance
   * @param comment the human-readable comment of the particular refactoring instance, or <code>null
   *     </code> for no comment
   * @param arguments a map of arguments that will be persisted and describes all settings for this
   *     refactoring
   * @param flags the flags of the refactoring descriptor
   * @throws IllegalArgumentException if the argument map contains invalid keys/values
   */
  public IntroduceParameterObjectDescriptor(
      final String project,
      final String description,
      final String comment,
      final Map arguments,
      final int flags)
      throws IllegalArgumentException {
    super(
        IJavaRefactorings.INTRODUCE_PARAMETER_OBJECT,
        project,
        description,
        comment,
        arguments,
        flags);
    initializeFromMap(arguments);
  }

  /**
   * The name of the class that will be generated. If <code>null</code> the refactoring will
   * automatically choose a class name.
   *
   * @return the name of the class that will be generated or <code>null</code> if the name will be
   *     automatically chosen
   */
  public String getClassName() {
    return fClassName;
  }

  /**
   * The method the refactoring will operate on. Can be set using {@link #setMethod(IMethod)}.
   *
   * @return the method that the refactoring will operate on.
   */
  public IMethod getMethod() {
    return fMethod;
  }

  /**
   * The parameter object class will be created in this package if the top level is <code>true
   * </code>. Can be set using {@link #setPackageName(String)}. If the package name was <code>null
   * </code> and the method has already been set this method returns the package where the method is
   * declared in.
   *
   * @return the package name that has been set or the package where the method is declared. Can
   *     return <code>null</code> if neither the package nor the method has been set
   */
  public String getPackageName() {
    if (fPackageName == null && fMethod != null) {
      IType type = fMethod.getDeclaringType();
      if (type != null) return type.getPackageFragment().getElementName();
    }
    return fPackageName;
  }

  /**
   * Returns the name of the parameter. Can return <code>null</code> in which case the refactoring
   * chooses a name. Default is <code>null</code>
   *
   * @return the name of the parameter. Can return <code>null</code> in which case the refactoring
   *     chooses a name. Default is <code>null</code>
   */
  public String getParameterName() {
    return fParameterName;
  }

  /**
   * Returns the parameters. Can return <code>null</code> if all parameters should be converted to
   * fields. Default is <code>null</code>.
   *
   * @return the parameters. Can return <code>null</code> if all parameters should be converted to
   *     fields. Default is <code>null</code>
   */
  public Parameter[] getParameters() {
    return fParameters;
  }

  /**
   * Returns <code>true</code> if delegates will be kept. Default is <code>false</code>.
   *
   * @return <code>true</code> if delegates will be kept. Default is <code>false</code>
   */
  public boolean isDelegate() {
    return fDelegate;
  }

  /**
   * Returns <code>true</code> if delegates will be marked as deprecated. Default is <code>false
   * </code>.
   *
   * @return <code>true</code> if delegates will be marked as deprecated. Default is <code>false
   *     </code>
   */
  public boolean isDeprecateDelegate() {
    return fDeprecateDelegate;
  }

  /**
   * Returns <code>true</code> if getters are generated for fields. Default is <code>false</code>.
   *
   * @return <code>true</code> if getters are generated for fields. Default is <code>false</code>
   */
  public boolean isGetters() {
    return fGetters;
  }

  /**
   * Returns <code>true</code> if setters are generated for fields. Default is <code>false</code>.
   *
   * @return <code>true</code> if setters are generated for fields. Default is <code>false</code>
   */
  public boolean isSetters() {
    return fSetters;
  }

  /**
   * Returns <code>true</code> if the new type is created as top level type. <code>false</code> is
   * returned when the type is created as enclosing type of the type declaring the method
   * declaration to be changed. Default is <code>true</code>.
   *
   * @return <code>true</code> if the new type is created as top level type. <code>false</code> is
   *     returned when the type is created as enclosing type of the type declaring the method
   *     declaration to be changed. Default is <code>true</code>
   */
  public boolean isTopLevel() {
    return fTopLevel;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor#populateArgumentMap()
   */
  protected void populateArgumentMap() {
    super.populateArgumentMap();
    JavaRefactoringDescriptorUtil.setJavaElement(
        fArguments, ATTRIBUTE_INPUT, getProject(), fMethod);
    Parameter[] parameters = fParameters;
    if (parameters == null) {
      parameters = createParameters(fMethod);
    }
    JavaRefactoringDescriptorUtil.setInt(fArguments, PARAMETER_COUNT, parameters.length);
    for (int i = 0; i < parameters.length; i++) {
      Parameter param = parameters[i];
      JavaRefactoringDescriptorUtil.setInt(
          fArguments,
          JavaRefactoringDescriptorUtil.getAttributeName(PARAMETER_IDX, i),
          param.getIndex());
      JavaRefactoringDescriptorUtil.setBoolean(
          fArguments,
          JavaRefactoringDescriptorUtil.getAttributeName(PARAMETER_CREATE_FIELD, i),
          param.isCreateField());
      if (param.isCreateField())
        JavaRefactoringDescriptorUtil.setString(
            fArguments,
            JavaRefactoringDescriptorUtil.getAttributeName(PARAMETER_FIELD_NAME, i),
            param.getFieldName());
    }
    JavaRefactoringDescriptorUtil.setString(fArguments, CLASS_NAME, fClassName);
    JavaRefactoringDescriptorUtil.setString(fArguments, PACKAGE_NAME, fPackageName);
    JavaRefactoringDescriptorUtil.setString(fArguments, PARAMETER_NAME, fParameterName);
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, DELEGATE, fDelegate);
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, DEPRECATE_DELEGATE, fDeprecateDelegate);
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, GETTERS, fGetters);
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, SETTERS, fSetters);
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, TOP_LEVEL, fTopLevel);
  }

  private void initializeFromMap(Map map) throws IllegalArgumentException {
    IMethod method =
        (IMethod) JavaRefactoringDescriptorUtil.getJavaElement(map, ATTRIBUTE_INPUT, getProject());
    setMethod(method);
    initializeParameter(map);
    setClassName(JavaRefactoringDescriptorUtil.getString(map, CLASS_NAME, true));
    setPackageName(JavaRefactoringDescriptorUtil.getString(map, PACKAGE_NAME, true));
    setParameterName(JavaRefactoringDescriptorUtil.getString(map, PARAMETER_NAME, true));
    setDelegate(JavaRefactoringDescriptorUtil.getBoolean(map, DELEGATE, fDelegate));
    setDeprecateDelegate(
        JavaRefactoringDescriptorUtil.getBoolean(map, DEPRECATE_DELEGATE, fDeprecateDelegate));
    setGetters(JavaRefactoringDescriptorUtil.getBoolean(map, GETTERS, fGetters));
    setSetters(JavaRefactoringDescriptorUtil.getBoolean(map, SETTERS, fSetters));
    setTopLevel(JavaRefactoringDescriptorUtil.getBoolean(map, TOP_LEVEL, fTopLevel));
  }

  private void initializeParameter(Map map) throws IllegalArgumentException {
    int[] idx = JavaRefactoringDescriptorUtil.getIntArray(map, PARAMETER_COUNT, PARAMETER_IDX);
    boolean[] createField =
        JavaRefactoringDescriptorUtil.getBooleanArray(
            map, PARAMETER_COUNT, PARAMETER_CREATE_FIELD, 0);
    Parameter[] result = new Parameter[idx.length];
    for (int i = 0; i < idx.length; i++) {
      int index = idx[i];
      if (index == PARAMETER_OBJECT_IDX) {
        result[i] = new Parameter(PARAMETER_OBJECT_IDX);
      } else {
        Parameter parameter = new Parameter(index);
        result[i] = parameter;
        parameter.setCreateField(createField[i]);
        if (createField[i])
          parameter.setFieldName(
              JavaRefactoringDescriptorUtil.getString(
                  map, JavaRefactoringDescriptorUtil.getAttributeName(PARAMETER_FIELD_NAME, i)));
      }
    }
    setParameters(result);
  }

  /**
   * Sets the name of the class for the generated parameter object. The name can be <code>null
   * </code> to indicate that the refactoring should chose one.
   *
   * @param className the name of the generated class or <code>null</code>. Default is <code>null
   *     </code>
   */
  public void setClassName(String className) {
    fClassName = className;
  }

  /**
   * Sets delegate keeping. If <code>true</code> delegates will be kept.
   *
   * @param delegate <code>true</code> to keep delegates. Default is <code>false</code>
   */
  public void setDelegate(boolean delegate) {
    fDelegate = delegate;
  }

  /**
   * Sets deprecate delegate. If <code>true</code> generated delegates will be marked as deprecated.
   *
   * @param deprecateDelegate <code>true</code> to deprecate kept delegates. Default is <code>false
   *     </code>
   */
  public void setDeprecateDelegate(boolean deprecateDelegate) {
    fDeprecateDelegate = deprecateDelegate;
  }

  /**
   * Sets whether getters will be created for all fields.
   *
   * @param getters <code>true</code> to create getters. Default is <code>false</code>.
   */
  public void setGetters(boolean getters) {
    fGetters = getters;
  }

  /**
   * Sets the method. The method may not be <code>null</code>, has to exist, and has to be in a Java
   * project.
   *
   * @param method the method. May not be <code>null</code>
   */
  public void setMethod(IMethod method) {
    if (method == null)
      throw new IllegalArgumentException("The method must not be null"); // $NON-NLS-1$
    if (!method.exists())
      throw new IllegalArgumentException("The method must exist"); // $NON-NLS-1$
    if (method.getJavaProject() == null)
      throw new IllegalArgumentException("The method has to be in a Java project"); // $NON-NLS-1$
    fMethod = method;
  }

  /**
   * Sets the package where the parameter object will be created in if it is created as top level
   * class. The package can be <code>null</code> to indicate that the package of the method should
   * be used.
   *
   * @param packageName the package for the top level class or <code>null</code>. Default is <code>
   *     null</code>.
   */
  public void setPackageName(String packageName) {
    fPackageName = packageName;
  }

  /**
   * Sets the name of the parameter object as it will appear in the method signature. The name can
   * be <code>null</code> to indicate that the refactoring will choose a name.
   *
   * @param parameterName the name of the parameter or <code>null</code>. Default is <code>null
   *     </code>.
   */
  public void setParameterName(String parameterName) {
    fParameterName = parameterName;
  }

  /**
   * Sets the parameters. The parameters can be <code>null</code> to indicate that all parameter
   * should be used as fields. If not <code>null</code>, the number of parameters passed has to be
   * the number of parameter of the method + 1. One element has to be the {@link #PARAMETER_OBJECT}.
   * Each parameter may only appear once.
   *
   * @param parameters the parameters or <code>null</code>. Default is <code>null</code>
   */
  public void setParameters(Parameter[] parameters) {
    fParameters = parameters;
  }

  /**
   * Sets whether setters will be created for all fields.
   *
   * @param setters <code>true</code> to create setters. Default is <code>false</code>
   */
  public void setSetters(boolean setters) {
    fSetters = setters;
  }

  /**
   * Sets whether the parameter object class will be created as top level class. if <code>true
   * </code> the class will be created as top level class in the package returned by {@link
   * #getPackageName()}. If <code>false</code> the class will be created as as nested class in the
   * class containing the method
   *
   * @param topLevel <code>true</code> to create the parameter object as top level. Default is
   *     <code>true</code>
   */
  public void setTopLevel(boolean topLevel) {
    fTopLevel = topLevel;
  }

  /** {@inheritDoc} */
  public RefactoringStatus validateDescriptor() {
    RefactoringStatus result = super.validateDescriptor();
    if (!result.isOK()) return result;
    if (fMethod == null) {
      result.addFatalError("The method must not be null"); // $NON-NLS-1$
      return result;
    }
    IJavaProject javaProject = fMethod.getJavaProject();
    if (javaProject == null) {
      result.addFatalError("Can not derive Java project from method"); // $NON-NLS-1$
      return result;
    }
    String sourceLevel = javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
    String complianceLevel = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
    if (fParameters != null) {
      if (fParameters.length - 1 != fMethod.getNumberOfParameters()) {
        result.addFatalError(
            "The number of parameters does not match the number of parameters of the method"); // $NON-NLS-1$
      }
      boolean hasParameterObject = false;
      for (int i = 0; i < fParameters.length; i++) {
        Parameter parameter = fParameters[i];
        if (parameter.isCreateField()) {
          String fieldName = parameter.getFieldName();
          if (fieldName == null)
            result.addError(
                "The parameter "
                    + parameter.getIndex()
                    + " is marked for field creation but does not have a field"
                    + " name"); // $NON-NLS-1$ //$NON-NLS-2$
          else {
            result.merge(
                RefactoringStatus.create(
                    JavaConventions.validateFieldName(fieldName, sourceLevel, complianceLevel)));
          }
        }
        if (parameter == PARAMETER_OBJECT) {
          if (hasParameterObject)
            result.addError("Can not have more than one parameter object"); // $NON-NLS-1$
          else hasParameterObject = true;
        }
      }
    }
    if (fClassName != null) {
      result.merge(
          RefactoringStatus.create(
              JavaConventions.validateIdentifier(fClassName, sourceLevel, complianceLevel)));
    }
    if (fParameterName != null) {
      result.merge(
          RefactoringStatus.create(
              JavaConventions.validateIdentifier(fParameterName, sourceLevel, complianceLevel)));
    }
    if (fPackageName != null && !"".equals(fPackageName)) { // $NON-NLS-1$
      result.merge(
          RefactoringStatus.create(
              JavaConventions.validatePackageName(fPackageName, sourceLevel, complianceLevel)));
    }
    return result;
  }
}
