package org.eclipse.jdt.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/** @author Evgen Vidolob */
public class JavaPluginImages {

  public static final ImageDescriptor DESC_OBJS_TEMPLATE = new ImageDescriptor("template");
  public static final ImageDescriptor DESC_OBJS_JAVADOCTAG = new ImageDescriptor("javadoc");
  public static final ImageDescriptor IMG_OBJS_JAVADOCTAG = new ImageDescriptor("javadoc");

  public static final ImageDescriptor DESC_OBJS_ANNOTATION = new ImageDescriptor("annotation");
  public static final ImageDescriptor IMG_OBJS_ANNOTATION = new ImageDescriptor("annotation");
  public static final ImageDescriptor DESC_OBJS_ANNOTATION_ALT = new ImageDescriptor("annotation");
  public static final ImageDescriptor DESC_OBJS_ANNOTATION_DEFAULT =
      new ImageDescriptor("defaultAnnotation");
  public static final ImageDescriptor DESC_OBJS_ANNOTATION_PRIVATE =
      new ImageDescriptor("privateAnnotation");
  public static final ImageDescriptor DESC_OBJS_ANNOTATION_PROTECTED =
      new ImageDescriptor("protectedAnnotation");

  public static final ImageDescriptor DESC_OBJS_ENUM_ALT = new ImageDescriptor("enum");
  public static final ImageDescriptor DESC_OBJS_ENUM = new ImageDescriptor("enum");
  public static final ImageDescriptor IMG_OBJS_ENUM = new ImageDescriptor("enum");
  public static final ImageDescriptor DESC_OBJS_ENUM_DEFAULT = new ImageDescriptor("defaultEnum");
  public static final ImageDescriptor DESC_OBJS_ENUM_PRIVATE = new ImageDescriptor("privateEnum");
  public static final ImageDescriptor DESC_OBJS_ENUM_PROTECTED =
      new ImageDescriptor("protectedEnum");

  public static final ImageDescriptor DESC_OBJS_INTERFACE = new ImageDescriptor("interface");
  public static final ImageDescriptor IMG_OBJS_INTERFACE = new ImageDescriptor("interface");
  public static final ImageDescriptor DESC_OBJS_INTERFACEALT = new ImageDescriptor("interface");
  public static final ImageDescriptor DESC_OBJS_INTERFACE_DEFAULT =
      new ImageDescriptor("defaultInterface");
  public static final ImageDescriptor DESC_OBJS_INNER_INTERFACE_PUBLIC =
      new ImageDescriptor("innerInterfacePublic");
  public static final ImageDescriptor DESC_OBJS_INNER_INTERFACE_PRIVATE =
      new ImageDescriptor("innerInterfacePrivate");
  public static final ImageDescriptor DESC_OBJS_INNER_INTERFACE_PROTECTED =
      new ImageDescriptor("innerInterfaceProtected");

  public static final ImageDescriptor DESC_OBJS_CLASS = new ImageDescriptor("class");
  public static final ImageDescriptor IMG_OBJS_CLASS = new ImageDescriptor("class");
  public static final ImageDescriptor DESC_OBJS_CLASSALT = new ImageDescriptor("class");
  public static final ImageDescriptor DESC_OBJS_CLASS_DEFAULT = new ImageDescriptor("defaultClass");
  public static final ImageDescriptor DESC_OBJS_INNER_CLASS_PUBLIC =
      new ImageDescriptor("innerClassPublic");
  public static final ImageDescriptor DESC_OBJS_INNER_CLASS_PRIVATE =
      new ImageDescriptor("innerClassPrivate");
  public static final ImageDescriptor DESC_OBJS_INNER_CLASS_PROTECTED =
      new ImageDescriptor("innerClassProtected");
  public static final ImageDescriptor DESC_OBJS_INNER_CLASS_DEFAULT =
      new ImageDescriptor("innerClassDefault");

  public static final ImageDescriptor DESC_MISC_PRIVATE = new ImageDescriptor("privateMethod");
  public static final ImageDescriptor IMG_MISC_PRIVATE = new ImageDescriptor("privateMethod");
  public static final ImageDescriptor DESC_MISC_PUBLIC = new ImageDescriptor("publicMethod");
  public static final ImageDescriptor IMG_MISC_PUBLIC = new ImageDescriptor("publicMethod");
  public static final ImageDescriptor DESC_MISC_PROTECTED = new ImageDescriptor("protectedMethod");
  public static final ImageDescriptor IMG_MISC_PROTECTED = new ImageDescriptor("protectedMethod");
  public static final ImageDescriptor DESC_MISC_DEFAULT = new ImageDescriptor("defaultMethod");

  public static final ImageDescriptor DESC_FIELD_PUBLIC = new ImageDescriptor("publicField");
  public static final ImageDescriptor IMG_FIELD_PUBLIC = new ImageDescriptor("publicField");
  public static final ImageDescriptor DESC_FIELD_PROTECTED = new ImageDescriptor("protectedField");
  public static final ImageDescriptor DESC_FIELD_PRIVATE = new ImageDescriptor("privateField");
  public static final ImageDescriptor IMG_FIELD_PRIVATE = new ImageDescriptor("privateField");
  public static final ImageDescriptor DESC_FIELD_DEFAULT = new ImageDescriptor("defaultField");

  public static final ImageDescriptor DESC_OBJS_LOCAL_VARIABLE =
      new ImageDescriptor("localVariable");
  public static final ImageDescriptor DESC_OBJS_PACKDECL =
      new ImageDescriptor("packageDeclaration");
  public static final ImageDescriptor IMG_OBJS_PACKDECL = new ImageDescriptor("packageDeclaration");
  public static final ImageDescriptor DESC_OBJS_IMPDECL = new ImageDescriptor("importDeclaration");
  public static final ImageDescriptor DESC_OBJS_IMPCONT = new ImageDescriptor("importContainer");
  public static final ImageDescriptor DESC_OBJS_EXTJAR = new ImageDescriptor("extJar");
  public static final ImageDescriptor DESC_OBJS_EXTJAR_WSRC = new ImageDescriptor("extJarWSRC");
  public static final ImageDescriptor DESC_OBJS_JAR = new ImageDescriptor("jar");
  public static final ImageDescriptor DESC_OBJS_JAR_WSRC = new ImageDescriptor("jarWSRC");
  public static final ImageDescriptor DESC_OBJS_CLASSFOLDER = new ImageDescriptor("classfolder");
  public static final ImageDescriptor DESC_OBJS_CLASSFOLDER_WSRC =
      new ImageDescriptor("classfolderWSRC");
  public static final ImageDescriptor DESC_OBJS_PACKFRAG_ROOT =
      new ImageDescriptor("packageFragmentRoot");
  public static final ImageDescriptor DESC_OBJS_CUNIT = new ImageDescriptor("cunit"); // *.java file
  public static final ImageDescriptor DESC_OBJS_CFILE =
      new ImageDescriptor("cfile"); // *.class file
  public static final ImageDescriptor DESC_OBJS_JAVA_MODEL = new ImageDescriptor("javaModel");
  public static final ImageDescriptor DESC_OBJS_TYPEVARIABLE = new ImageDescriptor("typevariable");
  public static final ImageDescriptor DESC_OBJS_UNKNOWN = new ImageDescriptor("unknown");
  public static final ImageDescriptor DESC_OBJS_EMPTY_PACKAGE_RESOURCES =
      new ImageDescriptor("emptyPackage");
  public static final ImageDescriptor DESC_OBJS_EMPTY_PACKAGE = new ImageDescriptor("emptyPackage");
  public static final ImageDescriptor DESC_OBJS_PACKAGE = new ImageDescriptor("package");

  public static final ImageDescriptor IMG_CORRECTION_LOCAL = new ImageDescriptor("correctionLocal");
  public static final ImageDescriptor IMG_CORRECTION_CHANGE =
      new ImageDescriptor("correctionChange");
  public static final ImageDescriptor IMG_CORRECTION_ADD = new ImageDescriptor("correctionAdd");
  public static final ImageDescriptor IMG_OBJS_EXCEPTION = new ImageDescriptor("jexception");
  public static final ImageDescriptor IMG_CORRECTION_REMOVE =
      new ImageDescriptor("correctionRemove");
  public static final ImageDescriptor IMG_CORRECTION_CAST = new ImageDescriptor("correctionCast");
  public static final ImageDescriptor IMG_CORRECTION_MOVE = new ImageDescriptor("correctionMove");
  public static final ImageDescriptor IMG_CORRECTION_DELETE_IMPORT =
      new ImageDescriptor("correctionDeleteImport");
  public static final ImageDescriptor IMG_CORRECTION_RENAME =
      new ImageDescriptor("correctionRename");
  public static final ImageDescriptor IMG_OBJS_IMPDECL = new ImageDescriptor("impObj");
  public static final ImageDescriptor IMG_CORRECTION_LINKED_RENAME =
      new ImageDescriptor("linkedRename");

  public static final ImageDescriptor IMG_TOOL_DELETE = new ImageDescriptor("toolDelete");

  public static final ImageDescriptor DESC_OBJS_GHOST = new ImageDescriptor("ghost");

  public static final ImageDescriptor DESC_OBJS_SEARCH_DECL =
      new ImageDescriptor("search_decl_obj");
  public static final ImageDescriptor DESC_OBJS_SEARCH_REF = new ImageDescriptor("search_ref_obj");

  public static Image get(ImageDescriptor key) {
    return new Image(key);
  }
}
