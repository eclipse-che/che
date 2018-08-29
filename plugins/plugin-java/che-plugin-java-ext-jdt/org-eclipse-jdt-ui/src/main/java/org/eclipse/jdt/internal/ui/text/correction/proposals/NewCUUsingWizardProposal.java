/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Renaud Waldura &lt;renaud+eclipse@waldura.com&gt; IBM Corporation - updates
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.UnresolvedElementsSubProcessor;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.jface.text.IDocument;

/**
 * This proposal is listed in the corrections list for a "type not found" problem. It offers to
 * create a new type by running the class/interface wizard. If selected, this proposal will open a
 * {@link NewClassCreationWizard}, {@link NewInterfaceCreationWizard}, {@link NewEnumCreationWizard}
 * or {@link NewAnnotationCreationWizard}.
 *
 * @see UnresolvedElementsSubProcessor#getTypeProposals(IInvocationContext, IProblemLocation,
 *     Collection)
 */
public class NewCUUsingWizardProposal extends ChangeCorrectionProposal {

  public static final int K_CLASS = 1;
  public static final int K_INTERFACE = 2;
  public static final int K_ENUM = 3;
  public static final int K_ANNOTATION = 4;

  private Name fNode;
  private ICompilationUnit fCompilationUnit;
  private int fTypeKind;
  private IJavaElement fTypeContainer; // IType or IPackageFragment
  private String fTypeNameWithParameters;
  private IType fCreatedType;

  private boolean fShowDialog;

  public NewCUUsingWizardProposal(
      ICompilationUnit cu, Name node, int typeKind, IJavaElement typeContainer, int severity) {
    super("", null, severity, null); // $NON-NLS-1$

    fCompilationUnit = cu;
    fNode = node;
    fTypeKind = typeKind;
    fTypeContainer = typeContainer;
    fTypeNameWithParameters = getTypeName(typeKind, node);

    fCreatedType = null;

    String containerName = ASTNodes.getQualifier(node);
    String typeName = fTypeNameWithParameters;
    String containerLabel = BasicElementLabels.getJavaElementName(containerName);
    String typeLabel = BasicElementLabels.getJavaElementName(typeName);
    boolean isInnerType = typeContainer instanceof IType;
    switch (typeKind) {
      case K_CLASS:
        setImage(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_CLASS));
        if (isInnerType) {
          if (containerName.length() == 0) {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinnerclass_description,
                    typeLabel));
          } else {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinnerclass_intype_description,
                    new String[] {typeLabel, containerLabel}));
          }
        } else {
          if (containerName.length() == 0) {
            setDisplayName(
                Messages.format(
                    CorrectionMessages.NewCUCompletionUsingWizardProposal_createclass_description,
                    typeLabel));
          } else {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createclass_inpackage_description,
                    new String[] {typeLabel, containerLabel}));
          }
        }
        break;
      case K_INTERFACE:
        setImage(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_INTERFACE));
        if (isInnerType) {
          if (containerName.length() == 0) {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinnerinterface_description,
                    typeLabel));
          } else {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinnerinterface_intype_description,
                    new String[] {typeLabel, containerLabel}));
          }
        } else {
          if (containerName.length() == 0) {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinterface_description,
                    typeLabel));
          } else {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinterface_inpackage_description,
                    new String[] {typeLabel, containerLabel}));
          }
        }
        break;
      case K_ENUM:
        setImage(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_ENUM));
        if (isInnerType) {
          if (containerName.length() == 0) {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinnerenum_description,
                    typeLabel));
          } else {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinnerenum_intype_description,
                    new String[] {typeLabel, containerLabel}));
          }
        } else {
          if (containerName.length() == 0) {
            setDisplayName(
                Messages.format(
                    CorrectionMessages.NewCUCompletionUsingWizardProposal_createenum_description,
                    typeLabel));
          } else {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createenum_inpackage_description,
                    new String[] {typeLabel, containerLabel}));
          }
        }
        break;
      case K_ANNOTATION:
        setImage(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_ANNOTATION));
        if (isInnerType) {
          if (containerName.length() == 0) {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinnerannotation_description,
                    typeLabel));
          } else {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createinnerannotation_intype_description,
                    new String[] {typeLabel, containerLabel}));
          }
        } else {
          if (containerName.length() == 0) {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createannotation_description,
                    typeLabel));
          } else {
            setDisplayName(
                Messages.format(
                    CorrectionMessages
                        .NewCUCompletionUsingWizardProposal_createannotation_inpackage_description,
                    new String[] {typeLabel, containerLabel}));
          }
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown type kind"); // $NON-NLS-1$
    }
    fShowDialog = true;
  }

  private static String getTypeName(int typeKind, Name node) {
    String name = ASTNodes.getSimpleNameIdentifier(node);

    if (typeKind == K_CLASS || typeKind == K_INTERFACE) {
      ASTNode parent = node.getParent();
      if (parent.getLocationInParent() == ParameterizedType.TYPE_PROPERTY) {
        String typeArgBaseName =
            name.startsWith(String.valueOf('T'))
                ? String.valueOf('S')
                : String.valueOf('T'); // use 'S' or 'T'

        int nTypeArgs = ((ParameterizedType) parent.getParent()).typeArguments().size();
        StringBuffer buf = new StringBuffer(name);
        buf.append('<');
        if (nTypeArgs == 1) {
          buf.append(typeArgBaseName);
        } else {
          for (int i = 0; i < nTypeArgs; i++) {
            if (i != 0) buf.append(", "); // $NON-NLS-1$
            buf.append(typeArgBaseName).append(i + 1);
          }
        }
        buf.append('>');
        return buf.toString();
      }
    }
    return name;
  }

  @Override
  public void apply(IDocument document) {
    //		StructuredSelection selection= new StructuredSelection(fCompilationUnit);
    //		NewElementWizard wizard= createWizard(selection);
    //		wizard.init(JavaPlugin.getDefault().getWorkbench(), selection);
    //
    //		IType createdType= null;
    //
    //		if (fShowDialog) {
    //			Shell shell= JavaPlugin.getActiveWorkbenchShell();
    //			WizardDialog dialog= new WizardDialog(shell, wizard);
    //
    //			PixelConverter converter= new PixelConverter(JFaceResources.getDialogFont());
    //			dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(70),
    // converter.convertHeightInCharsToPixels(20));
    //			dialog.create();
    //
    //	dialog.getShell().setText(CorrectionMessages.NewCUCompletionUsingWizardProposal_dialogtitle);
    //
    //			if (dialog.open() == Window.OK) {
    //				createdType= (IType) wizard.getCreatedElement();
    //			}
    //		} else {
    //			wizard.addPages();
    //			try {
    //				NewTypeWizardPage page= getPage(wizard);
    //				page.createType(null);
    //				createdType= page.getCreatedType();
    //			} catch (CoreException e) {
    //				JavaPlugin.log(e);
    //			} catch (InterruptedException e) {
    //			}
    //		}
    //
    //		if (createdType != null) {
    //			IJavaElement container= createdType.getParent();
    //			if (container instanceof ICompilationUnit) {
    //				container= container.getParent();
    //			}
    //			if (!container.equals(fTypeContainer)) {
    //				// add import
    //				try {
    //					ImportRewrite rewrite= StubUtility.createImportRewrite(fCompilationUnit, true);
    //					rewrite.addImport(createdType.getFullyQualifiedName('.'));
    //					JavaModelUtil.applyEdit(fCompilationUnit, rewrite.rewriteImports(null), false, null);
    //				} catch (CoreException e) {
    //				}
    //			}
    //			fCreatedType= createdType;
    //		}
    throw new UnsupportedOperationException("apply");
  }

  //	private NewTypeWizardPage getPage(NewElementWizard wizard) {
  //		IWizardPage[] pages= wizard.getPages();
  //		Assert.isTrue(pages.length > 0 && pages[0] instanceof NewTypeWizardPage);
  //		return (NewTypeWizardPage) pages[0];
  //	}
  //
  //	private NewElementWizard createWizard(StructuredSelection selection) {
  //		switch (fTypeKind) {
  //			case K_CLASS: {
  //				NewClassWizardPage page=  new NewClassWizardPage();
  //				page.init(selection);
  //				configureWizardPage(page);
  //				return new NewClassCreationWizard(page, true);
  //			}
  //			case K_INTERFACE: {
  //				NewInterfaceWizardPage page=  new NewInterfaceWizardPage();
  //				page.init(selection);
  //				configureWizardPage(page);
  //				return new NewInterfaceCreationWizard(page, true);
  //			}
  //			case K_ENUM: {
  //				NewEnumWizardPage page=  new NewEnumWizardPage();
  //				page.init(selection);
  //				configureWizardPage(page);
  //				return new NewEnumCreationWizard(page, true);
  //			}
  //			case K_ANNOTATION: {
  //				NewAnnotationWizardPage page=  new NewAnnotationWizardPage();
  //				page.init(selection);
  //				configureWizardPage(page);
  //				return new NewAnnotationCreationWizard(page, true);
  //			}
  //		}
  //		throw new IllegalArgumentException();
  //	}
  //
  //	private void configureWizardPage(NewTypeWizardPage page) {
  //		fillInWizardPageName(page);
  //		fillInWizardPageSuperTypes(page);
  //	}
  //
  //	/**
  //	 * Fill-in the "Package" and "Name" fields.
  //	 * @param page the wizard page.
  //	 */
  //	private void fillInWizardPageName(NewTypeWizardPage page) {
  //		// allow to edit when there are type parameters
  //		page.setTypeName(fTypeNameWithParameters, fTypeNameWithParameters.indexOf('<') != -1);
  //
  //		boolean isInEnclosingType= fTypeContainer instanceof IType;
  //		if (isInEnclosingType) {
  //			page.setEnclosingType((IType) fTypeContainer, true);
  //		} else {
  //			page.setPackageFragment((IPackageFragment) fTypeContainer, true);
  //		}
  //		page.setEnclosingTypeSelection(isInEnclosingType, true);
  //	}
  //
  //	/**
  //	 * Fill-in the "Super Class" and "Super Interfaces" fields.
  //	 * @param page the wizard page.
  //	 */
  //	private void fillInWizardPageSuperTypes(NewTypeWizardPage page) {
  //		ITypeBinding type= getPossibleSuperTypeBinding(fNode);
  //		type= Bindings.normalizeTypeBinding(type);
  //		if (type != null) {
  //			if (type.isArray()) {
  //				type= type.getElementType();
  //			}
  //			if (type.isTopLevel() || type.isMember()) {
  //				if (type.isClass() && (fTypeKind == K_CLASS)) {
  //					page.setSuperClass(type.getQualifiedName(), true);
  //				} else if (type.isInterface()) {
  //					List<String> superInterfaces= new ArrayList<String>();
  //					superInterfaces.add(type.getQualifiedName());
  //					page.setSuperInterfaces(superInterfaces, true);
  //				}
  //			}
  //		}
  //	}

  private ITypeBinding getPossibleSuperTypeBinding(ASTNode node) {
    if (fTypeKind == K_ANNOTATION) {
      return null;
    }

    AST ast = node.getAST();
    node = ASTNodes.getNormalizedNode(node);
    ASTNode parent = node.getParent();
    switch (parent.getNodeType()) {
      case ASTNode.METHOD_DECLARATION:
        if (node.getLocationInParent() == MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY) {
          return ast.resolveWellKnownType("java.lang.Exception"); // $NON-NLS-1$
        }
        break;
      case ASTNode.THROW_STATEMENT:
        return ast.resolveWellKnownType("java.lang.Exception"); // $NON-NLS-1$
      case ASTNode.SINGLE_VARIABLE_DECLARATION:
        if (parent.getLocationInParent() == CatchClause.EXCEPTION_PROPERTY) {
          return ast.resolveWellKnownType("java.lang.Exception"); // $NON-NLS-1$
        }
        break;
      case ASTNode.VARIABLE_DECLARATION_STATEMENT:
      case ASTNode.FIELD_DECLARATION:
        return null; // no guessing for LHS types, cannot be a supertype of a known type
      case ASTNode.PARAMETERIZED_TYPE:
        return null; // Inheritance doesn't help: A<X> z= new A<String>(); ->
    }
    ITypeBinding binding = ASTResolving.guessBindingForTypeReference(node);
    if (binding != null && !binding.isRecovered()) {
      return binding;
    }
    return null;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension5#getAdditionalProposalInfo(org.eclipse.core.runtime.IProgressMonitor)
   * @since 3.5
   */
  @Override
  public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
    StringBuffer buf = new StringBuffer();
    switch (fTypeKind) {
      case K_CLASS:
        buf.append(CorrectionMessages.NewCUCompletionUsingWizardProposal_createclass_info);
        break;
      case K_INTERFACE:
        buf.append(CorrectionMessages.NewCUCompletionUsingWizardProposal_createinterface_info);
        break;
      case K_ENUM:
        buf.append(CorrectionMessages.NewCUCompletionUsingWizardProposal_createenum_info);
        break;
      case K_ANNOTATION:
        buf.append(CorrectionMessages.NewCUCompletionUsingWizardProposal_createannotation_info);
        break;
    }
    buf.append("<br>"); // $NON-NLS-1$
    buf.append("<br>"); // $NON-NLS-1$
    if (fTypeContainer instanceof IType) {
      buf.append(CorrectionMessages.NewCUCompletionUsingWizardProposal_tooltip_enclosingtype);
    } else {
      buf.append(CorrectionMessages.NewCUCompletionUsingWizardProposal_tooltip_package);
    }
    buf.append(" <b>"); // $NON-NLS-1$
    buf.append(
        JavaElementLabels.getElementLabel(fTypeContainer, JavaElementLabels.T_FULLY_QUALIFIED));
    buf.append("</b><br>"); // $NON-NLS-1$
    buf.append("public "); // $NON-NLS-1$

    switch (fTypeKind) {
      case K_CLASS:
        buf.append("class <b>"); // $NON-NLS-1$
        break;
      case K_INTERFACE:
        buf.append("interface <b>"); // $NON-NLS-1$
        break;
      case K_ENUM:
        buf.append("enum <b>"); // $NON-NLS-1$
        break;
      case K_ANNOTATION:
        buf.append("@interface <b>"); // $NON-NLS-1$
        break;
    }
    nameToHTML(fTypeNameWithParameters, buf);

    ITypeBinding superclass = getPossibleSuperTypeBinding(fNode);
    if (superclass != null) {
      if (superclass.isClass()) {
        if (fTypeKind == K_CLASS) {
          buf.append("</b> extends <b>"); // $NON-NLS-1$
          nameToHTML(
              BindingLabelProvider.getBindingLabel(
                  superclass, BindingLabelProvider.DEFAULT_TEXTFLAGS),
              buf);
        }
      } else {
        if (fTypeKind == K_INTERFACE) {
          buf.append("</b> extends <b>"); // $NON-NLS-1$
        } else {
          buf.append("</b> implements <b>"); // $NON-NLS-1$
        }
        nameToHTML(
            BindingLabelProvider.getBindingLabel(
                superclass, BindingLabelProvider.DEFAULT_TEXTFLAGS),
            buf);
      }
    }
    buf.append("</b> {<br>}<br>"); // $NON-NLS-1$
    return buf.toString();
  }

  private void nameToHTML(String name, StringBuffer buf) {
    for (int i = 0; i < name.length(); i++) {
      char ch = name.charAt(i);
      if (ch == '>') {
        buf.append("&gt;"); // $NON-NLS-1$
      } else if (ch == '<') {
        buf.append("&lt;"); // $NON-NLS-1$
      } else {
        buf.append(ch);
      }
    }
  }

  /**
   * Returns the showDialog.
   *
   * @return boolean
   */
  public boolean isShowDialog() {
    return fShowDialog;
  }

  /**
   * Sets the showDialog.
   *
   * @param showDialog The showDialog to set
   */
  public void setShowDialog(boolean showDialog) {
    fShowDialog = showDialog;
  }

  public IType getCreatedType() {
    return fCreatedType;
  }

  public int getTypeKind() {
    return fTypeKind;
  }
}
