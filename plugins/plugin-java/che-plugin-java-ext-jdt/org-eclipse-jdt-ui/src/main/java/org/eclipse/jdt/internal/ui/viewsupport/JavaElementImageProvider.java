/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Matt Chapman,
 * mpchapman@gmail.com - 89977 Make JDT .java agnostic
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.viewsupport;

import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.che.jdt.util.JdtFlags;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/** Default strategy of the Java plugin for the construction of Java element icons. */
public class JavaElementImageProvider {

  /** Flags for the JavaImageLabelProvider: Generate images with overlays. */
  public static final int OVERLAY_ICONS = 0x1;

  /** Generate small sized images. */
  public static final int SMALL_ICONS = 0x2;

  /** Use the 'light' style for rendering types. */
  public static final int LIGHT_TYPE_ICONS = 0x4;

  public static final Point SMALL_SIZE = new Point(16, 16);
  public static final Point BIG_SIZE = new Point(22, 16);

  private static ImageDescriptor DESC_OBJ_PROJECT_CLOSED = new ImageDescriptor("projectClosed");
  private static ImageDescriptor DESC_OBJ_PROJECT = new ImageDescriptor("projectOpened");

  {
    //		ISharedImages images = JavaPlugin.getDefault().getWorkbench().getSharedImages();
    //		DESC_OBJ_PROJECT_CLOSED =
    // images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED);
    //		DESC_OBJ_PROJECT = images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
  }

  private ImageDescriptorRegistry fRegistry;

  public JavaElementImageProvider() {
    fRegistry = null; // lazy initialization
  }

  /**
   * Returns the icon for a given element. The icon depends on the element type and element
   * properties. If configured, overlay icons are constructed for <code>ISourceReference</code>s.
   *
   * @param element the element
   * @param flags Flags as defined by the JavaImageLabelProvider
   * @return return the image or <code>null</code>
   */
  public Image getImageLabel(Object element, int flags) {
    return getImageLabel(computeDescriptor(element, flags));
  }

  private Image getImageLabel(ImageDescriptor descriptor) {
    if (descriptor == null) return null;
    return getRegistry().get(descriptor);
  }

  private ImageDescriptorRegistry getRegistry() {
    if (fRegistry == null) {
      fRegistry = JavaPlugin.getImageDescriptorRegistry();
    }
    return fRegistry;
  }

  private ImageDescriptor computeDescriptor(Object element, int flags) {
    if (element instanceof IJavaElement) {
      return getJavaImageDescriptor((IJavaElement) element, flags);
    }
    //		} else if (element instanceof IFile) {
    //			IFile file = (IFile)element;
    //			if (JavaCore.isJavaLikeFileName(file.getName())) {
    //				return getCUResourceImageDescriptor(file, flags); // image for a CU not on the build path
    //			}
    //			return getWorkbenchImageDescriptor(file, flags);
    //		} else if (element instanceof IAdaptable) {
    //			return getWorkbenchImageDescriptor((IAdaptable)element, flags);
    //		}
    return null;
  }

  private static boolean showOverlayIcons(int flags) {
    return (flags & OVERLAY_ICONS) != 0;
  }

  private static boolean useSmallSize(int flags) {
    return (flags & SMALL_ICONS) != 0;
  }

  private static boolean useLightIcons(int flags) {
    return (flags & LIGHT_TYPE_ICONS) != 0;
  }

  //	/**
  //	 * Returns an image descriptor for a compilation unit not on the class path.
  //	 * The descriptor includes overlays, if specified.
  //	 * @param file the cu resource file
  //	 * @param flags the image flags
  //	 * @return returns the image descriptor
  //	 */
  //	public ImageDescriptor getCUResourceImageDescriptor(IFile file, int flags) {
  //		Point size = useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
  //		return new JavaElementImageDescriptor(JavaPluginImages.DESC_OBJS_CUNIT_RESOURCE, 0, size);
  //	}

  /**
   * Returns an image descriptor for a java element. The descriptor includes overlays, if specified.
   *
   * @param element the Java element
   * @param flags the image flags
   * @return returns the image descriptor
   */
  public ImageDescriptor getJavaImageDescriptor(IJavaElement element, int flags) {
    Point size = useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;

    ImageDescriptor baseDesc = getBaseImageDescriptor(element, flags);
    if (baseDesc != null) {
      int adornmentFlags = computeJavaAdornmentFlags(element, flags);
      return new JavaElementImageDescriptor(baseDesc, adornmentFlags /*, size*/);
    }
    return new JavaElementImageDescriptor(JavaPluginImages.DESC_OBJS_GHOST, 0 /*, size*/);
  }

  //	/**
  //	 * Returns an image descriptor for a IAdaptable. The descriptor includes overlays, if specified
  // (only error ticks apply).
  //	 * Returns <code>null</code> if no image could be found.
  //	 * @param adaptable the adaptable
  //	 * @param flags the image flags
  //	 * @return returns the image descriptor
  //	 */
  //	public ImageDescriptor getWorkbenchImageDescriptor(IAdaptable adaptable, int flags) {
  //		IWorkbenchAdapter wbAdapter= (IWorkbenchAdapter)
  // adaptable.getAdapter(IWorkbenchAdapter.class);
  //		if (wbAdapter == null) {
  //			return null;
  //		}
  //		ImageDescriptor descriptor= wbAdapter.getImageDescriptor(adaptable);
  //		if (descriptor == null) {
  //			return null;
  //		}
  //
  //		Point size= useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
  //		return new JavaElementImageDescriptor(descriptor, 0/*, size*/);
  //	}

  // ---- Computation of base image key -------------------------------------------------

  /**
   * Returns an image descriptor for a java element. This is the base image, no overlays.
   *
   * @param element the element
   * @param renderFlags the image flags
   * @return returns the image descriptor
   */
  public ImageDescriptor getBaseImageDescriptor(IJavaElement element, int renderFlags) {

    try {
      switch (element.getElementType()) {
        case IJavaElement.INITIALIZER:
          return JavaPluginImages.DESC_MISC_PRIVATE; // 23479
        case IJavaElement.METHOD:
          {
            IMethod method = (IMethod) element;
            IType declType = method.getDeclaringType();
            int flags = method.getFlags();
            if (declType.isEnum() && isDefaultFlag(flags) && method.isConstructor())
              return JavaPluginImages.DESC_MISC_PRIVATE;
            return getMethodImageDescriptor(JavaModelUtil.isInterfaceOrAnnotation(declType), flags);
          }
        case IJavaElement.FIELD:
          {
            IMember member = (IMember) element;
            IType declType = member.getDeclaringType();
            return getFieldImageDescriptor(
                JavaModelUtil.isInterfaceOrAnnotation(declType), member.getFlags());
          }
        case IJavaElement.LOCAL_VARIABLE:
          return JavaPluginImages.DESC_OBJS_LOCAL_VARIABLE;

        case IJavaElement.PACKAGE_DECLARATION:
          return JavaPluginImages.DESC_OBJS_PACKDECL;

        case IJavaElement.IMPORT_DECLARATION:
          return JavaPluginImages.DESC_OBJS_IMPDECL;

        case IJavaElement.IMPORT_CONTAINER:
          return JavaPluginImages.DESC_OBJS_IMPCONT;

        case IJavaElement.TYPE:
          {
            IType type = (IType) element;

            IType declType = type.getDeclaringType();
            boolean isInner = declType != null;
            boolean isInInterfaceOrAnnotation =
                isInner && JavaModelUtil.isInterfaceOrAnnotation(declType);
            return getTypeImageDescriptor(
                isInner, isInInterfaceOrAnnotation, type.getFlags(), useLightIcons(renderFlags));
          }

        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
          {
            IPackageFragmentRoot root = (IPackageFragmentRoot) element;
            IPath attach = root.getSourceAttachmentPath();
            if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
              if (root.isArchive()) {
                if (root.isExternal()) {
                  if (attach == null) {
                    return JavaPluginImages.DESC_OBJS_EXTJAR;
                  } else {
                    return JavaPluginImages.DESC_OBJS_EXTJAR_WSRC;
                  }
                } else {
                  if (attach == null) {
                    return JavaPluginImages.DESC_OBJS_JAR;
                  } else {
                    return JavaPluginImages.DESC_OBJS_JAR_WSRC;
                  }
                }
              } else {
                if (attach == null) {
                  return JavaPluginImages.DESC_OBJS_CLASSFOLDER;
                } else {
                  return JavaPluginImages.DESC_OBJS_CLASSFOLDER_WSRC;
                }
              }
            } else {
              return JavaPluginImages.DESC_OBJS_PACKFRAG_ROOT;
            }
          }

        case IJavaElement.PACKAGE_FRAGMENT:
          return getPackageFragmentIcon(element);

        case IJavaElement.COMPILATION_UNIT:
          return JavaPluginImages.DESC_OBJS_CUNIT;

        case IJavaElement.CLASS_FILE:
          /* this is too expensive for large packages
          try {
          	IClassFile cfile= (IClassFile)element;
          	if (cfile.isClass())
          		return JavaPluginImages.IMG_OBJS_CFILECLASS;
          	return JavaPluginImages.IMG_OBJS_CFILEINT;
          } catch(JavaModelException e) {
          	// fall through;
          }*/
          return JavaPluginImages.DESC_OBJS_CFILE;

        case IJavaElement.JAVA_PROJECT:
          IJavaProject jp = (IJavaProject) element;
          if (jp.getProject().isOpen()) {
            //						IProject project= jp.getProject();
            //						IWorkbenchAdapter adapter=
            // (IWorkbenchAdapter)project.getAdapter(IWorkbenchAdapter.class);
            //						if (adapter != null) {
            //							ImageDescriptor result= adapter.getImageDescriptor(project);
            //							if (result != null)
            //								return result;
            //						}
            return DESC_OBJ_PROJECT;
          }
          return DESC_OBJ_PROJECT_CLOSED;

        case IJavaElement.JAVA_MODEL:
          return JavaPluginImages.DESC_OBJS_JAVA_MODEL;

        case IJavaElement.TYPE_PARAMETER:
          return JavaPluginImages.DESC_OBJS_TYPEVARIABLE;

        case IJavaElement.ANNOTATION:
          return JavaPluginImages.DESC_OBJS_ANNOTATION;

        default:
          // ignore. Must be a new, yet unknown Java element
          //					// give an advanced IWorkbenchAdapter the chance
          //					IWorkbenchAdapter wbAdapter= (IWorkbenchAdapter)
          // element.getAdapter(IWorkbenchAdapter.class);
          //					if (wbAdapter != null && !(wbAdapter instanceof JavaWorkbenchAdapter)) { // avoid
          // recursion
          //						ImageDescriptor imageDescriptor= wbAdapter.getImageDescriptor(element);
          //						if (imageDescriptor != null) {
          //							return imageDescriptor;
          //						}
          //					}
          return JavaPluginImages.DESC_OBJS_GHOST;
      }

    } catch (JavaModelException e) {
      if (e.isDoesNotExist()) return JavaPluginImages.DESC_OBJS_UNKNOWN;
      JavaPlugin.log(e);
      return JavaPluginImages.DESC_OBJS_GHOST;
    }
  }

  private static boolean isDefaultFlag(int flags) {
    return !Flags.isPublic(flags) && !Flags.isProtected(flags) && !Flags.isPrivate(flags);
  }

  private ImageDescriptor getPackageFragmentIcon(IJavaElement element) throws JavaModelException {
    IPackageFragment fragment = (IPackageFragment) element;
    boolean containsJavaElements = false;
    try {
      containsJavaElements = fragment.hasChildren();
    } catch (JavaModelException e) {
      // assuming no children;
    }
    if (!containsJavaElements && (fragment.getNonJavaResources().length > 0))
      return JavaPluginImages.DESC_OBJS_EMPTY_PACKAGE_RESOURCES;
    else if (!containsJavaElements) return JavaPluginImages.DESC_OBJS_EMPTY_PACKAGE;
    return JavaPluginImages.DESC_OBJS_PACKAGE;
  }

  public void dispose() {}

  // ---- Methods to compute the adornments flags ---------------------------------

  private int computeJavaAdornmentFlags(IJavaElement element, int renderFlags) {
    int flags = 0;
    if (showOverlayIcons(renderFlags)) {
      try {
        if (element instanceof IMember) {
          IMember member = (IMember) element;

          int modifiers = member.getFlags();
          if (confirmAbstract(member) && JdtFlags.isAbstract(member))
            flags |= JavaElementImageDescriptor.ABSTRACT;
          if (Flags.isFinal(modifiers)
              || isInterfaceOrAnnotationField(member)
              || isEnumConstant(member, modifiers)) flags |= JavaElementImageDescriptor.FINAL;
          if (Flags.isStatic(modifiers)
              || isInterfaceOrAnnotationFieldOrType(member)
              || isEnumConstant(member, modifiers)) flags |= JavaElementImageDescriptor.STATIC;

          if (Flags.isDeprecated(modifiers)) flags |= JavaElementImageDescriptor.DEPRECATED;

          int elementType = element.getElementType();
          if (elementType == IJavaElement.METHOD) {
            if (((IMethod) element).isConstructor())
              flags |= JavaElementImageDescriptor.CONSTRUCTOR;
            if (Flags.isSynchronized(modifiers)) // collides with 'super' flag
            flags |= JavaElementImageDescriptor.SYNCHRONIZED;
            if (Flags.isNative(modifiers)) flags |= JavaElementImageDescriptor.NATIVE;
            if (Flags.isDefaultMethod(modifiers))
              flags |= JavaElementImageDescriptor.DEFAULT_METHOD;
            if (Flags.isAnnnotationDefault(modifiers))
              flags |= JavaElementImageDescriptor.ANNOTATION_DEFAULT;
          }

          if (member.getElementType() == IJavaElement.TYPE) {
            if (JavaModelUtil.hasMainMethod((IType) member)) {
              flags |= JavaElementImageDescriptor.RUNNABLE;
            }
          }

          if (member.getElementType() == IJavaElement.FIELD) {
            if (Flags.isVolatile(modifiers)) flags |= JavaElementImageDescriptor.VOLATILE;
            if (Flags.isTransient(modifiers)) flags |= JavaElementImageDescriptor.TRANSIENT;
          }
        } else if (element instanceof ILocalVariable
            && Flags.isFinal(((ILocalVariable) element).getFlags())) {
          flags |= JavaElementImageDescriptor.FINAL;
        }
      } catch (JavaModelException e) {
        // do nothing. Can't compute runnable adornment or get flags
      }
    }
    return flags;
  }

  private static boolean confirmAbstract(IMember element) throws JavaModelException {
    // never show the abstract symbol on interfaces
    if (element.getElementType() == IJavaElement.TYPE) {
      return !JavaModelUtil.isInterfaceOrAnnotation((IType) element);
    }
    return true;
  }

  private static boolean isInterfaceOrAnnotationField(IMember element) throws JavaModelException {
    // always show the final symbol on interface fields
    if (element.getElementType() == IJavaElement.FIELD) {
      return JavaModelUtil.isInterfaceOrAnnotation(element.getDeclaringType());
    }
    return false;
  }

  private static boolean isInterfaceOrAnnotationFieldOrType(IMember element)
      throws JavaModelException {
    // always show the static symbol on interface fields and types
    if (element.getElementType() == IJavaElement.FIELD) {
      return JavaModelUtil.isInterfaceOrAnnotation(element.getDeclaringType());
    } else if (element.getElementType() == IJavaElement.TYPE
        && element.getDeclaringType() != null) {
      return JavaModelUtil.isInterfaceOrAnnotation(element.getDeclaringType());
    }
    return false;
  }

  private static boolean isEnumConstant(IMember element, int modifiers) {
    if (element.getElementType() == IJavaElement.FIELD) {
      return Flags.isEnum(modifiers);
    }
    return false;
  }

  public static ImageDescriptor getMethodImageDescriptor(
      boolean isInInterfaceOrAnnotation, int flags) {
    if (Flags.isPublic(flags) || isInInterfaceOrAnnotation)
      return JavaPluginImages.DESC_MISC_PUBLIC;
    if (Flags.isProtected(flags)) return JavaPluginImages.DESC_MISC_PROTECTED;
    if (Flags.isPrivate(flags)) return JavaPluginImages.DESC_MISC_PRIVATE;

    return JavaPluginImages.DESC_MISC_DEFAULT;
  }

  public static ImageDescriptor getFieldImageDescriptor(
      boolean isInInterfaceOrAnnotation, int flags) {
    if (Flags.isPublic(flags) || isInInterfaceOrAnnotation || Flags.isEnum(flags))
      return JavaPluginImages.DESC_FIELD_PUBLIC;
    if (Flags.isProtected(flags)) return JavaPluginImages.DESC_FIELD_PROTECTED;
    if (Flags.isPrivate(flags)) return JavaPluginImages.DESC_FIELD_PRIVATE;

    return JavaPluginImages.DESC_FIELD_DEFAULT;
  }

  public static ImageDescriptor getTypeImageDescriptor(
      boolean isInner, boolean isInInterfaceOrAnnotation, int flags, boolean useLightIcons) {
    if (Flags.isEnum(flags)) {
      if (useLightIcons) {
        return JavaPluginImages.DESC_OBJS_ENUM_ALT;
      }
      if (isInner) {
        return getInnerEnumImageDescriptor(isInInterfaceOrAnnotation, flags);
      }
      return getEnumImageDescriptor(flags);
    } else if (Flags.isAnnotation(flags)) {
      if (useLightIcons) {
        return JavaPluginImages.DESC_OBJS_ANNOTATION_ALT;
      }
      if (isInner) {
        return getInnerAnnotationImageDescriptor(isInInterfaceOrAnnotation, flags);
      }
      return getAnnotationImageDescriptor(flags);
    } else if (Flags.isInterface(flags)) {
      if (useLightIcons) {
        return JavaPluginImages.DESC_OBJS_INTERFACEALT;
      }
      if (isInner) {
        return getInnerInterfaceImageDescriptor(isInInterfaceOrAnnotation, flags);
      }
      return getInterfaceImageDescriptor(flags);
    } else {
      if (useLightIcons) {
        return JavaPluginImages.DESC_OBJS_CLASSALT;
      }
      if (isInner) {
        return getInnerClassImageDescriptor(isInInterfaceOrAnnotation, flags);
      }
      return getClassImageDescriptor(flags);
    }
  }

  public static Image getDecoratedImage(ImageDescriptor baseImage, int adornments, Point size) {
    return JavaPlugin.getImageDescriptorRegistry()
        .get(new JavaElementImageDescriptor(baseImage, adornments /*, size*/));
  }

  private static ImageDescriptor getClassImageDescriptor(int flags) {
    if (Flags.isPublic(flags) || Flags.isProtected(flags) || Flags.isPrivate(flags))
      return JavaPluginImages.DESC_OBJS_CLASS;
    else return JavaPluginImages.DESC_OBJS_CLASS_DEFAULT;
  }

  private static ImageDescriptor getInnerClassImageDescriptor(
      boolean isInInterfaceOrAnnotation, int flags) {
    if (Flags.isPublic(flags) || isInInterfaceOrAnnotation)
      return JavaPluginImages.DESC_OBJS_INNER_CLASS_PUBLIC;
    else if (Flags.isPrivate(flags)) return JavaPluginImages.DESC_OBJS_INNER_CLASS_PRIVATE;
    else if (Flags.isProtected(flags)) return JavaPluginImages.DESC_OBJS_INNER_CLASS_PROTECTED;
    else return JavaPluginImages.DESC_OBJS_INNER_CLASS_DEFAULT;
  }

  private static ImageDescriptor getEnumImageDescriptor(int flags) {
    if (Flags.isPublic(flags) || Flags.isProtected(flags) || Flags.isPrivate(flags))
      return JavaPluginImages.DESC_OBJS_ENUM;
    else return JavaPluginImages.DESC_OBJS_ENUM_DEFAULT;
  }

  private static ImageDescriptor getInnerEnumImageDescriptor(
      boolean isInInterfaceOrAnnotation, int flags) {
    if (Flags.isPublic(flags) || isInInterfaceOrAnnotation) return JavaPluginImages.DESC_OBJS_ENUM;
    else if (Flags.isPrivate(flags)) return JavaPluginImages.DESC_OBJS_ENUM_PRIVATE;
    else if (Flags.isProtected(flags)) return JavaPluginImages.DESC_OBJS_ENUM_PROTECTED;
    else return JavaPluginImages.DESC_OBJS_ENUM_DEFAULT;
  }

  private static ImageDescriptor getAnnotationImageDescriptor(int flags) {
    if (Flags.isPublic(flags) || Flags.isProtected(flags) || Flags.isPrivate(flags))
      return JavaPluginImages.DESC_OBJS_ANNOTATION;
    else return JavaPluginImages.DESC_OBJS_ANNOTATION_DEFAULT;
  }

  private static ImageDescriptor getInnerAnnotationImageDescriptor(
      boolean isInInterfaceOrAnnotation, int flags) {
    if (Flags.isPublic(flags) || isInInterfaceOrAnnotation)
      return JavaPluginImages.DESC_OBJS_ANNOTATION;
    else if (Flags.isPrivate(flags)) return JavaPluginImages.DESC_OBJS_ANNOTATION_PRIVATE;
    else if (Flags.isProtected(flags)) return JavaPluginImages.DESC_OBJS_ANNOTATION_PROTECTED;
    else return JavaPluginImages.DESC_OBJS_ANNOTATION_DEFAULT;
  }

  private static ImageDescriptor getInterfaceImageDescriptor(int flags) {
    if (Flags.isPublic(flags) || Flags.isProtected(flags) || Flags.isPrivate(flags))
      return JavaPluginImages.DESC_OBJS_INTERFACE;
    else return JavaPluginImages.DESC_OBJS_INTERFACE_DEFAULT;
  }

  private static ImageDescriptor getInnerInterfaceImageDescriptor(
      boolean isInInterfaceOrAnnotation, int flags) {
    if (Flags.isPublic(flags) || isInInterfaceOrAnnotation)
      return JavaPluginImages.DESC_OBJS_INNER_INTERFACE_PUBLIC;
    else if (Flags.isPrivate(flags)) return JavaPluginImages.DESC_OBJS_INNER_INTERFACE_PRIVATE;
    else if (Flags.isProtected(flags)) return JavaPluginImages.DESC_OBJS_INNER_INTERFACE_PROTECTED;
    else return JavaPluginImages.DESC_OBJS_INTERFACE_DEFAULT;
  }
}
