/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt;

import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.jdt.dom.ASTNodes;
import org.eclipse.che.jdt.javadoc.HTMLPrinter;
import org.eclipse.che.jdt.javadoc.JavaDocLocations;
import org.eclipse.che.jdt.javadoc.JavaElementLabels;
import org.eclipse.che.jdt.javadoc.JavaElementLinks;
import org.eclipse.che.jdt.javadoc.JavadocContentAccess2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.core.JavaProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
@Singleton
public class JavadocFinder {
  private static final Logger LOG = LoggerFactory.getLogger(JavadocFinder.class);
  private static final long LABEL_FLAGS =
      JavaElementLabels.ALL_FULLY_QUALIFIED
          | JavaElementLabels.M_PRE_RETURNTYPE
          | JavaElementLabels.M_PARAMETER_ANNOTATIONS
          | JavaElementLabels.M_PARAMETER_TYPES
          | JavaElementLabels.M_PARAMETER_NAMES
          | JavaElementLabels.M_EXCEPTIONS
          | JavaElementLabels.F_PRE_TYPE_SIGNATURE
          | JavaElementLabels.M_PRE_TYPE_PARAMETERS
          | JavaElementLabels.T_TYPE_PARAMETERS
          | JavaElementLabels.USE_RESOLVED;
  private static final long LOCAL_VARIABLE_FLAGS =
      LABEL_FLAGS & ~JavaElementLabels.F_FULLY_QUALIFIED | JavaElementLabels.F_POST_QUALIFIED;
  private static final long TYPE_PARAMETER_FLAGS =
      LABEL_FLAGS | JavaElementLabels.TP_POST_QUALIFIED;
  private static final long PACKAGE_FLAGS = LABEL_FLAGS & ~JavaElementLabels.ALL_FULLY_QUALIFIED;
  private static String styleSheet;
  private String baseHref;

  public JavadocFinder(String baseHref) {
    this.baseHref = baseHref;
  }

  private static long getHeaderFlags(IJavaElement element) {
    switch (element.getElementType()) {
      case IJavaElement.LOCAL_VARIABLE:
        return LOCAL_VARIABLE_FLAGS;
      case IJavaElement.TYPE_PARAMETER:
        return TYPE_PARAMETER_FLAGS;
      case IJavaElement.PACKAGE_FRAGMENT:
        return PACKAGE_FLAGS;
      default:
        return LABEL_FLAGS;
    }
  }

  private static IBinding resolveBinding(ASTNode node) {
    if (node instanceof SimpleName) {
      SimpleName simpleName = (SimpleName) node;
      // workaround for https://bugs.eclipse.org/62605 (constructor name resolves to type, not
      // method)
      ASTNode normalized = ASTNodes.getNormalizedNode(simpleName);
      if (normalized.getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY) {
        ClassInstanceCreation cic = (ClassInstanceCreation) normalized.getParent();
        IMethodBinding constructorBinding = cic.resolveConstructorBinding();
        if (constructorBinding == null) return null;
        ITypeBinding declaringClass = constructorBinding.getDeclaringClass();
        if (!declaringClass.isAnonymous()) return constructorBinding;
        ITypeBinding superTypeDeclaration = declaringClass.getSuperclass().getTypeDeclaration();
        return resolveSuperclassConstructor(superTypeDeclaration, constructorBinding);
      }
      return simpleName.resolveBinding();

    } else if (node instanceof SuperConstructorInvocation) {
      return ((SuperConstructorInvocation) node).resolveConstructorBinding();
    } else if (node instanceof ConstructorInvocation) {
      return ((ConstructorInvocation) node).resolveConstructorBinding();
    } else {
      return null;
    }
  }

  private static IBinding resolveSuperclassConstructor(
      ITypeBinding superClassDeclaration, IMethodBinding constructor) {
    IMethodBinding[] methods = superClassDeclaration.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      IMethodBinding method = methods[i];
      if (method.isConstructor() && constructor.isSubsignature(method)) return method;
    }
    return null;
  }

  private static StringBuffer addLink(StringBuffer buf, String uri, String label) {
    return buf.append(JavaElementLinks.createLink(uri, label));
  }

  private static String getImageURL(IJavaElement element) {
    String imageName = null;
    // todo
    URL imageUrl = null; // JavaPlugin.getDefault().getImagesOnFSRegistry().getImageURL(element);
    if (imageUrl != null) {
      imageName = imageUrl.toExternalForm();
    }

    return imageName;
  }

  public String findJavadoc4Handle(IJavaProject project, String handle) {
    IJavaElement javaElement = JavaElementLinks.parseURI(handle, (JavaProject) project);
    if (javaElement == null || !(javaElement instanceof IMember)) {
      return null;
    }
    return getJavadoc((IMember) javaElement);
  }

  public String findJavadoc(IJavaProject project, String fqn, int offset)
      throws JavaModelException {

    IMember member = null;
    IType type = project.findType(fqn);
    ICodeAssist codeAssist;
    if (type.isBinary()) {
      codeAssist = type.getClassFile();
    } else {
      codeAssist = type.getCompilationUnit();
    }

    IJavaElement[] elements = null;
    if (codeAssist != null) {
      elements = codeAssist.codeSelect(/*region.getOffset(), region.getLength()*/ offset, 0);
    }
    IJavaElement element = null;
    if (elements != null && elements.length > 0) {
      element = elements[0];
    }

    if (element != null && element instanceof IMember) {
      member = ((IMember) element);
    }
    if (member == null) {
      return null;
    }
    return getJavadoc(member);
  }

  private String getJavadoc(IMember element) {
    StringBuffer buffer = new StringBuffer();
    boolean hasContents = false;
    if (element instanceof IPackageFragment || element instanceof IMember) {
      HTMLPrinter.addSmallHeader(buffer, getInfoText(element, element.getTypeRoot(), true));
      buffer.append("<br>"); // $NON-NLS-1$
      addAnnotations(buffer, element, element.getTypeRoot(), null);
      Reader reader = null;
      try {
        String content =
            element instanceof IMember
                ? JavadocContentAccess2.getHTMLContent(element, true, baseHref)
                : null; // JavadocContentAccess2.getHTMLContent((IPackageFragment)element);
        IPackageFragmentRoot root =
            (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        if (content != null) {
          reader = new StringReader(content);
        } else {
          String explanationForMissingJavadoc =
              JavaDocLocations.getExplanationForMissingJavadoc(element, root);
          if (explanationForMissingJavadoc != null)
            reader = new StringReader(explanationForMissingJavadoc);
        }
      } catch (CoreException ex) {
        reader = new StringReader(JavaDocLocations.handleFailedJavadocFetch(ex));
      }

      if (reader != null) {
        HTMLPrinter.addParagraph(buffer, reader);
      }
      hasContents = true;
    }

    if (!hasContents) return null;

    if (buffer.length() > 0) {
      HTMLPrinter.insertPageProlog(buffer, 0, getStyleSheet());
      HTMLPrinter.addPageEpilog(buffer);
      return buffer.toString();
    }

    return null;
  }

  public static String getStyleSheet() {
    if (styleSheet == null) {
      try (InputStream stream =
          JavadocFinder.class
              .getClassLoader()
              .getResource("JavadocHoverStyleSheet.css")
              .openStream()) {
        styleSheet = IoUtil.readStream(stream);
      } catch (IOException e) {
        LOG.error("Can't read JavadocHoverStyleSheet.css", e);
      }
    }
    return styleSheet;
  }

  private String getInfoText(
      IJavaElement element, ITypeRoot editorInputElement, boolean allowImage) {
    long flags = getHeaderFlags(element);
    StringBuffer label = new StringBuffer(JavaElementLinks.getElementLabel(element, flags));

    if (element.getElementType() == IJavaElement.FIELD) {
      String constantValue = getConstantValue((IField) element, editorInputElement);
      if (constantValue != null) {
        constantValue = HTMLPrinter.convertToHTMLContentWithWhitespace(constantValue);
        IJavaProject javaProject = element.getJavaProject();
        label.append(getFormattedAssignmentOperator(javaProject));
        label.append(constantValue);
      }
    }

    //		if (element.getElementType() == IJavaElement.METHOD) {
    //			IMethod method= (IMethod)element;
    //			//TODO: add default value for annotation type members, see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=249016
    //		}

    return getImageAndLabel(element, allowImage, label.toString());
  }

  /**
   * Returns the assignment operator string with the project's formatting applied to it.
   *
   * @param javaProject the Java project whose formatting options will be used.
   * @return the formatted assignment operator string.
   * @since 3.4
   */
  public static String getFormattedAssignmentOperator(IJavaProject javaProject) {
    StringBuffer buffer = new StringBuffer();
    if (JavaCore.INSERT.equals(
        javaProject.getOption(
            DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, true)))
      buffer.append(' ');
    buffer.append('=');
    if (JavaCore.INSERT.equals(
        javaProject.getOption(
            DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR, true)))
      buffer.append(' ');
    return buffer.toString();
  }
  /**
   * Returns the constant value for the given field.
   *
   * @param field the field
   * @param editorInputElement the editor input element
   * @param hoverRegion the hover region in the editor
   * @return the constant value for the given field or <code>null</code> if none
   * @since 3.4
   */
  private String getConstantValue(IField field, ITypeRoot editorInputElement) {
    //        if (!isStaticFinal(field))
    //            return null;
    //
    //        ASTNode node= getHoveredASTNode(editorInputElement, hoverRegion);
    //        if (node == null)
    //            return null;
    //
    //        Object constantValue= getVariableBindingConstValue(node, field);
    //        if (constantValue == null)
    //            return null;
    //
    //        if (constantValue instanceof String) {
    //            return ASTNodes.getEscapedStringLiteral((String) constantValue);
    //        } else {
    //            return getHexConstantValue(constantValue);
    //        }
    return null;
  }

  public void addAnnotations(
      StringBuffer buf, IJavaElement element, ITypeRoot editorInputElement, IRegion hoverRegion) {
    try {
      if (element instanceof IAnnotatable) {
        String annotationString = getAnnotations(element, editorInputElement, hoverRegion);
        if (annotationString != null) {
          buf.append("<div style='margin-bottom: 5px;'>"); // $NON-NLS-1$
          buf.append(annotationString);
          buf.append("</div>"); // $NON-NLS-1$
        }
      } else if (element instanceof IPackageFragment) {
        //                IPackageFragment pack= (IPackageFragment) element;
        //                ICompilationUnit cu=
        // pack.getCompilationUnit(JavaModelUtil.PACKAGE_INFO_JAVA);
        //                if (cu.exists()) {
        //                    IPackageDeclaration[] packDecls= cu.getPackageDeclarations();
        //                    if (packDecls.length > 0) {
        //                        addAnnotations(buf, packDecls[0], null, null);
        //                    }
        //                } else {
        //                    IClassFile classFile=
        // pack.getClassFile(JavaModelUtil.PACKAGE_INFO_CLASS);
        //                    if (classFile.exists()) {
        //                        addAnnotations(buf, classFile.getType(), null, null);
        //                    }
        //                }
      }
    } catch (JavaModelException e) {
      // no annotations this time...
      buf.append("<br>"); // $NON-NLS-1$
    } catch (URISyntaxException e) {
      // no annotations this time...
      buf.append("<br>"); // $NON-NLS-1$
    }
  }

  private String getAnnotations(
      IJavaElement element, ITypeRoot editorInputElement, IRegion hoverRegion)
      throws URISyntaxException, JavaModelException {
    if (!(element instanceof IPackageFragment)) {
      if (!(element instanceof IAnnotatable)) return null;

      if (((IAnnotatable) element).getAnnotations().length == 0) return null;
    }

    IBinding binding = null;
    // TODO
    ASTNode node = null; // getHoveredASTNode(editorInputElement, hoverRegion);

    if (node == null) {
      // todo use ast ported parser,that uses our java model
      //            ASTParser p = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
      //            p.setProject(element.getJavaProject());
      //            p.setBindingsRecovery(true);
      //            try {
      //                binding = p.createBindings(new IJavaElement[]{element}, null)[0];
      //            } catch (OperationCanceledException e) {
      //                return null;
      //            }

    } else {
      binding = resolveBinding(node);
    }

    if (binding == null) return null;

    IAnnotationBinding[] annotations = binding.getAnnotations();
    if (annotations.length == 0) return null;

    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < annotations.length; i++) {
      // TODO: skip annotations that don't have an @Documented annotation?
      addAnnotation(buf, element, annotations[i]);
      buf.append("<br>"); // $NON-NLS-1$
    }

    return buf.toString();
  }

  private void addAnnotation(StringBuffer buf, IJavaElement element, IAnnotationBinding annotation)
      throws URISyntaxException {
    IJavaElement javaElement = annotation.getAnnotationType().getJavaElement();
    buf.append('@');
    if (javaElement != null) {
      String uri = JavaElementLinks.createURI(baseHref, javaElement);
      addLink(buf, uri, annotation.getName());
    } else {
      buf.append(annotation.getName());
    }

    IMemberValuePairBinding[] mvPairs = annotation.getDeclaredMemberValuePairs();
    if (mvPairs.length > 0) {
      buf.append('(');
      for (int j = 0; j < mvPairs.length; j++) {
        if (j > 0) {
          buf.append(JavaElementLabels.COMMA_STRING);
        }
        IMemberValuePairBinding mvPair = mvPairs[j];
        String memberURI =
            JavaElementLinks.createURI(baseHref, mvPair.getMethodBinding().getJavaElement());
        addLink(buf, memberURI, mvPair.getName());
        buf.append('=');
        addValue(buf, element, mvPair.getValue());
      }
      buf.append(')');
    }
  }

  private void addValue(StringBuffer buf, IJavaElement element, Object value)
      throws URISyntaxException {
    // Note: To be bug-compatible with Javadoc from Java 5/6/7, we currently don't escape HTML tags
    // in String-valued annotations.
    if (value instanceof ITypeBinding) {
      ITypeBinding typeBinding = (ITypeBinding) value;
      IJavaElement type = typeBinding.getJavaElement();
      if (type == null) {
        buf.append(typeBinding.getName());
      } else {
        String uri = JavaElementLinks.createURI(baseHref, type);
        String name = type.getElementName();
        addLink(buf, uri, name);
      }
      buf.append(".class"); // $NON-NLS-1$

    } else if (value instanceof IVariableBinding) { // only enum constants
      IVariableBinding variableBinding = (IVariableBinding) value;
      IJavaElement variable = variableBinding.getJavaElement();
      String uri = JavaElementLinks.createURI(baseHref, variable);
      String name = variable.getElementName();
      addLink(buf, uri, name);

    } else if (value instanceof IAnnotationBinding) {
      IAnnotationBinding annotationBinding = (IAnnotationBinding) value;
      addAnnotation(buf, element, annotationBinding);

    } else if (value instanceof String) {
      buf.append(ASTNodes.getEscapedStringLiteral((String) value));

    } else if (value instanceof Character) {
      buf.append(ASTNodes.getEscapedCharacterLiteral((Character) value));

    } else if (value instanceof Object[]) {
      Object[] values = (Object[]) value;
      buf.append('{');
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          buf.append(JavaElementLabels.COMMA_STRING);
        }
        addValue(buf, element, values[i]);
      }
      buf.append('}');

    } else { // primitive types (except char) or null
      buf.append(String.valueOf(value));
    }
  }

  public String getImageAndLabel(IJavaElement element, boolean allowImage, String label) {
    StringBuffer buf = new StringBuffer();
    int imageWidth = 16;
    int imageHeight = 16;
    int labelLeft = 20;
    int labelTop = 2;

    buf.append("<div style='word-wrap: break-word; position: relative; "); // $NON-NLS-1$

    String imageSrcPath = allowImage ? getImageURL(element) : null;
    if (imageSrcPath != null) {
      buf.append("margin-left: ").append(labelLeft).append("px; "); // $NON-NLS-1$ //$NON-NLS-2$
      buf.append("padding-top: ").append(labelTop).append("px; "); // $NON-NLS-1$ //$NON-NLS-2$
    }

    buf.append("'>"); // $NON-NLS-1$
    if (imageSrcPath != null) {
      if (element != null) {
        try {
          String uri = JavaElementLinks.createURI(baseHref, element);
          buf.append("<a href='").append(uri).append("'>"); // $NON-NLS-1$//$NON-NLS-2$
        } catch (URISyntaxException e) {
          element = null; // no link
        }
      }
      StringBuffer imageStyle =
          new StringBuffer("border:none; position: absolute; "); // $NON-NLS-1$
      imageStyle.append("width: ").append(imageWidth).append("px; "); // $NON-NLS-1$ //$NON-NLS-2$
      imageStyle.append("height: ").append(imageHeight).append("px; "); // $NON-NLS-1$ //$NON-NLS-2$
      imageStyle
          .append("left: ")
          .append(-labelLeft - 1)
          .append("px; "); // $NON-NLS-1$ //$NON-NLS-2$

      //            // hack for broken transparent PNG support in IE 6, see
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=223900 :
      //            buf.append("<!--[if lte IE 6]><![if gte IE 5.5]>\n"); //$NON-NLS-1$
      String tooltip =
          element == null
              ? ""
              : "alt='" + "Open Declaration" + "' "; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      //            buf.append("<span ").append(tooltip).append("style=\"").append(imageStyle).
      // //$NON-NLS-1$ //$NON-NLS-2$
      //
      // append("filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='").append(imageSrcPath).append("')
      // \"></span>\n"); //$NON-NLS-1$ //$NON-NLS-2$
      //            buf.append("<![endif]><![endif]-->\n"); //$NON-NLS-1$
      //
      //            buf.append("<!--[if !IE]>-->\n"); //$NON-NLS-1$
      buf.append("<img ")
          .append(tooltip)
          .append("style='")
          .append(imageStyle)
          .append("' src='")
          .append(imageSrcPath)
          .append("'/>\n"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      //            buf.append("<!--<![endif]-->\n"); //$NON-NLS-1$
      //            buf.append("<!--[if gte IE 7]>\n"); //$NON-NLS-1$
      //            buf.append("<img
      // ").append(tooltip).append("style='").append(imageStyle).append("'
      // src='").append(imageSrcPath).append
      // ("'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      //            buf.append("<![endif]-->\n"); //$NON-NLS-1$
      if (element != null) {
        buf.append("</a>"); // $NON-NLS-1$
      }
    }

    buf.append(label);

    buf.append("</div>"); // $NON-NLS-1$
    return buf.toString();
  }
}
