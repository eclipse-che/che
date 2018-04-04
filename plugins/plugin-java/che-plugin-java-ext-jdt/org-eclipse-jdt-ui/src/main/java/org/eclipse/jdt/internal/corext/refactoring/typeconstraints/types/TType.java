/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * TTypes are lightweight fully-resolved type objects that stand for {@link ITypeBinding}s. TTypes
 * can answer basic questions about the relationship between types.
 *
 * <p>They do not hold references to their corresponding {@link ITypeBinding}s, and they don't carry
 * any information about members of a type.
 *
 * @see TypeEnvironment
 * @see TType#canAssignTo(TType)
 * @see HierarchyType#isSubType(HierarchyType)
 * @see TType#getSubTypes()
 */
public abstract class TType {

  public static final int NULL_TYPE = 1;
  public static final int VOID_TYPE = 2;
  public static final int PRIMITIVE_TYPE = 3;

  public static final int ARRAY_TYPE = 4;

  public static final int STANDARD_TYPE = 5;
  public static final int GENERIC_TYPE = 6;
  public static final int PARAMETERIZED_TYPE = 7;
  public static final int RAW_TYPE = 8;

  public static final int UNBOUND_WILDCARD_TYPE = 9;
  public static final int SUPER_WILDCARD_TYPE = 10;
  public static final int EXTENDS_WILDCARD_TYPE = 11;

  public static final int TYPE_VARIABLE = 12;
  public static final int CAPTURE_TYPE = 13;

  protected static final int WILDCARD_TYPE_SHIFT = 3;
  protected static final int ARRAY_TYPE_SHIFT = 5;

  private static final int F_IS_CLASS = 1 << 0;
  private static final int F_IS_INTERFACE = 1 << 1;
  private static final int F_IS_ENUM = 1 << 2;
  private static final int F_IS_ANNOTATION = 1 << 3;

  private static final int F_IS_TOP_LEVEL = 1 << 4;
  private static final int F_IS_NESTED = 1 << 5;
  private static final int F_IS_MEMBER = 1 << 6;
  private static final int F_IS_LOCAL = 1 << 7;
  private static final int F_IS_ANONYMOUS = 1 << 8;

  protected static final TType[] EMPTY_TYPE_ARRAY = new TType[0];

  private TypeEnvironment fEnvironment;
  private String fBindingKey;
  private int fModifiers;
  private int fFlags;

  /**
   * Creates a new type with the given environment as an owner. The type environment <em>must</em>
   * call {@link #initialize(ITypeBinding)} after using this constructor.
   *
   * @param environment owner
   */
  protected TType(TypeEnvironment environment) {
    fEnvironment = environment;
  }

  /**
   * Creates a new type with the given environment as an owner. The type environment must
   * <em>not</em> call {@link #initialize(ITypeBinding)} after using this constructor.
   *
   * @param environment owner
   * @param key this type's binding key
   */
  protected TType(TypeEnvironment environment, String key) {
    this(environment);
    Assert.isNotNull(key);
    fBindingKey = key;
  }

  /**
   * Initialized the type from the given binding
   *
   * @param binding the binding to initialize from
   */
  protected void initialize(ITypeBinding binding) {
    fBindingKey = binding.getKey();
    Assert.isNotNull(fBindingKey);
    fModifiers = binding.getModifiers();
    if (binding.isClass()) {
      fFlags = F_IS_CLASS;
      // the annotation test has to be done before test for interface
      // since annotations are interfaces as well.
    } else if (binding.isAnnotation()) {
      fFlags = F_IS_ANNOTATION | F_IS_INTERFACE;
    } else if (binding.isInterface()) {
      fFlags = F_IS_INTERFACE;
    } else if (binding.isEnum()) {
      fFlags = F_IS_ENUM;
    }

    if (binding.isTopLevel()) {
      fFlags |= F_IS_TOP_LEVEL;
    } else if (binding.isNested()) {
      fFlags |= F_IS_NESTED;
      if (binding.isMember()) {
        fFlags |= F_IS_MEMBER;
      } else if (binding.isLocal()) {
        fFlags |= F_IS_LOCAL;
      } else if (binding.isAnonymous()) {
        fFlags |= F_IS_ANONYMOUS;
      }
    }
  }

  /**
   * Returns the type's environment
   *
   * @return the types's environment
   */
  public TypeEnvironment getEnvironment() {
    return fEnvironment;
  }

  /**
   * Returns the key of the binding from which this type got constructed.
   *
   * @return the binding key
   */
  public String getBindingKey() {
    return fBindingKey;
  }

  /**
   * Returns the modifiers for this type.
   *
   * @return the bit-wise or of <code>Modifier</code> constants
   * @see org.eclipse.jdt.core.dom.IBinding#getModifiers()
   * @see org.eclipse.jdt.core.dom.Modifier
   */
  public int getModifiers() {
    return fModifiers;
  }

  /**
   * Returns the element kind
   *
   * @return the element kind.
   */
  public abstract int getKind();

  /**
   * Returns whether this type represents <code>java.lang.Object</code> or not.
   *
   * @return whether this type is <code>java.lang.Object</code> or not
   */
  public boolean isJavaLangObject() {
    return false;
  }

  /**
   * Returns whether this type represents <code>java.lang.Cloneable</code> or not.
   *
   * @return whether this type is <code>java.lang.Cloneable</code> or not
   */
  public boolean isJavaLangCloneable() {
    return false;
  }

  /**
   * Returns whether this type represents <code>java.io.Serializable</code> or not.
   *
   * @return whether this type is <code>java.io.Serializable</code> or not
   */
  public boolean isJavaIoSerializable() {
    return false;
  }

  /**
   * Returns <code>true</code> if the given type represents the null type. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is the null type or not
   */
  public final boolean isNullType() {
    return getKind() == NULL_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents the void type. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is the void type or not
   */
  public final boolean isVoidType() {
    return getKind() == VOID_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a primitive type. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is a primitive type or not
   */
  public final boolean isPrimitiveType() {
    return getKind() == PRIMITIVE_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents an array type. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is an array type or not
   */
  public final boolean isArrayType() {
    return getKind() == ARRAY_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a hierarchy type. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is a hierarchy type or not
   */
  public final boolean isHierarchyType() {
    int elementType = getKind();
    return elementType == RAW_TYPE
        || elementType == PARAMETERIZED_TYPE
        || elementType == GENERIC_TYPE
        || elementType == STANDARD_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a standard type. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is a standard type or not
   */
  public final boolean isStandardType() {
    return getKind() == STANDARD_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a raw type. Otherwise <code>false</code>
   * is returned.
   *
   * @return whether this type is a raw type or not
   */
  public final boolean isRawType() {
    return getKind() == RAW_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a parameterized type. Otherwise <code>
   * false</code> is returned.
   *
   * @return whether this type is a parameterized type or not
   */
  public final boolean isParameterizedType() {
    return getKind() == PARAMETERIZED_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a generic type. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is a generic type or not
   */
  public final boolean isGenericType() {
    return getKind() == GENERIC_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a type variable. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is a type variable or not
   */
  public final boolean isTypeVariable() {
    return getKind() == TYPE_VARIABLE;
  }

  /**
   * Returns <code>true</code> if the given type represents a capture type. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is a capture type or not
   */
  public final boolean isCaptureType() {
    return getKind() == CAPTURE_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a wildcard type. Otherwise <code>false
   * </code> is returned.
   *
   * @return whether this type is a wildcard type or not
   */
  public final boolean isWildcardType() {
    int elementType = getKind();
    return elementType == EXTENDS_WILDCARD_TYPE
        || elementType == UNBOUND_WILDCARD_TYPE
        || elementType == SUPER_WILDCARD_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a unbound wildcard type. Otherwise
   * <code>false</code> is returned.
   *
   * @return whether this type is a unbound wildcard type or not
   */
  public final boolean isUnboundWildcardType() {
    return getKind() == UNBOUND_WILDCARD_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents an extends wildcard type. Otherwise
   * <code>false</code> is returned.
   *
   * @return whether this type is an extends wildcard type or not
   */
  public final boolean isExtendsWildcardType() {
    return getKind() == EXTENDS_WILDCARD_TYPE;
  }

  /**
   * Returns <code>true</code> if the given type represents a super wildcard type. Otherwise <code>
   * false</code> is returned.
   *
   * @return whether this type is a super wildcard type or not
   */
  public final boolean isSuperWildcardType() {
    return getKind() == SUPER_WILDCARD_TYPE;
  }

  /**
   * Returns whether this type represents a class.
   *
   * @return whether this type represents a class
   * @see ITypeBinding#isClass()
   */
  public final boolean isClass() {
    return (fFlags & F_IS_CLASS) != 0;
  }

  /**
   * Returns whether this type represents a interface.
   *
   * @return whether this type represents a interface
   * @see ITypeBinding#isInterface()
   */
  public final boolean isInterface() {
    return (fFlags & F_IS_INTERFACE) != 0;
  }

  /**
   * Returns whether this type represents a enumeration.
   *
   * @return whether this type represents a enumeration
   * @see ITypeBinding#isEnum()
   */
  public final boolean isEnum() {
    return (fFlags & F_IS_ENUM) != 0;
  }

  /**
   * Returns whether this type represents an annotation.
   *
   * @return whether this type represents an annotation
   * @see ITypeBinding#isAnnotation()
   */
  public final boolean isAnnotation() {
    return (fFlags & F_IS_ANNOTATION) != 0;
  }

  /**
   * Returns whether this type represents a top level type.
   *
   * @return whether this type represents a top level type
   * @see ITypeBinding#isTopLevel()
   */
  public final boolean isTopLevel() {
    return (fFlags & F_IS_TOP_LEVEL) != 0;
  }

  /**
   * Returns whether this type represents a nested type.
   *
   * @return whether this type represents a nested type
   * @see ITypeBinding#isNested()
   */
  public final boolean isNested() {
    return (fFlags & F_IS_NESTED) != 0;
  }

  /**
   * Returns whether this type represents a member type.
   *
   * @return whether this type represents a member type
   * @see ITypeBinding#isMember()
   */
  public final boolean isMember() {
    return (fFlags & F_IS_MEMBER) != 0;
  }

  /**
   * Returns whether this type represents a local type.
   *
   * @return whether this type represents a local type
   * @see ITypeBinding#isLocal()
   */
  public final boolean isLocal() {
    return (fFlags & F_IS_LOCAL) != 0;
  }

  /**
   * Returns whether this type represents an anonymous type.
   *
   * @return whether this type represents an anonymous type
   * @see ITypeBinding#isAnonymous()
   */
  public final boolean isAnonymous() {
    return (fFlags & F_IS_ANONYMOUS) != 0;
  }

  /**
   * Returns the super classes of this type or <code>null</code>.
   *
   * @return the super class of this type
   */
  public TType getSuperclass() {
    return null;
  }

  /**
   * Returns the interfaces this type implements or extends.
   *
   * @return the "super" interfaces or an empty array
   */
  public TType[] getInterfaces() {
    return EMPTY_TYPE_ARRAY;
  }

  public boolean isEqualTo(ITypeBinding binding) {
    if (binding == null) return false;
    return binding.getKey().equals(fBindingKey);
  }

  /** {@inheritDoc} */
  @Override
  public final boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof TType)) return false;
    TType otherType = (TType) other;
    if (getKind() != otherType.getKind()) return false;
    return doEquals(otherType);
  }

  @Override
  public abstract int hashCode();

  /**
   * Performs the actual equals check.
   *
   * @param type The right hand side of the equals operation. The dynamic type of the actual
   *     argument must be the same as the receiver type.
   * @return <code>true</code> iff this type is the same as the argument
   */
  protected abstract boolean doEquals(TType type);

  /**
   * Returns the erasure of this type as defined by ITypeBinding#getErasure().
   *
   * @return the erasure of this type
   */
  public TType getErasure() {
    return this;
  }

  /**
   * Returns the type for the type declaration corresponding to this type.
   *
   * @return the type representing the declaration of this type
   * @see ITypeBinding#getTypeDeclaration()
   */
  public TType getTypeDeclaration() {
    return this;
  }

  /**
   * @return direct subtypes of this type
   * @throws IllegalStateException if this type's TypeEnvironment was not created with
   *     rememberSubtypes == true
   */
  public TType[] getSubTypes() throws IllegalStateException {
    Map<TType, ArrayList<TType>> subTypes = fEnvironment.getSubTypes();
    if (subTypes == null)
      throw new IllegalStateException(
          "This TypeEnvironment does not remember subtypes"); // $NON-NLS-1$
    List<TType> subtypes = subTypes.get(this);
    if (subtypes == null) return EMPTY_TYPE_ARRAY;
    else return subtypes.toArray(new TType[subtypes.size()]);
  }

  /**
   * Answer <code>true</code> if the receiver of this method can be assigned to the argument lhs
   * (e.g lhs= this is a valid assignment).
   *
   * @param lhs the left hand side of the assignment
   * @return whether or not this type can be assigned to lhs
   */
  public final boolean canAssignTo(TType lhs) {
    if (this.isTypeEquivalentTo(lhs)) return true;
    return doCanAssignTo(lhs);
  }

  /**
   * Returns whether the receiver type is type equivalent to the other type. This method considers
   * the erasure for generic, raw and parameterized types.
   *
   * @param other the other type
   * @return whether the receiver is type equivalent to other
   */
  protected boolean isTypeEquivalentTo(TType other) {
    return this.equals(other);
  }

  /**
   * Checks whether the <code>this</code> left hand side type interpreted as a type argument of a
   * parameterized type is compatible with the given type <code>rhs</code>. For example if <code>
   * List&lt;this&gt;= List&lt;rhs&gt;</code> is a valid assignment.
   *
   * @param rhs the right-hand-side type
   * @return <code>true</code> iff <code>this</code> contains <code>rhs</code> according to JLS3
   *     4.5.1.1
   */
  protected boolean checkTypeArgument(TType rhs) {
    return this.equals(rhs);
  }

  /**
   * Hook method to perform the actual can assign test
   *
   * @param lhs the left hand side of the assignment
   * @return whether or not this type can be assigned to lhs
   */
  protected abstract boolean doCanAssignTo(TType lhs);

  /**
   * Returns the name of this type as defined by {@link ITypeBinding#getName()}.
   *
   * @return the name of this type
   * @see ITypeBinding#getName()
   */
  public abstract String getName();

  /**
   * Returns a signature of this type which can be presented to the user.
   *
   * @return a pretty signature for this type
   */
  public String getPrettySignature() {
    return getPlainPrettySignature();
  }

  /**
   * Computes a plain pretty signature. For type with bounds (e.g type variables and wildcards) the
   * plain signature is different than the full pretty signature.
   *
   * @return a plain pretty signature for this type
   */
  protected abstract String getPlainPrettySignature();

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return getPrettySignature();
  }
}
