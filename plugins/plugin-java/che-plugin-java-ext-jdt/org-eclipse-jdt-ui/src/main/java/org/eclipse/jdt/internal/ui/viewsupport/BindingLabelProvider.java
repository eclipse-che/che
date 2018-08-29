/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.viewsupport;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Label provider to render bindings in viewers.
 *
 * @since 3.1
 */
public class BindingLabelProvider /*extends LabelProvider */ {

  private static int getAdornmentFlags(IBinding binding) {
    int adornments = 0;
    final int modifiers = binding.getModifiers();
    if (Modifier.isAbstract(modifiers)) adornments |= JavaElementImageDescriptor.ABSTRACT;
    if (Modifier.isFinal(modifiers)) adornments |= JavaElementImageDescriptor.FINAL;
    if (Modifier.isStatic(modifiers)) adornments |= JavaElementImageDescriptor.STATIC;

    if (binding.isDeprecated()) adornments |= JavaElementImageDescriptor.DEPRECATED;

    if (binding instanceof IMethodBinding) {
      if (((IMethodBinding) binding).isConstructor())
        adornments |= JavaElementImageDescriptor.CONSTRUCTOR;
      if (Modifier.isSynchronized(modifiers)) adornments |= JavaElementImageDescriptor.SYNCHRONIZED;
      if (Modifier.isNative(modifiers)) adornments |= JavaElementImageDescriptor.NATIVE;
      ITypeBinding type = ((IMethodBinding) binding).getDeclaringClass();
      if (type.isInterface() && !Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers))
        adornments |= JavaElementImageDescriptor.DEFAULT_METHOD;
      if (((IMethodBinding) binding).getDefaultValue() != null)
        adornments |= JavaElementImageDescriptor.ANNOTATION_DEFAULT;
    }
    if (binding instanceof IVariableBinding && ((IVariableBinding) binding).isField()) {
      if (Modifier.isTransient(modifiers)) adornments |= JavaElementImageDescriptor.TRANSIENT;
      if (Modifier.isVolatile(modifiers)) adornments |= JavaElementImageDescriptor.VOLATILE;
    }
    return adornments;
  }

  private static ImageDescriptor getBaseImageDescriptor(IBinding binding, int flags) {
    if (binding instanceof ITypeBinding) {
      ITypeBinding typeBinding = (ITypeBinding) binding;
      if (typeBinding.isArray()) {
        typeBinding = typeBinding.getElementType();
      }
      if (typeBinding.isCapture()) {
        typeBinding.getWildcard();
      }
      return getTypeImageDescriptor(typeBinding.getDeclaringClass() != null, typeBinding, flags);
    } else if (binding instanceof IMethodBinding) {
      ITypeBinding type = ((IMethodBinding) binding).getDeclaringClass();
      int modifiers = binding.getModifiers();
      if (type.isEnum()
          && (!Modifier.isPublic(modifiers)
              && !Modifier.isProtected(modifiers)
              && !Modifier.isPrivate(modifiers))
          && ((IMethodBinding) binding).isConstructor()) return JavaPluginImages.DESC_MISC_PRIVATE;
      return getMethodImageDescriptor(binding.getModifiers());
    } else if (binding instanceof IVariableBinding)
      return getFieldImageDescriptor((IVariableBinding) binding);
    return JavaPluginImages.DESC_OBJS_UNKNOWN;
  }

  private static ImageDescriptor getClassImageDescriptor(int modifiers) {
    if (Modifier.isPublic(modifiers)
        || Modifier.isProtected(modifiers)
        || Modifier.isPrivate(modifiers)) return JavaPluginImages.DESC_OBJS_CLASS;
    else return JavaPluginImages.DESC_OBJS_CLASS_DEFAULT;
  }

  private static ImageDescriptor getFieldImageDescriptor(IVariableBinding binding) {
    final int modifiers = binding.getModifiers();
    if (Modifier.isPublic(modifiers) || binding.isEnumConstant())
      return JavaPluginImages.DESC_FIELD_PUBLIC;
    if (Modifier.isProtected(modifiers)) return JavaPluginImages.DESC_FIELD_PROTECTED;
    if (Modifier.isPrivate(modifiers)) return JavaPluginImages.DESC_FIELD_PRIVATE;

    return JavaPluginImages.DESC_FIELD_DEFAULT;
  }

  private static void getFieldLabel(IVariableBinding binding, long flags, StringBuffer buffer) {
    if (((flags & JavaElementLabels.F_PRE_TYPE_SIGNATURE) != 0) && !binding.isEnumConstant()) {
      getTypeLabel(binding.getType(), (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
      buffer.append(' ');
    }
    // qualification

    if ((flags & JavaElementLabels.F_FULLY_QUALIFIED) != 0) {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      if (declaringClass != null) { // test for array.length
        getTypeLabel(
            declaringClass,
            JavaElementLabels.T_FULLY_QUALIFIED | (flags & JavaElementLabels.P_COMPRESSED),
            buffer);
        buffer.append('.');
      }
    }
    buffer.append(binding.getName());
    if (((flags & JavaElementLabels.F_APP_TYPE_SIGNATURE) != 0) && !binding.isEnumConstant()) {
      buffer.append(JavaElementLabels.DECL_STRING);
      getTypeLabel(binding.getType(), (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
    }
    // post qualification
    if ((flags & JavaElementLabels.F_POST_QUALIFIED) != 0) {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      if (declaringClass != null) { // test for array.length
        buffer.append(JavaElementLabels.CONCAT_STRING);
        getTypeLabel(
            declaringClass,
            JavaElementLabels.T_FULLY_QUALIFIED | (flags & JavaElementLabels.P_COMPRESSED),
            buffer);
      }
    }
  }

  private static void getLocalVariableLabel(
      IVariableBinding binding, long flags, StringBuffer buffer) {
    if (((flags & JavaElementLabels.F_PRE_TYPE_SIGNATURE) != 0)) {
      getTypeLabel(binding.getType(), (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
      buffer.append(' ');
    }
    if (((flags & JavaElementLabels.F_FULLY_QUALIFIED) != 0)) {
      IMethodBinding declaringMethod = binding.getDeclaringMethod();
      if (declaringMethod != null) {
        getMethodLabel(declaringMethod, flags, buffer);
        buffer.append('.');
      }
    }
    buffer.append(binding.getName());
    if (((flags & JavaElementLabels.F_APP_TYPE_SIGNATURE) != 0)) {
      buffer.append(JavaElementLabels.DECL_STRING);
      getTypeLabel(binding.getType(), (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
    }
  }

  private static ImageDescriptor getInnerClassImageDescriptor(int modifiers) {
    if (Modifier.isPublic(modifiers)) return JavaPluginImages.DESC_OBJS_INNER_CLASS_PUBLIC;
    else if (Modifier.isPrivate(modifiers)) return JavaPluginImages.DESC_OBJS_INNER_CLASS_PRIVATE;
    else if (Modifier.isProtected(modifiers))
      return JavaPluginImages.DESC_OBJS_INNER_CLASS_PROTECTED;
    else return JavaPluginImages.DESC_OBJS_INNER_CLASS_DEFAULT;
  }

  private static ImageDescriptor getInnerInterfaceImageDescriptor(int modifiers) {
    if (Modifier.isPublic(modifiers)) return JavaPluginImages.DESC_OBJS_INNER_INTERFACE_PUBLIC;
    else if (Modifier.isPrivate(modifiers))
      return JavaPluginImages.DESC_OBJS_INNER_INTERFACE_PRIVATE;
    else if (Modifier.isProtected(modifiers))
      return JavaPluginImages.DESC_OBJS_INNER_INTERFACE_PROTECTED;
    else return JavaPluginImages.DESC_OBJS_INTERFACE_DEFAULT;
  }

  private static ImageDescriptor getInterfaceImageDescriptor(int modifiers) {
    if (Modifier.isPublic(modifiers)
        || Modifier.isProtected(modifiers)
        || Modifier.isPrivate(modifiers)) return JavaPluginImages.DESC_OBJS_INTERFACE;
    else return JavaPluginImages.DESC_OBJS_INTERFACE_DEFAULT;
  }

  private static ImageDescriptor getMethodImageDescriptor(int modifiers) {
    if (Modifier.isPublic(modifiers)) return JavaPluginImages.DESC_MISC_PUBLIC;
    if (Modifier.isProtected(modifiers)) return JavaPluginImages.DESC_MISC_PROTECTED;
    if (Modifier.isPrivate(modifiers)) return JavaPluginImages.DESC_MISC_PRIVATE;

    return JavaPluginImages.DESC_MISC_DEFAULT;
  }

  private static void appendDimensions(int dim, StringBuffer buffer) {
    for (int i = 0; i < dim; i++) {
      buffer.append('[').append(']');
    }
  }

  private static void getMethodLabel(IMethodBinding binding, long flags, StringBuffer buffer) {
    // return type
    if ((flags & JavaElementLabels.M_PRE_TYPE_PARAMETERS) != 0) {
      if (binding.isGenericMethod()) {
        ITypeBinding[] typeParameters = binding.getTypeParameters();
        if (typeParameters.length > 0) {
          getTypeParametersLabel(typeParameters, buffer);
          buffer.append(' ');
        }
      }
    }
    // return type
    if (((flags & JavaElementLabels.M_PRE_RETURNTYPE) != 0) && !binding.isConstructor()) {
      getTypeLabel(binding.getReturnType(), (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
      buffer.append(' ');
    }
    // qualification
    if ((flags & JavaElementLabels.M_FULLY_QUALIFIED) != 0) {
      getTypeLabel(
          binding.getDeclaringClass(),
          JavaElementLabels.T_FULLY_QUALIFIED | (flags & JavaElementLabels.P_COMPRESSED),
          buffer);
      buffer.append('.');
    }
    buffer.append(binding.getName());
    if ((flags & JavaElementLabels.M_APP_TYPE_PARAMETERS) != 0) {
      if (binding.isParameterizedMethod()) {
        ITypeBinding[] typeArguments = binding.getTypeArguments();
        if (typeArguments.length > 0) {
          buffer.append(' ');
          getTypeArgumentsLabel(
              typeArguments, (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
        }
      }
    }

    // parameters
    buffer.append('(');
    if ((flags & JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES) != 0) {
      ITypeBinding[] parameters =
          ((flags & JavaElementLabels.M_PARAMETER_TYPES) != 0) ? binding.getParameterTypes() : null;
      if (parameters != null) {
        for (int index = 0; index < parameters.length; index++) {
          if (index > 0) {
            buffer.append(JavaElementLabels.COMMA_STRING);
          }
          ITypeBinding paramType = parameters[index];
          if (binding.isVarargs() && (index == parameters.length - 1)) {
            getTypeLabel(
                paramType.getElementType(), (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
            appendDimensions(paramType.getDimensions() - 1, buffer);
            buffer.append(JavaElementLabels.ELLIPSIS_STRING);
          } else {
            getTypeLabel(paramType, (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
          }
        }
      }
    } else {
      if (binding.getParameterTypes().length > 0) {
        buffer.append(JavaElementLabels.ELLIPSIS_STRING);
      }
    }
    buffer.append(')');

    if ((flags & JavaElementLabels.M_EXCEPTIONS) != 0) {
      ITypeBinding[] exceptions = binding.getExceptionTypes();
      if (exceptions.length > 0) {
        buffer.append(" throws "); // $NON-NLS-1$
        for (int index = 0; index < exceptions.length; index++) {
          if (index > 0) {
            buffer.append(JavaElementLabels.COMMA_STRING);
          }
          getTypeLabel(exceptions[index], (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
        }
      }
    }
    if ((flags & JavaElementLabels.M_APP_TYPE_PARAMETERS) != 0) {
      if (binding.isGenericMethod()) {
        ITypeBinding[] typeParameters = binding.getTypeParameters();
        if (typeParameters.length > 0) {
          buffer.append(' ');
          getTypeParametersLabel(typeParameters, buffer);
        }
      }
    }
    if (((flags & JavaElementLabels.M_APP_RETURNTYPE) != 0) && !binding.isConstructor()) {
      buffer.append(JavaElementLabels.DECL_STRING);
      getTypeLabel(binding.getReturnType(), (flags & JavaElementLabels.T_TYPE_PARAMETERS), buffer);
    }
    // post qualification
    if ((flags & JavaElementLabels.M_POST_QUALIFIED) != 0) {
      buffer.append(JavaElementLabels.CONCAT_STRING);
      getTypeLabel(
          binding.getDeclaringClass(),
          JavaElementLabels.T_FULLY_QUALIFIED | (flags & JavaElementLabels.P_COMPRESSED),
          buffer);
    }
  }

  private static ImageDescriptor getTypeImageDescriptor(
      boolean inner, ITypeBinding binding, int flags) {
    if (binding.isEnum()) return JavaPluginImages.DESC_OBJS_ENUM;
    else if (binding.isAnnotation()) return JavaPluginImages.DESC_OBJS_ANNOTATION;
    else if (binding.isInterface()) {
      if ((flags & JavaElementImageProvider.LIGHT_TYPE_ICONS) != 0)
        return JavaPluginImages.DESC_OBJS_INTERFACEALT;
      if (inner) return getInnerInterfaceImageDescriptor(binding.getModifiers());
      return getInterfaceImageDescriptor(binding.getModifiers());
    } else if (binding.isClass()) {
      if ((flags & JavaElementImageProvider.LIGHT_TYPE_ICONS) != 0)
        return JavaPluginImages.DESC_OBJS_CLASSALT;
      if (inner) return getInnerClassImageDescriptor(binding.getModifiers());
      return getClassImageDescriptor(binding.getModifiers());
    } else if (binding.isTypeVariable()) {
      return JavaPluginImages.DESC_OBJS_TYPEVARIABLE;
    }
    // primitive type, wildcard
    return null;
  }

  private static void getTypeLabel(ITypeBinding binding, long flags, StringBuffer buffer) {
    if ((flags & JavaElementLabels.T_FULLY_QUALIFIED) != 0) {
      final IPackageBinding pack = binding.getPackage();
      if (pack != null && !pack.isUnnamed()) {
        buffer.append(pack.getName());
        buffer.append('.');
      }
    }
    if ((flags & (JavaElementLabels.T_FULLY_QUALIFIED | JavaElementLabels.T_CONTAINER_QUALIFIED))
        != 0) {
      final ITypeBinding declaring = binding.getDeclaringClass();
      if (declaring != null) {
        getTypeLabel(
            declaring,
            JavaElementLabels.T_CONTAINER_QUALIFIED | (flags & JavaElementLabels.P_COMPRESSED),
            buffer);
        buffer.append('.');
      }
      final IMethodBinding declaringMethod = binding.getDeclaringMethod();
      if (declaringMethod != null) {
        getMethodLabel(declaringMethod, 0, buffer);
        buffer.append('.');
      }
    }

    if (binding.isCapture()) {
      getTypeLabel(binding.getWildcard(), flags & JavaElementLabels.T_TYPE_PARAMETERS, buffer);
    } else if (binding.isWildcardType()) {
      buffer.append('?');
      ITypeBinding bound = binding.getBound();
      if (bound != null) {
        if (binding.isUpperbound()) {
          buffer.append(" extends "); // $NON-NLS-1$
        } else {
          buffer.append(" super "); // $NON-NLS-1$
        }
        getTypeLabel(bound, flags & JavaElementLabels.T_TYPE_PARAMETERS, buffer);
      }
    } else if (binding.isArray()) {
      getTypeLabel(binding.getElementType(), flags & JavaElementLabels.T_TYPE_PARAMETERS, buffer);
      appendDimensions(binding.getDimensions(), buffer);
    } else { // type variables, primitive, reftype
      String name = binding.getTypeDeclaration().getName();
      if (name.length() == 0) {
        if (binding.isEnum()) {
          buffer.append('{' + JavaElementLabels.ELLIPSIS_STRING + '}');
        } else if (binding.isAnonymous()) {
          ITypeBinding[] superInterfaces = binding.getInterfaces();
          ITypeBinding baseType;
          if (superInterfaces.length > 0) {
            baseType = superInterfaces[0];
          } else {
            baseType = binding.getSuperclass();
          }
          if (baseType != null) {
            StringBuffer anonymBaseType = new StringBuffer();
            getTypeLabel(baseType, flags & JavaElementLabels.T_TYPE_PARAMETERS, anonymBaseType);
            buffer.append(
                Messages.format(
                    JavaUIMessages.JavaElementLabels_anonym_type, anonymBaseType.toString()));
          } else {
            buffer.append(JavaUIMessages.JavaElementLabels_anonym);
          }
        } else {
          buffer.append("UNKNOWN"); // $NON-NLS-1$
        }
      } else {
        buffer.append(name);
      }

      if ((flags & JavaElementLabels.T_TYPE_PARAMETERS) != 0) {
        if (binding.isGenericType()) {
          getTypeParametersLabel(binding.getTypeParameters(), buffer);
        } else if (binding.isParameterizedType()) {
          getTypeArgumentsLabel(binding.getTypeArguments(), flags, buffer);
        }
      }
    }

    if ((flags & JavaElementLabels.T_POST_QUALIFIED) != 0) {
      final IMethodBinding declaringMethod = binding.getDeclaringMethod();
      final ITypeBinding declaringType = binding.getDeclaringClass();
      if (declaringMethod != null) {
        buffer.append(JavaElementLabels.CONCAT_STRING);
        getMethodLabel(
            declaringMethod,
            JavaElementLabels.T_FULLY_QUALIFIED | (flags & JavaElementLabels.P_COMPRESSED),
            buffer);
      } else if (declaringType != null) {
        buffer.append(JavaElementLabels.CONCAT_STRING);
        getTypeLabel(
            declaringType,
            JavaElementLabels.T_FULLY_QUALIFIED | (flags & JavaElementLabels.P_COMPRESSED),
            buffer);
      } else {
        final IPackageBinding pack = binding.getPackage();
        if (pack != null && !pack.isUnnamed()) {
          buffer.append(JavaElementLabels.CONCAT_STRING);
          buffer.append(pack.getName());
        }
      }
    }
  }

  private static void getTypeArgumentsLabel(ITypeBinding[] typeArgs, long flags, StringBuffer buf) {
    if (typeArgs.length > 0) {
      buf.append('<');
      for (int i = 0; i < typeArgs.length; i++) {
        if (i > 0) {
          buf.append(JavaElementLabels.COMMA_STRING);
        }
        getTypeLabel(typeArgs[i], flags & JavaElementLabels.T_TYPE_PARAMETERS, buf);
      }
      buf.append('>');
    }
  }

  private static void getTypeParametersLabel(ITypeBinding[] typeParameters, StringBuffer buffer) {
    if (typeParameters.length > 0) {
      buffer.append('<');
      for (int index = 0; index < typeParameters.length; index++) {
        if (index > 0) {
          buffer.append(JavaElementLabels.COMMA_STRING);
        }
        buffer.append(typeParameters[index].getName());
      }
      buffer.append('>');
    }
  }

  /**
   * Returns the label for a Java element with the flags as defined by {@link JavaElementLabels}.
   *
   * @param binding The binding to render.
   * @param flags The text flags as defined in {@link JavaElementLabels}
   * @return the label of the binding
   */
  public static String getBindingLabel(IBinding binding, long flags) {
    StringBuffer buffer = new StringBuffer(60);
    if (binding instanceof ITypeBinding) {
      getTypeLabel(((ITypeBinding) binding), flags, buffer);
    } else if (binding instanceof IMethodBinding) {
      getMethodLabel(((IMethodBinding) binding), flags, buffer);
    } else if (binding instanceof IVariableBinding) {
      final IVariableBinding variable = (IVariableBinding) binding;
      if (variable.isField()) getFieldLabel(variable, flags, buffer);
      else getLocalVariableLabel(variable, flags, buffer);
    }
    return Strings.markLTR(buffer.toString());
  }

  /**
   * Returns the image descriptor for a binding with the flags as defined by {@link
   * JavaElementImageProvider}.
   *
   * @param binding The binding to get the image for.
   * @param imageFlags The image flags as defined in {@link JavaElementImageProvider}.
   * @return the image of the binding or null if there is no image
   */
  public static ImageDescriptor getBindingImageDescriptor(IBinding binding, int imageFlags) {
    ImageDescriptor baseImage = getBaseImageDescriptor(binding, imageFlags);
    if (baseImage != null) {
      int adornmentFlags = getAdornmentFlags(binding);
      Point size =
          ((imageFlags & JavaElementImageProvider.SMALL_ICONS) != 0)
              ? JavaElementImageProvider.SMALL_SIZE
              : JavaElementImageProvider.BIG_SIZE;
      return new JavaElementImageDescriptor(baseImage, adornmentFlags /*, size*/);
    }
    return null;
  }

  public static final long DEFAULT_TEXTFLAGS = JavaElementLabels.ALL_DEFAULT;
  public static final int DEFAULT_IMAGEFLAGS = JavaElementImageProvider.OVERLAY_ICONS;

  private final long fTextFlags;
  private final int fImageFlags;

  private ImageDescriptorRegistry fRegistry;

  /** Creates a new binding label provider with default text and image flags */
  public BindingLabelProvider() {
    this(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS);
  }

  /**
   * @param textFlags Flags defined in {@link JavaElementLabels}.
   * @param imageFlags Flags defined in {@link JavaElementImageProvider}.
   */
  public BindingLabelProvider(final long textFlags, final int imageFlags) {
    fImageFlags = imageFlags;
    fTextFlags = textFlags;
    fRegistry = null;
  }

  /*
   * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
   */
  //	@Override
  public Image getImage(Object element) {
    if (element instanceof IBinding) {
      ImageDescriptor baseImage = getBindingImageDescriptor((IBinding) element, fImageFlags);
      if (baseImage != null) {
        return getRegistry().get(baseImage);
      }
    }
    return null;
  }

  private ImageDescriptorRegistry getRegistry() {
    if (fRegistry == null) fRegistry = JavaPlugin.getImageDescriptorRegistry();
    return fRegistry;
  }

  /*
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
   */
  //	@Override
  public String getText(Object element) {
    if (element instanceof IBinding) {
      return getBindingLabel((IBinding) element, fTextFlags);
    }
    return element == null ? "" : element.toString(); // $NON-NLS-1$
  }
}
