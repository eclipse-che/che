/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.ui.refactoring.contentassist;


import org.eclipse.che.jdt.util.JavaModelUtil;

public class JavaTypeCompletionProcessor/* extends CUPositionCompletionProcessor*/ {

	public static final String DUMMY_CLASS_NAME= "$$__$$"; //$NON-NLS-1$

	/**
	 * The CU name to be used if no parent ICompilationUnit is available.
	 * The main type of this class will be filtered out from the proposals list.
	 */
	public static final String DUMMY_CU_NAME= DUMMY_CLASS_NAME + JavaModelUtil.DEFAULT_CU_SUFFIX;

//	/**
//	 * Creates a <code>JavaTypeCompletionProcessor</code>.
//	 * The completion context must be set via {@link #setPackageFragment(IPackageFragment)}.
//	 *
//	 * @param enableBaseTypes complete java base types iff <code>true</code>
//	 * @param enableVoid complete <code>void</code> base type iff <code>true</code>
//	 */
//	public JavaTypeCompletionProcessor(boolean enableBaseTypes, boolean enableVoid) {
//		this(enableBaseTypes, enableVoid, false);
//	}
//
//	/**
//	 * Creates a <code>JavaTypeCompletionProcessor</code>.
//	 * The completion context must be set via {@link #setPackageFragment(IPackageFragment)}.
//	 *
//	 * @param enableBaseTypes complete java base types iff <code>true</code>
//	 * @param enableVoid complete <code>void</code> base type iff <code>true</code>
//	 * @param fullyQualify always complete to fully qualifies type iff <code>true</code>
//	 */
//	public JavaTypeCompletionProcessor(boolean enableBaseTypes, boolean enableVoid, boolean fullyQualify) {
//		super(new TypeCompletionRequestor(enableBaseTypes, enableVoid, fullyQualify));
//	}
//
//	@Override
//	public char[] getCompletionProposalAutoActivationCharacters() {
//		// disable auto activation in dialog fields, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=89476
//		return null;
//	}
//
//	/**
//	 * @param packageFragment the new completion context
//	 */
//	public void setPackageFragment(IPackageFragment packageFragment) {
//		//TODO: Some callers have a better completion context and should include imports
//		// and nested classes of their declaring CU in WC's source.
//		if (packageFragment == null) {
//			setCompletionContext(null, null, null);
//		} else {
//			String before= "public class " + DUMMY_CLASS_NAME + " { ";  //$NON-NLS-1$//$NON-NLS-2$
//			String after= " }"; //$NON-NLS-1$
//			setCompletionContext(packageFragment.getCompilationUnit(DUMMY_CU_NAME), before, after);
//		}
//	}
//
//	public void setExtendsCompletionContext(IJavaElement javaElement) {
//		if (javaElement instanceof IPackageFragment) {
//			IPackageFragment packageFragment= (IPackageFragment) javaElement;
//			ICompilationUnit cu= packageFragment.getCompilationUnit(DUMMY_CU_NAME);
//			setCompletionContext(cu, "public class " + DUMMY_CLASS_NAME + " extends ", " {}"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
//		} else if (javaElement instanceof IType) {
//			// pattern: public class OuterType { public class Type extends /*caret*/  {} }
//			IType type= (IType) javaElement;
//			String before= "public class " + type.getElementName() + " extends "; //$NON-NLS-1$ //$NON-NLS-2$
//			String after= " {}"; //$NON-NLS-1$
//			IJavaElement parent= type.getParent();
//			while (parent instanceof IType) {
//				type= (IType) parent;
//				before+= "public class " + type.getElementName() + " {"; //$NON-NLS-1$ //$NON-NLS-2$
//				after+= "}"; //$NON-NLS-1$
//				parent= type.getParent();
//			}
//			ICompilationUnit cu= type.getCompilationUnit();
//			setCompletionContext(cu, before, after);
//		} else {
//			setCompletionContext(null, null, null);
//		}
//	}
//
////	public void setImplementsCompletionContext(IPackageFragment packageFragment) {
////		ICompilationUnit cu= packageFragment.getCompilationUnit(DUMMY_CU_NAME);
////		setCompletionContext(cu, "public class " + DUMMY_CLASS_NAME + " implements ", " {}"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
////	}
//
//	protected static class TypeCompletionRequestor extends CUPositionCompletionRequestor {
//		private static final String VOID= "void"; //$NON-NLS-1$
//		private static final List<String> BASE_TYPES= Arrays.asList(
//			new String[] {"boolean", "byte", "char", "double", "float", "int", "long", "short"});  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
//
//		private boolean fEnableBaseTypes;
//		private boolean fEnableVoid;
//		private final boolean fFullyQualify;
//
//		public TypeCompletionRequestor(boolean enableBaseTypes, boolean enableVoid, boolean fullyQualify) {
//			fFullyQualify= fullyQualify;
//			fEnableBaseTypes= enableBaseTypes;
//			fEnableVoid= enableVoid;
//			setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, true);
//			setIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, true);
//			setIgnored(CompletionProposal.FIELD_REF, true);
//			setIgnored(CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER, true);
//			setIgnored(CompletionProposal.LABEL_REF, true);
//			setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, true);
//			setIgnored(CompletionProposal.METHOD_DECLARATION, true);
//			setIgnored(CompletionProposal.METHOD_REF, true);
//			setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION, true);
//			setIgnored(CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER, true);
//			setIgnored(CompletionProposal.VARIABLE_DECLARATION, true);
//			setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION, true);
//			setIgnored(CompletionProposal.METHOD_NAME_REFERENCE, true);
//		}
//
//		@Override
//		public void accept(CompletionProposal proposal) {
//			switch (proposal.getKind()) {
//				case CompletionProposal.PACKAGE_REF :
//					char[] packageName= proposal.getDeclarationSignature();
//					if (TypeFilter.isFiltered(packageName))
//						return;
//					addAdjustedCompletion(
//							new String(packageName),
//							new String(proposal.getCompletion()),
//							proposal.getReplaceStart(),
//							proposal.getReplaceEnd(),
//							proposal.getRelevance(),
//							JavaPluginImages.DESC_OBJS_PACKAGE);
//					return;
//
//				case CompletionProposal.TYPE_REF :
//					char[] signature= proposal.getSignature();
//					char[] fullName= Signature.toCharArray(signature);
//					if (TypeFilter.isFiltered(fullName))
//						return;
//					StringBuffer buf= new StringBuffer();
//					buf.append(Signature.getSimpleName(fullName));
//					if (buf.length() == 0)
//						return; // this is the dummy class, whose $ have been converted to dots
//					char[] typeQualifier= Signature.getQualifier(fullName);
//					if (typeQualifier.length > 0) {
//						buf.append(JavaElementLabels.CONCAT_STRING);
//						buf.append(typeQualifier);
//					}
//					String name= buf.toString();
//
//					// Only fully qualify if it's a top level type:
//					boolean fullyQualify= fFullyQualify && CharOperation.equals(proposal.getDeclarationSignature(), typeQualifier);
//
//					ImageDescriptor typeImageDescriptor;
//					switch (Signature.getTypeSignatureKind(signature)) {
//						case Signature.TYPE_VARIABLE_SIGNATURE :
//							typeImageDescriptor= JavaPluginImages.DESC_OBJS_TYPEVARIABLE;
//							break;
//						case Signature.CLASS_TYPE_SIGNATURE :
//							typeImageDescriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, proposal.getFlags(), false);
//							break;
//						default :
//							typeImageDescriptor= null;
//					}
//
//					addAdjustedTypeCompletion(
//							name,
//							new String(proposal.getCompletion()),
//							proposal.getReplaceStart(),
//							proposal.getReplaceEnd(),
//							proposal.getRelevance(),
//							typeImageDescriptor,
//							fullyQualify ? new String(fullName) : null);
//					return;
//
//				case CompletionProposal.KEYWORD:
//					if (! fEnableBaseTypes)
//						return;
//					String keyword= new String(proposal.getName());
//					if ( (fEnableVoid && VOID.equals(keyword)) || (fEnableBaseTypes && BASE_TYPES.contains(keyword)) )
//						addAdjustedCompletion(
//								keyword,
//								new String(proposal.getCompletion()),
//								proposal.getReplaceStart(),
//								proposal.getReplaceEnd(),
//								proposal.getRelevance(),
//								null);
//					return;
//
//				default :
//					return;
//			}
//
//		}
//	}
}
