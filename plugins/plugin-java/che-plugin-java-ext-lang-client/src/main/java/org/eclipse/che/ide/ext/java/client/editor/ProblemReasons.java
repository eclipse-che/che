/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Jesper S Moller - Contributions
 * for bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference
 * expression Stephan Herrmann - Contribution for bug 404649 - [1.8][compiler] detect illegal
 * reference to indirect or redundant super Bug 400874 - [1.8][compiler] Inference infrastructure
 * should evolve to meet JLS8 18.x (Part G of JSR335 spec) Bug 416182 - [1.8][compiler][null]
 * Contradictory null annotations not rejected
 * *****************************************************************************
 */
package org.eclipse.che.ide.ext.java.client.editor;

public interface ProblemReasons {
  final int NoError = 0;
  final int NotFound = 1;
  final int NotVisible = 2;
  final int Ambiguous = 3;
  final int InternalNameProvided = 4; // used if an internal name is used in source
  final int InheritedNameHidesEnclosingName = 5;
  final int NonStaticReferenceInConstructorInvocation = 6;
  final int NonStaticReferenceInStaticContext = 7;
  final int ReceiverTypeNotVisible = 8;
  final int IllegalSuperTypeVariable = 9;
  final int ParameterBoundMismatch = 10; // for generic method
  final int TypeParameterArityMismatch = 11; // for generic method
  final int ParameterizedMethodTypeMismatch = 12; // for generic method
  final int TypeArgumentsForRawGenericMethod = 13; // for generic method
  final int InvalidTypeForStaticImport = 14;
  final int InvalidTypeForAutoManagedResource = 15;
  final int VarargsElementTypeNotVisible = 16;
  final int NoSuchSingleAbstractMethod = 17;
  final int NotAWellFormedParameterizedType = 18;
  final int IntersectionHasMultipleFunctionalInterfaces = 19;
  final int NonStaticOrAlienTypeReceiver = 20;
  final int AttemptToBypassDirectSuper = 21; // super access within default method
  final int DefectiveContainerAnnotationType = 22;
  final int ParameterizedMethodExpectedTypeProblem = 23;
  final int ApplicableMethodOverriddenByInapplicable = 24;
  final int ContradictoryNullAnnotations = 25;
}
