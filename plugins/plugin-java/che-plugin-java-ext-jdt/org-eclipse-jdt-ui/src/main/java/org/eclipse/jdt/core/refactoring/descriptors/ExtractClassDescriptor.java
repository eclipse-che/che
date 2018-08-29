/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.core.refactoring.descriptors;

import java.util.ArrayList;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Refactoring descriptor for the extract class refactoring.
 *
 * <p>An instance of this refactoring descriptor may be obtained by calling {@link
 * RefactoringContribution#createDescriptor()} on a refactoring contribution requested by invoking
 * {@link RefactoringCore#getRefactoringContribution(String)} with the appropriate refactoring id.
 *
 * @since 1.2
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ExtractClassDescriptor extends JavaRefactoringDescriptor {

  private static final String CREATE_GETTER_SETTER = "createGetterSetter"; // $NON-NLS-1$

  private static final String PACKAGE_NAME = "packageName"; // $NON-NLS-1$

  private static final String CLASS_NAME = "className"; // $NON-NLS-1$

  private static final String FIELD_NAME = "fieldName"; // $NON-NLS-1$

  private static final String CREATE_TOP_LEVEL = "createTopLevel"; // $NON-NLS-1$

  private static final String NEW_FIELD_COUNT = "newFieldCount"; // $NON-NLS-1$

  private static final String CREATE_FIELD_COUNT = "createFieldCount"; // $NON-NLS-1$

  private static final String CREATE_FIELD = "createField"; // $NON-NLS-1$

  private static final String NEW_FIELD_NAME = "newFieldName"; // $NON-NLS-1$

  private static final String OLD_FIELD_NAME = "oldFieldName"; // $NON-NLS-1$

  private static final String OLD_FIELD_COUNT = "oldFieldCount"; // $NON-NLS-1$

  /**
   * Instances of {@link ExtractClassDescriptor.Field} describe which fields will be moved to the
   * extracted class and their new name there.
   */
  public static class Field {
    private final String fFieldName;
    private String fNewFieldName;
    private boolean fCreateField = true;

    private Field(String fieldName) {
      super();
      Assert.isNotNull(fieldName);
      this.fFieldName = fieldName;
      this.fNewFieldName = fieldName;
    }

    /**
     * The name of the field in the selected type
     *
     * @return the name of the field in the selected type
     */
    public String getFieldName() {
      return fFieldName;
    }

    /**
     * The name of the field in the extracted class. The default is the same as in the selected type
     *
     * @return the name of the field in the extracted class
     */
    public String getNewFieldName() {
      return fNewFieldName;
    }

    /**
     * Sets the name of the field in the extracted class. The default is the same as in the selected
     * type
     *
     * @param newFieldName the new field name. Must not be <code>null</code>
     */
    public void setNewFieldName(String newFieldName) {
      Assert.isNotNull(newFieldName);
      this.fNewFieldName = newFieldName;
    }

    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fFieldName == null) ? 0 : fFieldName.hashCode());
      return result;
    }

    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Field other = (Field) obj;
      if (fFieldName == null) {
        if (other.fFieldName != null) return false;
      } else if (!fFieldName.equals(other.fFieldName)) return false;
      return true;
    }

    public String toString() {
      return "Field:"
          + fFieldName
          + " new name:"
          + fNewFieldName
          + " create field:"
          + fCreateField; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Returns whether the field will be moved to extracted class. The default is <code>true</code>
     *
     * @return <code>true</code> if the field will be moved
     */
    public boolean isCreateField() {
      return fCreateField;
    }

    /**
     * Sets whether the field will be moved to extracted class. The default is <code>true</code>
     *
     * @param createField if <code>true</code> the field will be moved
     */
    public void setCreateField(boolean createField) {
      fCreateField = createField;
    }
  }

  private Field[] fFields;

  /** Creates a new refactoring descriptor. */
  public ExtractClassDescriptor() {
    super(IJavaRefactorings.EXTRACT_CLASS);
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
  public ExtractClassDescriptor(
      String project, String description, String comment, Map arguments, int flags)
      throws IllegalArgumentException {
    super(IJavaRefactorings.EXTRACT_CLASS, project, description, comment, arguments, flags);
    if (JavaRefactoringDescriptorUtil.getString(arguments, OLD_FIELD_COUNT, true) != null) {
      String[] oldFieldNames =
          JavaRefactoringDescriptorUtil.getStringArray(
              arguments, OLD_FIELD_COUNT, OLD_FIELD_NAME, 0);
      boolean[] createField =
          JavaRefactoringDescriptorUtil.getBooleanArray(
              arguments, CREATE_FIELD_COUNT, CREATE_FIELD, 0);
      fFields = new Field[oldFieldNames.length];
      for (int i = 0; i < oldFieldNames.length; i++) {
        fFields[i] = new Field(oldFieldNames[i]);
        fFields[i].setCreateField(createField[i]);
        if (createField[i])
          fFields[i].setNewFieldName(
              JavaRefactoringDescriptorUtil.getString(
                  arguments, JavaRefactoringDescriptorUtil.getAttributeName(NEW_FIELD_NAME, i)));
      }
    }
  }

  /**
   * Creates {@link Field} objects for all instance fields of the type
   *
   * @param type the type declaring the field that will be moved to the extracted class
   * @return an instance of {@link Field} for every field declared in type that is not static
   * @throws JavaModelException if the type does not exist or if an exception occurs while accessing
   *     its corresponding resource.
   */
  public static Field[] getFields(IType type) throws JavaModelException {
    IField[] fields = type.getFields();
    ArrayList result = new ArrayList();
    for (int i = 0; i < fields.length; i++) {
      IField field = fields[i];
      if (!Flags.isStatic(field.getFlags()) && !field.isEnumConstant())
        result.add(new Field(field.getElementName()));
    }
    return (Field[]) result.toArray(new Field[result.size()]);
  }

  /**
   * Sets the fields. The order is important and should be the same as the order returned from
   * {@link #getFields(IType)}. Changing the order can have side effects because of different
   * initialization order. Only fields which return <code>true</code> for {@link
   * Field#isCreateField()} are created in the extracted class. Can be <code>null</code> to indicate
   * that all instance fields should be moved
   *
   * @param fields the fields to move to the extracted class. Can be <code>null</code> to indicate
   *     that all instance fields should be moved
   * @throws IllegalArgumentException if one of the fields is <code>null</code>
   */
  public void setFields(Field[] fields) throws IllegalArgumentException {
    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      if (field == null) throw new IllegalArgumentException("Field can not be null"); // $NON-NLS-1$
    }
    fFields = fields;
  }

  /**
   * Returns the fields. The order of the fields is the same as they will appear in the extracted
   * class if {@link Field#isCreateField()} returns <code>true</code>.
   *
   * @return the fields or <code>null</code>. If <code>null</code> all instance fields from the
   *     selected type will be moved
   */
  public Field[] getFields() {
    return fFields;
  }

  /**
   * Returns the type from which the fields are moved
   *
   * @return the type
   */
  public IType getType() {
    return (IType)
        JavaRefactoringDescriptorUtil.getJavaElement(fArguments, ATTRIBUTE_INPUT, getProject());
  }

  /**
   * Sets the type to extract class from
   *
   * @param type the type to extract class from
   */
  public void setType(IType type) {
    Assert.isNotNull(type);
    String project = type.getJavaProject().getElementName();
    setProject(project);
    JavaRefactoringDescriptorUtil.setJavaElement(fArguments, ATTRIBUTE_INPUT, project, type);
  }

  /**
   * Returns the package where the extracted class will be created in if {{@link
   * #isCreateTopLevel()} returns <code>true</code>. Can return <code>null</code> to indicate that
   * the package will be the same as the type
   *
   * @return the package for the toplevel extracted class or <code>null</code>. If <code>null</code>
   *     the package will be the same as the type
   */
  public String getPackage() {
    return JavaRefactoringDescriptorUtil.getString(fArguments, PACKAGE_NAME, true);
  }

  /**
   * Sets the package in which the top level class will be created. Can be <code>null</code> to
   * indicate that the package will be the same as the type
   *
   * @param packageName the package in which the top level class will be created. Can be <code>null
   *     </code> to indicate that the package will be the same as the type
   */
  public void setPackage(String packageName) {
    JavaRefactoringDescriptorUtil.setString(fArguments, PACKAGE_NAME, packageName);
  }

  /**
   * Returns the class name for the extracted class or <code>null</code> if the refactoring should
   * choose a name
   *
   * @return the class name for the extracted class or <code>null</code> if the refactoring should
   *     choose a name
   */
  public String getClassName() {
    return JavaRefactoringDescriptorUtil.getString(fArguments, CLASS_NAME, true);
  }

  /**
   * Sets the class name for the extracted class or <code>null</code> if the refactoring should
   * choose a name
   *
   * @param className the class name for the extracted class or <code>null</code> if the refactoring
   *     should choose a name
   */
  public void setClassName(String className) {
    JavaRefactoringDescriptorUtil.setString(fArguments, CLASS_NAME, className);
  }

  /**
   * Returns the field name for the generated field or <code>null</code> if the refactoring should
   * choose a name
   *
   * @return the field name for the generated field or <code>null</code> if the refactoring should
   *     choose a name
   */
  public String getFieldName() {
    return JavaRefactoringDescriptorUtil.getString(fArguments, FIELD_NAME, true);
  }

  /**
   * Sets the field name for the generated field or <code>null</code> if the refactoring should
   * choose a name
   *
   * @param fieldName the field name for the generated field or <code>null</code> if the refactoring
   *     should choose a name
   */
  public void setFieldName(String fieldName) {
    JavaRefactoringDescriptorUtil.setString(fArguments, FIELD_NAME, fieldName);
  }

  /**
   * Returns whether the extracted class will be created as top level class or as nested class. If
   * <code>true</code> the extracted class will be generated as top level class. The default is
   * <code>true</code>
   *
   * @return if <code>true</code> the extracted class will be generated as top level class. The
   *     default is <code>true</code>
   */
  public boolean isCreateTopLevel() {
    return JavaRefactoringDescriptorUtil.getBoolean(fArguments, CREATE_TOP_LEVEL, true);
  }

  /**
   * Sets whether the extracted class will be created as top level class or as nested class. If
   * <code>true</code> the extracted class will be generated as top level class. Else the class will
   * be created as nested class in the type. The default is <code>true</code>
   *
   * @param createTopLevel <code>true</code> to generated as top level class. The default is <code>
   *     true</code>
   */
  public void setCreateTopLevel(boolean createTopLevel) {
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, CREATE_TOP_LEVEL, createTopLevel);
  }

  /**
   * Sets whether getters and setters will be created for all fields.
   *
   * @param createGetterSetter <code>true</code> to create getters and setters. Default is <code>
   *     false</code>.
   */
  public void setCreateGetterSetter(boolean createGetterSetter) {
    JavaRefactoringDescriptorUtil.setBoolean(fArguments, CREATE_GETTER_SETTER, createGetterSetter);
  }

  /**
   * Returns <code>true</code> if getters and setters are generated for fields. Default is <code>
   * false</code>.
   *
   * @return <code>true</code> if getters and setters are generated for fields. Default is <code>
   *     false</code>
   */
  public boolean isCreateGetterSetter() {
    return JavaRefactoringDescriptorUtil.getBoolean(fArguments, CREATE_GETTER_SETTER, false);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor#populateArgumentMap()
   */
  protected void populateArgumentMap() {
    super.populateArgumentMap();
    if (fFields != null) {
      String[] oldFieldNames = new String[fFields.length];
      String[] newFieldNames = new String[fFields.length];
      boolean[] createField = new boolean[fFields.length];
      for (int i = 0; i < fFields.length; i++) {
        Field field = fFields[i];
        Assert.isNotNull(field);
        oldFieldNames[i] = field.getFieldName();
        createField[i] = field.isCreateField();
        if (field.isCreateField()) newFieldNames[i] = field.getNewFieldName();
      }
      JavaRefactoringDescriptorUtil.setStringArray(
          fArguments, OLD_FIELD_COUNT, OLD_FIELD_NAME, oldFieldNames, 0);
      JavaRefactoringDescriptorUtil.setStringArray(
          fArguments, NEW_FIELD_COUNT, NEW_FIELD_NAME, newFieldNames, 0);
      JavaRefactoringDescriptorUtil.setBooleanArray(
          fArguments, CREATE_FIELD_COUNT, CREATE_FIELD, createField, 0);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor#validateDescriptor()
   */
  public RefactoringStatus validateDescriptor() {
    RefactoringStatus status = super.validateDescriptor();
    if (getType() == null) status.addFatalError("The type may not be null"); // $NON-NLS-1$
    return status;
  }
}
