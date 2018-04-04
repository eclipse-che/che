/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation IBM Corporation - added the
 * following constants NonStaticAccessToStaticField NonStaticAccessToStaticMethod Task
 * ExpressionShouldBeAVariable AssignmentHasNoEffect IBM Corporation - added the following constants
 * TooManySyntheticArgumentSlots TooManyArrayDimensions TooManyBytesForStringConstant TooManyMethods
 * TooManyFields NonBlankFinalLocalAssignment ObjectCannotHaveSuperTypes MissingSemiColon
 * InvalidParenthesizedExpression EnclosingInstanceInConstructorCall
 * BytecodeExceeds64KLimitForConstructor IncompatibleReturnTypeForNonInheritedInterfaceMethod
 * UnusedPrivateMethod UnusedPrivateConstructor UnusedPrivateType UnusedPrivateField
 * IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod InvalidExplicitConstructorCall
 * IBM Corporation - added the following constants PossibleAccidentalBooleanAssignment
 * SuperfluousSemicolon IndirectAccessToStaticField IndirectAccessToStaticMethod
 * IndirectAccessToStaticType BooleanMethodThrowingException UnnecessaryCast UnnecessaryArgumentCast
 * UnnecessaryInstanceof FinallyMustCompleteNormally UnusedMethodDeclaredThrownException
 * UnusedConstructorDeclaredThrownException InvalidCatchBlockSequence UnqualifiedFieldAccess IBM
 * Corporation - added the following constants Javadoc JavadocUnexpectedTag JavadocMissingParamTag
 * JavadocMissingParamName JavadocDuplicateParamName JavadocInvalidParamName JavadocMissingReturnTag
 * JavadocDuplicateReturnTag JavadocMissingThrowsTag JavadocMissingThrowsClassName
 * JavadocInvalidThrowsClass JavadocDuplicateThrowsClassName JavadocInvalidThrowsClassName
 * JavadocMissingSeeReference JavadocInvalidSeeReference JavadocInvalidSeeHref JavadocInvalidSeeArgs
 * JavadocMissing JavadocInvalidTag JavadocMessagePrefix EmptyControlFlowStatement IBM Corporation -
 * added the following constants IllegalUsageOfQualifiedTypeReference InvalidDigit IBM Corporation -
 * added the following constants ParameterAssignment FallthroughCase IBM Corporation - added the
 * following constants UnusedLabel UnnecessaryNLSTag LocalVariableMayBeNull
 * EnumConstantsCannotBeSurroundedByParenthesis JavadocMissingIdentifier
 * JavadocNonStaticTypeFromStaticInvocation RawTypeReference NoAdditionalBoundAfterTypeVariable
 * UnsafeGenericArrayForVarargs IllegalAccessFromTypeVariable AnnotationValueMustBeArrayInitializer
 * InvalidEncoding CannotReadSource EnumStaticFieldInInInitializerContext ExternalProblemNotFixable
 * ExternalProblemFixable IBM Corporation - added the following constants
 * AnnotationValueMustBeAnEnumConstant OverridingMethodWithoutSuperInvocation
 * MethodMustOverrideOrImplement TypeHidingTypeParameterFromType TypeHidingTypeParameterFromMethod
 * TypeHidingType IBM Corporation - added the following constants NullLocalVariableReference
 * PotentialNullLocalVariableReference RedundantNullCheckOnNullLocalVariable
 * NullLocalVariableComparisonYieldsFalse RedundantLocalVariableNullAssignment
 * NullLocalVariableInstanceofYieldsFalse RedundantNullCheckOnNonNullLocalVariable
 * NonNullLocalVariableComparisonYieldsFalse IBM Corporation - added the following constants
 * InvalidUsageOfTypeParametersForAnnotationDeclaration
 * InvalidUsageOfTypeParametersForEnumDeclaration IBM Corporation - added the following constants
 * RedundantSuperinterface Benjamin Muskalla - added the following constants
 * MissingSynchronizedModifierInInheritedMethod Stephan Herrmann - added the following constants
 * UnusedObjectAllocation PotentiallyUnclosedCloseable PotentiallyUnclosedCloseableAtExit
 * UnclosedCloseable UnclosedCloseableAtExit ExplicitlyClosedAutoCloseable
 * RequiredNonNullButProvidedNull RequiredNonNullButProvidedPotentialNull
 * RequiredNonNullButProvidedUnknown NullAnnotationNameMustBeQualified
 * IllegalReturnNullityRedefinition IllegalRedefinitionToNonNullParameter
 * IllegalDefinitionToNonNullParameter ParameterLackingNonNullAnnotation
 * ParameterLackingNullableAnnotation PotentialNullMessageSendReference
 * RedundantNullCheckOnNonNullMessageSend CannotImplementIncompatibleNullness
 * RedundantNullAnnotation RedundantNullDefaultAnnotation RedundantNullDefaultAnnotationPackage
 * RedundantNullDefaultAnnotationType RedundantNullDefaultAnnotationMethod
 * ContradictoryNullAnnotations IllegalAnnotationForBaseType
 * RedundantNullCheckOnSpecdNonNullLocalVariable SpecdNonNullLocalVariableComparisonYieldsFalse
 * RequiredNonNullButProvidedSpecdNullable MissingDefaultCase MissingEnumConstantCaseDespiteDefault
 * UninitializedLocalVariableHintMissingDefault UninitializedBlankFinalFieldHintMissingDefault
 * ShouldReturnValueHintMissingDefault IllegalModifierForInterfaceDefaultMethod
 * InheritedDefaultMethodConflictsWithOtherInherited ConflictingNullAnnotations
 * ConflictingInheritedNullAnnotations UnsafeElementTypeConversion
 * ArrayReferencePotentialNullReference DereferencingNullableExpression
 * NullityMismatchingTypeAnnotation NullityMismatchingTypeAnnotationSuperHint
 * NullityUncheckedTypeAnnotationDetail NullityUncheckedTypeAnnotationDetailSuperHint
 * NullableFieldReference UninitializedNonNullField UninitializedNonNullFieldHintMissingDefault
 * NonNullMessageSendComparisonYieldsFalse RedundantNullCheckOnNonNullSpecdField
 * NonNullSpecdFieldComparisonYieldsFalse NonNullExpressionComparisonYieldsFalse
 * RedundantNullCheckOnNonNullExpression ReferenceExpressionParameterNullityMismatch
 * ReferenceExpressionParameterNullityUnchecked ReferenceExpressionReturnNullRedef
 * ReferenceExpressionReturnNullRedefUnchecked DuplicateInheritedDefaultMethods
 * SuperAccessCannotBypassDirectSuper SuperCallCannotBypassOverride ConflictingNullAnnotations
 * ConflictingInheritedNullAnnotations UnsafeElementTypeConversion PotentialNullUnboxing
 * NullUnboxing NullExpressionReference PotentialNullExpressionReference
 * RedundantNullCheckAgainstNonNullType NullAnnotationUnsupportedLocation
 * NullAnnotationUnsupportedLocationAtType NullityMismatchTypeArgument
 * ContradictoryNullAnnotationsOnBound UnsafeNullnessCast ContradictoryNullAnnotationsInferred
 * NonNullDefaultDetailIsNotEvaluated NullNotCompatibleToFreeTypeVariable
 * NullityMismatchAgainstFreeTypeVariable Jesper S Moller - added the following constants
 * TargetTypeNotAFunctionalInterface OuterLocalMustBeEffectivelyFinal IllegalModifiersForPackage
 * DuplicateAnnotationNotMarkedRepeatable DisallowedTargetForContainerAnnotation
 * RepeatedAnnotationWithContainerAnnotation ContainingAnnotationMustHaveValue
 * ContainingAnnotationHasNonDefaultMembers ContainingAnnotationHasWrongValueType
 * ContainingAnnotationHasShorterRetention RepeatableAnnotationHasTargets
 * RepeatableAnnotationTargetMismatch RepeatableAnnotationIsDocumented
 * RepeatableAnnotationIsInherited RepeatableAnnotationWithRepeatingContainerAnnotation
 * *****************************************************************************
 */
package org.eclipse.che.ide.ext.java.client.editor;

/**
 * Description of a Java problem, as detected by the compiler or some of the underlying technology
 * reusing the compiler. A problem provides access to:
 *
 * <ul>
 *   <li>its location (originating source file name, source position, line number),
 *   <li>its message description and a predicate to check its severity (warning or error).
 *   <li>its ID : a number identifying the very nature of this problem. All possible IDs are listed
 *       as constants on this interface.
 * </ul>
 *
 * Note: the compiler produces IProblems internally, which are turned into markers by the
 * JavaBuilder so as to persist problem descriptions. This explains why there is no API allowing to
 * reach IProblem detected when compiling. However, the Java problem markers carry equivalent
 * information to IProblem, in particular their ID (attribute "id") is set to one of the IDs defined
 * on this interface.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProblem {

  /**
   * Answer back the original arguments recorded into the problem.
   *
   * @return the original arguments recorded into the problem
   */
  String[] getArguments();

  /**
   * Returns the problem id
   *
   * @return the problem id
   */
  int getID();

  /**
   * Answer a localized, human-readable message string which describes the problem.
   *
   * @return a localized, human-readable message string which describes the problem
   */
  String getMessage();

  /**
   * Answer the file name in which the problem was found.
   *
   * @return the file name in which the problem was found
   */
  char[] getOriginatingFileName();

  /**
   * Answer the end position of the problem (inclusive), or -1 if unknown.
   *
   * @return the end position of the problem (inclusive), or -1 if unknown
   */
  int getSourceEnd();

  /**
   * Answer the line number in source where the problem begins.
   *
   * @return the line number in source where the problem begins
   */
  int getSourceLineNumber();

  /**
   * Answer the start position of the problem (inclusive), or -1 if unknown.
   *
   * @return the start position of the problem (inclusive), or -1 if unknown
   */
  int getSourceStart();

  /**
   * Checks the severity to see if the Error bit is set.
   *
   * @return true if the Error bit is set for the severity, false otherwise
   */
  boolean isError();

  /**
   * Checks the severity to see if the Error bit is not set.
   *
   * @return true if the Error bit is not set for the severity, false otherwise
   */
  boolean isWarning();

  /**
   * Set the end position of the problem (inclusive), or -1 if unknown. Used for shifting problem
   * positions.
   *
   * @param sourceEnd the given end position
   */
  void setSourceEnd(int sourceEnd);

  /**
   * Set the line number in source where the problem begins.
   *
   * @param lineNumber the given line number
   */
  void setSourceLineNumber(int lineNumber);

  /**
   * Set the start position of the problem (inclusive), or -1 if unknown. Used for shifting problem
   * positions.
   *
   * @param sourceStart the given start position
   */
  void setSourceStart(int sourceStart);

  /**
   * Problem Categories The high bits of a problem ID contains information about the category of a
   * problem. For example, (problemID & TypeRelated) != 0, indicates that this problem is type
   * related.
   *
   * <p>A problem category can help to implement custom problem filters. Indeed, when numerous
   * problems are listed, focusing on import related problems first might be relevant.
   *
   * <p>When a problem is tagged as Internal, it means that no change other than a local source code
   * change can fix the corresponding problem. A type related problem could be addressed by changing
   * the type involved in it.
   */
  int TypeRelated = 0x01000000;

  int FieldRelated = 0x02000000;
  int MethodRelated = 0x04000000;
  int ConstructorRelated = 0x08000000;
  int ImportRelated = 0x10000000;
  int Internal = 0x20000000;
  int Syntax = 0x40000000;
  /** @since 3.0 */
  int Javadoc = 0x80000000;

  /** Mask to use in order to filter out the category portion of the problem ID. */
  int IgnoreCategoriesMask = 0xFFFFFF;

  /**
   * Below are listed all available problem IDs. Note that this list could be augmented in the
   * future, as new features are added to the Java core implementation.
   */

  /**
   * ID reserved for referencing an internal error inside the JavaCore implementation which may be
   * surfaced as a problem associated with the compilation unit which caused it to occur.
   */
  int Unclassified = 0;

  /** General type related problems */
  int ObjectHasNoSuperclass = TypeRelated + 1;

  int UndefinedType = TypeRelated + 2;
  int NotVisibleType = TypeRelated + 3;
  int AmbiguousType = TypeRelated + 4;
  int UsingDeprecatedType = TypeRelated + 5;
  int InternalTypeNameProvided = TypeRelated + 6;
  /** @since 2.1 */
  int UnusedPrivateType = Internal + TypeRelated + 7;

  int IncompatibleTypesInEqualityOperator = TypeRelated + 15;
  int IncompatibleTypesInConditionalOperator = TypeRelated + 16;
  int TypeMismatch = TypeRelated + 17;
  /** @since 3.0 */
  int IndirectAccessToStaticType = Internal + TypeRelated + 18;

  /** @since 3.10 */
  int ReturnTypeMismatch = TypeRelated + 19;

  /** Inner types related problems */
  int MissingEnclosingInstanceForConstructorCall = TypeRelated + 20;

  int MissingEnclosingInstance = TypeRelated + 21;
  int IncorrectEnclosingInstanceReference = TypeRelated + 22;
  int IllegalEnclosingInstanceSpecification = TypeRelated + 23;
  int CannotDefineStaticInitializerInLocalType = Internal + 24;
  int OuterLocalMustBeFinal = Internal + 25;
  int CannotDefineInterfaceInLocalType = Internal + 26;
  int IllegalPrimitiveOrArrayTypeForEnclosingInstance = TypeRelated + 27;
  /** @since 2.1 */
  int EnclosingInstanceInConstructorCall = Internal + 28;

  int AnonymousClassCannotExtendFinalClass = TypeRelated + 29;
  /** @since 3.1 */
  int CannotDefineAnnotationInLocalType = Internal + 30;
  /** @since 3.1 */
  int CannotDefineEnumInLocalType = Internal + 31;
  /** @since 3.1 */
  int NonStaticContextForEnumMemberType = Internal + 32;
  /** @since 3.3 */
  int TypeHidingType = TypeRelated + 33;

  // variables
  int UndefinedName = Internal + FieldRelated + 50;
  int UninitializedLocalVariable = Internal + 51;
  int VariableTypeCannotBeVoid = Internal + 52;
  /** @deprecated - problem is no longer generated, use {@link #CannotAllocateVoidArray} instead */
  int VariableTypeCannotBeVoidArray = Internal + 53;

  int CannotAllocateVoidArray = Internal + 54;
  // local variables
  int RedefinedLocal = Internal + 55;
  int RedefinedArgument = Internal + 56;
  // final local variables
  int DuplicateFinalLocalInitialization = Internal + 57;
  /** @since 2.1 */
  int NonBlankFinalLocalAssignment = Internal + 58;
  /** @since 3.2 */
  int ParameterAssignment = Internal + 59;

  int FinalOuterLocalAssignment = Internal + 60;
  int LocalVariableIsNeverUsed = Internal + 61;
  int ArgumentIsNeverUsed = Internal + 62;
  int BytecodeExceeds64KLimit = Internal + 63;
  int BytecodeExceeds64KLimitForClinit = Internal + 64;
  int TooManyArgumentSlots = Internal + 65;
  int TooManyLocalVariableSlots = Internal + 66;
  /** @since 2.1 */
  int TooManySyntheticArgumentSlots = Internal + 67;
  /** @since 2.1 */
  int TooManyArrayDimensions = Internal + 68;
  /** @since 2.1 */
  int BytecodeExceeds64KLimitForConstructor = Internal + 69;

  // fields
  int UndefinedField = FieldRelated + 70;
  int NotVisibleField = FieldRelated + 71;
  int AmbiguousField = FieldRelated + 72;
  int UsingDeprecatedField = FieldRelated + 73;
  int NonStaticFieldFromStaticInvocation = FieldRelated + 74;
  int ReferenceToForwardField = FieldRelated + Internal + 75;
  /** @since 2.1 */
  int NonStaticAccessToStaticField = Internal + FieldRelated + 76;
  /** @since 2.1 */
  int UnusedPrivateField = Internal + FieldRelated + 77;
  /** @since 3.0 */
  int IndirectAccessToStaticField = Internal + FieldRelated + 78;
  /** @since 3.0 */
  int UnqualifiedFieldAccess = Internal + FieldRelated + 79;

  int FinalFieldAssignment = FieldRelated + 80;
  int UninitializedBlankFinalField = FieldRelated + 81;
  int DuplicateBlankFinalFieldInitialization = FieldRelated + 82;
  /** @since 3.6 */
  int UnresolvedVariable = FieldRelated + 83;
  /** @since 3.10 */
  int NonStaticOrAlienTypeReceiver = MethodRelated + 84;
  // variable hiding
  /** @since 3.0 */
  int LocalVariableHidingLocalVariable = Internal + 90;
  /** @since 3.0 */
  int LocalVariableHidingField = Internal + FieldRelated + 91;
  /** @since 3.0 */
  int FieldHidingLocalVariable = Internal + FieldRelated + 92;
  /** @since 3.0 */
  int FieldHidingField = Internal + FieldRelated + 93;
  /** @since 3.0 */
  int ArgumentHidingLocalVariable = Internal + 94;
  /** @since 3.0 */
  int ArgumentHidingField = Internal + 95;
  /** @since 3.1 */
  int MissingSerialVersion = Internal + 96;
  /** @since 3.10 */
  int LambdaRedeclaresArgument = Internal + 97;
  /** @since 3.10 */
  int LambdaRedeclaresLocal = Internal + 98;
  /** @since 3.10 */
  int LambdaDescriptorMentionsUnmentionable = 99;

  // methods
  int UndefinedMethod = MethodRelated + 100;
  int NotVisibleMethod = MethodRelated + 101;
  int AmbiguousMethod = MethodRelated + 102;
  int UsingDeprecatedMethod = MethodRelated + 103;
  int DirectInvocationOfAbstractMethod = MethodRelated + 104;
  int VoidMethodReturnsValue = MethodRelated + 105;
  int MethodReturnsVoid = MethodRelated + 106;
  int MethodRequiresBody = Internal + MethodRelated + 107;
  int ShouldReturnValue = Internal + MethodRelated + 108;
  int MethodButWithConstructorName = MethodRelated + 110;
  int MissingReturnType = TypeRelated + 111;
  int BodyForNativeMethod = Internal + MethodRelated + 112;
  int BodyForAbstractMethod = Internal + MethodRelated + 113;
  int NoMessageSendOnBaseType = MethodRelated + 114;
  int ParameterMismatch = MethodRelated + 115;
  int NoMessageSendOnArrayType = MethodRelated + 116;
  /** @since 2.1 */
  int NonStaticAccessToStaticMethod = Internal + MethodRelated + 117;
  /** @since 2.1 */
  int UnusedPrivateMethod = Internal + MethodRelated + 118;
  /** @since 3.0 */
  int IndirectAccessToStaticMethod = Internal + MethodRelated + 119;
  /** @since 3.4 */
  int MissingTypeInMethod = MethodRelated + 120;
  /** @since 3.7 */
  int MethodCanBeStatic = Internal + MethodRelated + 121;
  /** @since 3.7 */
  int MethodCanBePotentiallyStatic = Internal + MethodRelated + 122;
  /** @since 3.10 */
  int MethodReferenceSwingsBothWays = Internal + MethodRelated + 123;
  /** @since 3.10 */
  int StaticMethodShouldBeAccessedStatically = Internal + MethodRelated + 124;
  /** @since 3.10 */
  int InvalidArrayConstructorReference = Internal + MethodRelated + 125;
  /** @since 3.10 */
  int ConstructedArrayIncompatible = Internal + MethodRelated + 126;
  /** @since 3.10 */
  int DanglingReference = Internal + MethodRelated + 127;
  /** @since 3.10 */
  int IncompatibleMethodReference = Internal + MethodRelated + 128;

  // constructors
  /** @since 3.4 */
  int MissingTypeInConstructor = ConstructorRelated + 129;

  int UndefinedConstructor = ConstructorRelated + 130;
  int NotVisibleConstructor = ConstructorRelated + 131;
  int AmbiguousConstructor = ConstructorRelated + 132;
  int UsingDeprecatedConstructor = ConstructorRelated + 133;
  /** @since 2.1 */
  int UnusedPrivateConstructor = Internal + MethodRelated + 134;
  // explicit constructor calls
  int InstanceFieldDuringConstructorInvocation = ConstructorRelated + 135;
  int InstanceMethodDuringConstructorInvocation = ConstructorRelated + 136;
  int RecursiveConstructorInvocation = ConstructorRelated + 137;
  int ThisSuperDuringConstructorInvocation = ConstructorRelated + 138;
  /** @since 3.0 */
  int InvalidExplicitConstructorCall = ConstructorRelated + Syntax + 139;
  // implicit constructor calls
  int UndefinedConstructorInDefaultConstructor = ConstructorRelated + 140;
  int NotVisibleConstructorInDefaultConstructor = ConstructorRelated + 141;
  int AmbiguousConstructorInDefaultConstructor = ConstructorRelated + 142;
  int UndefinedConstructorInImplicitConstructorCall = ConstructorRelated + 143;
  int NotVisibleConstructorInImplicitConstructorCall = ConstructorRelated + 144;
  int AmbiguousConstructorInImplicitConstructorCall = ConstructorRelated + 145;
  int UnhandledExceptionInDefaultConstructor = TypeRelated + 146;
  int UnhandledExceptionInImplicitConstructorCall = TypeRelated + 147;

  // expressions
  /** @since 3.6 */
  int UnusedObjectAllocation = Internal + 148;
  /** @since 3.5 */
  int DeadCode = Internal + 149;

  int ArrayReferenceRequired = Internal + 150;
  int NoImplicitStringConversionForCharArrayExpression = Internal + 151;
  // constant expressions
  int StringConstantIsExceedingUtf8Limit = Internal + 152;
  int NonConstantExpression = Internal + 153;
  int NumericValueOutOfRange = Internal + 154;
  // cast expressions
  int IllegalCast = TypeRelated + 156;
  // allocations
  int InvalidClassInstantiation = TypeRelated + 157;
  int CannotDefineDimensionExpressionsWithInit = Internal + 158;
  int MustDefineEitherDimensionExpressionsOrInitializer = Internal + 159;
  // operators
  int InvalidOperator = Internal + 160;
  // statements
  int CodeCannotBeReached = Internal + 161;
  int CannotReturnInInitializer = Internal + 162;
  int InitializerMustCompleteNormally = Internal + 163;
  // assert
  int InvalidVoidExpression = Internal + 164;
  // try
  int MaskedCatch = TypeRelated + 165;
  int DuplicateDefaultCase = Internal + 166;
  int UnreachableCatch = TypeRelated + MethodRelated + 167;
  int UnhandledException = TypeRelated + 168;
  // switch
  int IncorrectSwitchType = TypeRelated + 169;
  int DuplicateCase = FieldRelated + 170;

  // labelled
  int DuplicateLabel = Internal + 171;
  int InvalidBreak = Internal + 172;
  int InvalidContinue = Internal + 173;
  int UndefinedLabel = Internal + 174;
  // synchronized
  int InvalidTypeToSynchronized = Internal + 175;
  int InvalidNullToSynchronized = Internal + 176;
  // throw
  int CannotThrowNull = Internal + 177;
  // assignment
  /** @since 2.1 */
  int AssignmentHasNoEffect = Internal + 178;
  /** @since 3.0 */
  int PossibleAccidentalBooleanAssignment = Internal + 179;
  /** @since 3.0 */
  int SuperfluousSemicolon = Internal + 180;
  /** @since 3.0 */
  int UnnecessaryCast = Internal + TypeRelated + 181;
  /**
   * @deprecated - no longer generated, use {@link #UnnecessaryCast} instead
   * @since 3.0
   */
  int UnnecessaryArgumentCast = Internal + TypeRelated + 182;
  /** @since 3.0 */
  int UnnecessaryInstanceof = Internal + TypeRelated + 183;
  /** @since 3.0 */
  int FinallyMustCompleteNormally = Internal + 184;
  /** @since 3.0 */
  int UnusedMethodDeclaredThrownException = Internal + 185;
  /** @since 3.0 */
  int UnusedConstructorDeclaredThrownException = Internal + 186;
  /** @since 3.0 */
  int InvalidCatchBlockSequence = Internal + TypeRelated + 187;
  /** @since 3.0 */
  int EmptyControlFlowStatement = Internal + TypeRelated + 188;
  /** @since 3.0 */
  int UnnecessaryElse = Internal + 189;

  // inner emulation
  int NeedToEmulateFieldReadAccess = FieldRelated + 190;
  int NeedToEmulateFieldWriteAccess = FieldRelated + 191;
  int NeedToEmulateMethodAccess = MethodRelated + 192;
  int NeedToEmulateConstructorAccess = MethodRelated + 193;

  /** @since 3.2 */
  int FallthroughCase = Internal + 194;

  // inherited name hides enclosing name (sort of ambiguous)
  int InheritedMethodHidesEnclosingName = MethodRelated + 195;
  int InheritedFieldHidesEnclosingName = FieldRelated + 196;
  int InheritedTypeHidesEnclosingName = TypeRelated + 197;

  /** @since 3.1 */
  int IllegalUsageOfQualifiedTypeReference = Internal + Syntax + 198;

  // miscellaneous
  /** @since 3.2 */
  int UnusedLabel = Internal + 199;

  int ThisInStaticContext = Internal + 200;
  int StaticMethodRequested = Internal + MethodRelated + 201;
  int IllegalDimension = Internal + 202;
  /** @deprecated - problem is no longer generated */
  int InvalidTypeExpression = Internal + 203;

  int ParsingError = Syntax + Internal + 204;
  int ParsingErrorNoSuggestion = Syntax + Internal + 205;
  int InvalidUnaryExpression = Syntax + Internal + 206;

  // syntax errors
  int InterfaceCannotHaveConstructors = Syntax + Internal + 207;
  int ArrayConstantsOnlyInArrayInitializers = Syntax + Internal + 208;
  int ParsingErrorOnKeyword = Syntax + Internal + 209;
  int ParsingErrorOnKeywordNoSuggestion = Syntax + Internal + 210;

  /** @since 3.5 */
  int ComparingIdentical = Internal + 211;

  int UnmatchedBracket = Syntax + Internal + 220;
  int NoFieldOnBaseType = FieldRelated + 221;
  int InvalidExpressionAsStatement = Syntax + Internal + 222;
  /** @since 2.1 */
  int ExpressionShouldBeAVariable = Syntax + Internal + 223;
  /** @since 2.1 */
  int MissingSemiColon = Syntax + Internal + 224;
  /** @since 2.1 */
  int InvalidParenthesizedExpression = Syntax + Internal + 225;

  /** @since 3.10 */
  int NoSuperInInterfaceContext = Syntax + Internal + 226;

  /** @since 3.0 */
  int ParsingErrorInsertTokenBefore = Syntax + Internal + 230;
  /** @since 3.0 */
  int ParsingErrorInsertTokenAfter = Syntax + Internal + 231;
  /** @since 3.0 */
  int ParsingErrorDeleteToken = Syntax + Internal + 232;
  /** @since 3.0 */
  int ParsingErrorDeleteTokens = Syntax + Internal + 233;
  /** @since 3.0 */
  int ParsingErrorMergeTokens = Syntax + Internal + 234;
  /** @since 3.0 */
  int ParsingErrorInvalidToken = Syntax + Internal + 235;
  /** @since 3.0 */
  int ParsingErrorMisplacedConstruct = Syntax + Internal + 236;
  /** @since 3.0 */
  int ParsingErrorReplaceTokens = Syntax + Internal + 237;
  /** @since 3.0 */
  int ParsingErrorNoSuggestionForTokens = Syntax + Internal + 238;
  /** @since 3.0 */
  int ParsingErrorUnexpectedEOF = Syntax + Internal + 239;
  /** @since 3.0 */
  int ParsingErrorInsertToComplete = Syntax + Internal + 240;
  /** @since 3.0 */
  int ParsingErrorInsertToCompleteScope = Syntax + Internal + 241;
  /** @since 3.0 */
  int ParsingErrorInsertToCompletePhrase = Syntax + Internal + 242;

  // scanner errors
  int EndOfSource = Syntax + Internal + 250;
  int InvalidHexa = Syntax + Internal + 251;
  int InvalidOctal = Syntax + Internal + 252;
  int InvalidCharacterConstant = Syntax + Internal + 253;
  int InvalidEscape = Syntax + Internal + 254;
  int InvalidInput = Syntax + Internal + 255;
  int InvalidUnicodeEscape = Syntax + Internal + 256;
  int InvalidFloat = Syntax + Internal + 257;
  int NullSourceString = Syntax + Internal + 258;
  int UnterminatedString = Syntax + Internal + 259;
  int UnterminatedComment = Syntax + Internal + 260;
  int NonExternalizedStringLiteral = Internal + 261;
  /** @since 3.1 */
  int InvalidDigit = Syntax + Internal + 262;
  /** @since 3.1 */
  int InvalidLowSurrogate = Syntax + Internal + 263;
  /** @since 3.1 */
  int InvalidHighSurrogate = Syntax + Internal + 264;
  /** @since 3.2 */
  int UnnecessaryNLSTag = Internal + 265;
  /** @since 3.7.1 */
  int InvalidBinary = Syntax + Internal + 266;
  /** @since 3.7.1 */
  int BinaryLiteralNotBelow17 = Syntax + Internal + 267;
  /** @since 3.7.1 */
  int IllegalUnderscorePosition = Syntax + Internal + 268;
  /** @since 3.7.1 */
  int UnderscoresInLiteralsNotBelow17 = Syntax + Internal + 269;
  /** @since 3.7.1 */
  int IllegalHexaLiteral = Syntax + Internal + 270;

  /** @since 3.10 */
  int MissingTypeInLambda = MethodRelated + 271;

  // type related problems
  /** @since 3.1 */
  int DiscouragedReference = TypeRelated + 280;

  int InterfaceCannotHaveInitializers = TypeRelated + 300;
  int DuplicateModifierForType = TypeRelated + 301;
  int IllegalModifierForClass = TypeRelated + 302;
  int IllegalModifierForInterface = TypeRelated + 303;
  int IllegalModifierForMemberClass = TypeRelated + 304;
  int IllegalModifierForMemberInterface = TypeRelated + 305;
  int IllegalModifierForLocalClass = TypeRelated + 306;
  /** @since 3.1 */
  int ForbiddenReference = TypeRelated + 307;

  int IllegalModifierCombinationFinalAbstractForClass = TypeRelated + 308;
  int IllegalVisibilityModifierForInterfaceMemberType = TypeRelated + 309;
  int IllegalVisibilityModifierCombinationForMemberType = TypeRelated + 310;
  int IllegalStaticModifierForMemberType = TypeRelated + 311;
  int SuperclassMustBeAClass = TypeRelated + 312;
  int ClassExtendFinalClass = TypeRelated + 313;
  int DuplicateSuperInterface = TypeRelated + 314;
  int SuperInterfaceMustBeAnInterface = TypeRelated + 315;
  int HierarchyCircularitySelfReference = TypeRelated + 316;
  int HierarchyCircularity = TypeRelated + 317;
  int HidingEnclosingType = TypeRelated + 318;
  int DuplicateNestedType = TypeRelated + 319;
  int CannotThrowType = TypeRelated + 320;
  int PackageCollidesWithType = TypeRelated + 321;
  int TypeCollidesWithPackage = TypeRelated + 322;
  int DuplicateTypes = TypeRelated + 323;
  int IsClassPathCorrect = TypeRelated + 324;
  int PublicClassMustMatchFileName = TypeRelated + 325;
  /** @deprecated - problem is no longer generated */
  int MustSpecifyPackage = Internal + 326;

  int HierarchyHasProblems = TypeRelated + 327;
  int PackageIsNotExpectedPackage = Internal + 328;
  /** @since 2.1 */
  int ObjectCannotHaveSuperTypes = Internal + 329;
  /** @since 3.1 */
  int ObjectMustBeClass = Internal + 330;
  /** @since 3.4 */
  int RedundantSuperinterface = TypeRelated + 331;
  /** @since 3.5 */
  int ShouldImplementHashcode = TypeRelated + 332;
  /** @since 3.5 */
  int AbstractMethodsInConcreteClass = TypeRelated + 333;

  /** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
  int SuperclassNotFound = TypeRelated + 329 + ProblemReasons.NotFound; // TypeRelated + 330
  /** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
  int SuperclassNotVisible = TypeRelated + 329 + ProblemReasons.NotVisible; // TypeRelated + 331
  /** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
  int SuperclassAmbiguous = TypeRelated + 329 + ProblemReasons.Ambiguous; // TypeRelated + 332
  /** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
  int SuperclassInternalNameProvided =
      TypeRelated + 329 + ProblemReasons.InternalNameProvided; // TypeRelated + 333
  /**
   * @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName}
   *     instead
   */
  int SuperclassInheritedNameHidesEnclosingName =
      TypeRelated + 329 + ProblemReasons.InheritedNameHidesEnclosingName; // TypeRelated + 334

  /** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
  int InterfaceNotFound = TypeRelated + 334 + ProblemReasons.NotFound; // TypeRelated + 335
  /** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
  int InterfaceNotVisible = TypeRelated + 334 + ProblemReasons.NotVisible; // TypeRelated + 336
  /** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
  int InterfaceAmbiguous = TypeRelated + 334 + ProblemReasons.Ambiguous; // TypeRelated + 337
  /** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
  int InterfaceInternalNameProvided =
      TypeRelated + 334 + ProblemReasons.InternalNameProvided; // TypeRelated + 338
  /**
   * @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName}
   *     instead
   */
  int InterfaceInheritedNameHidesEnclosingName =
      TypeRelated + 334 + ProblemReasons.InheritedNameHidesEnclosingName; // TypeRelated + 339

  // field related problems
  int DuplicateField = FieldRelated + 340;
  int DuplicateModifierForField = FieldRelated + 341;
  int IllegalModifierForField = FieldRelated + 342;
  int IllegalModifierForInterfaceField = FieldRelated + 343;
  int IllegalVisibilityModifierCombinationForField = FieldRelated + 344;
  int IllegalModifierCombinationFinalVolatileForField = FieldRelated + 345;
  int UnexpectedStaticModifierForField = FieldRelated + 346;

  /** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
  int FieldTypeNotFound = FieldRelated + 349 + ProblemReasons.NotFound; // FieldRelated + 350
  /** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
  int FieldTypeNotVisible = FieldRelated + 349 + ProblemReasons.NotVisible; // FieldRelated + 351
  /** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
  int FieldTypeAmbiguous = FieldRelated + 349 + ProblemReasons.Ambiguous; // FieldRelated + 352
  /** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
  int FieldTypeInternalNameProvided =
      FieldRelated + 349 + ProblemReasons.InternalNameProvided; // FieldRelated + 353
  /**
   * @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName}
   *     instead
   */
  int FieldTypeInheritedNameHidesEnclosingName =
      FieldRelated + 349 + ProblemReasons.InheritedNameHidesEnclosingName; // FieldRelated + 354

  // method related problems
  int DuplicateMethod = MethodRelated + 355;
  int IllegalModifierForArgument = MethodRelated + 356;
  int DuplicateModifierForMethod = MethodRelated + 357;
  int IllegalModifierForMethod = MethodRelated + 358;
  int IllegalModifierForInterfaceMethod = MethodRelated + 359;
  int IllegalVisibilityModifierCombinationForMethod = MethodRelated + 360;
  int UnexpectedStaticModifierForMethod = MethodRelated + 361;
  int IllegalAbstractModifierCombinationForMethod = MethodRelated + 362;
  int AbstractMethodInAbstractClass = MethodRelated + 363;
  int ArgumentTypeCannotBeVoid = MethodRelated + 364;
  /** @deprecated - problem is no longer generated, use {@link #CannotAllocateVoidArray} instead */
  int ArgumentTypeCannotBeVoidArray = MethodRelated + 365;
  /** @deprecated - problem is no longer generated, use {@link #CannotAllocateVoidArray} instead */
  int ReturnTypeCannotBeVoidArray = MethodRelated + 366;

  int NativeMethodsCannotBeStrictfp = MethodRelated + 367;
  int DuplicateModifierForArgument = MethodRelated + 368;
  /** @since 3.5 */
  int IllegalModifierForConstructor = MethodRelated + 369;

  /** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
  int ArgumentTypeNotFound = MethodRelated + 369 + ProblemReasons.NotFound; // MethodRelated + 370
  /** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
  int ArgumentTypeNotVisible =
      MethodRelated + 369 + ProblemReasons.NotVisible; // MethodRelated + 371
  /** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
  int ArgumentTypeAmbiguous = MethodRelated + 369 + ProblemReasons.Ambiguous; // MethodRelated + 372
  /** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
  int ArgumentTypeInternalNameProvided =
      MethodRelated + 369 + ProblemReasons.InternalNameProvided; // MethodRelated + 373
  /**
   * @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName}
   *     instead
   */
  int ArgumentTypeInheritedNameHidesEnclosingName =
      MethodRelated + 369 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 374

  /** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
  int ExceptionTypeNotFound = MethodRelated + 374 + ProblemReasons.NotFound; // MethodRelated + 375
  /** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
  int ExceptionTypeNotVisible =
      MethodRelated + 374 + ProblemReasons.NotVisible; // MethodRelated + 376
  /** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
  int ExceptionTypeAmbiguous =
      MethodRelated + 374 + ProblemReasons.Ambiguous; // MethodRelated + 377
  /** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
  int ExceptionTypeInternalNameProvided =
      MethodRelated + 374 + ProblemReasons.InternalNameProvided; // MethodRelated + 378
  /**
   * @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName}
   *     instead
   */
  int ExceptionTypeInheritedNameHidesEnclosingName =
      MethodRelated + 374 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 379

  /** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
  int ReturnTypeNotFound = MethodRelated + 379 + ProblemReasons.NotFound; // MethodRelated + 380
  /** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
  int ReturnTypeNotVisible = MethodRelated + 379 + ProblemReasons.NotVisible; // MethodRelated + 381
  /** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
  int ReturnTypeAmbiguous = MethodRelated + 379 + ProblemReasons.Ambiguous; // MethodRelated + 382
  /** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
  int ReturnTypeInternalNameProvided =
      MethodRelated + 379 + ProblemReasons.InternalNameProvided; // MethodRelated + 383
  /**
   * @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName}
   *     instead
   */
  int ReturnTypeInheritedNameHidesEnclosingName =
      MethodRelated + 379 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 384

  // import related problems
  int ConflictingImport = ImportRelated + 385;
  int DuplicateImport = ImportRelated + 386;
  int CannotImportPackage = ImportRelated + 387;
  int UnusedImport = ImportRelated + 388;

  int ImportNotFound = ImportRelated + 389 + ProblemReasons.NotFound; // ImportRelated + 390
  /** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
  int ImportNotVisible = ImportRelated + 389 + ProblemReasons.NotVisible; // ImportRelated + 391
  /** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
  int ImportAmbiguous = ImportRelated + 389 + ProblemReasons.Ambiguous; // ImportRelated + 392
  /** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
  int ImportInternalNameProvided =
      ImportRelated + 389 + ProblemReasons.InternalNameProvided; // ImportRelated + 393
  /**
   * @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName}
   *     instead
   */
  int ImportInheritedNameHidesEnclosingName =
      ImportRelated + 389 + ProblemReasons.InheritedNameHidesEnclosingName; // ImportRelated + 394

  /** @since 3.1 */
  int InvalidTypeForStaticImport = ImportRelated + 391;

  // local variable related problems
  int DuplicateModifierForVariable = MethodRelated + 395;
  int IllegalModifierForVariable = MethodRelated + 396;
  /**
   * @deprecated - problem is no longer generated, use {@link
   *     #RedundantNullCheckOnNonNullLocalVariable} instead
   */
  int LocalVariableCannotBeNull =
      Internal + 397; // since 3.3: semantics are LocalVariableRedundantCheckOnNonNull
  /**
   * @deprecated - problem is no longer generated, use {@link #NullLocalVariableReference}, {@link
   *     #RedundantNullCheckOnNullLocalVariable} or {@link #RedundantLocalVariableNullAssignment}
   *     instead
   */
  int LocalVariableCanOnlyBeNull =
      Internal
          + 398; // since 3.3: split with LocalVariableRedundantCheckOnNull depending on context
  /**
   * @deprecated - problem is no longer generated, use {@link #PotentialNullLocalVariableReference}
   *     instead
   */
  int LocalVariableMayBeNull = Internal + 399;

  // method verifier problems
  int AbstractMethodMustBeImplemented = MethodRelated + 400;
  int FinalMethodCannotBeOverridden = MethodRelated + 401;
  int IncompatibleExceptionInThrowsClause = MethodRelated + 402;
  int IncompatibleExceptionInInheritedMethodThrowsClause = MethodRelated + 403;
  int IncompatibleReturnType = MethodRelated + 404;
  int InheritedMethodReducesVisibility = MethodRelated + 405;
  int CannotOverrideAStaticMethodWithAnInstanceMethod = MethodRelated + 406;
  int CannotHideAnInstanceMethodWithAStaticMethod = MethodRelated + 407;
  int StaticInheritedMethodConflicts = MethodRelated + 408;
  int MethodReducesVisibility = MethodRelated + 409;
  int OverridingNonVisibleMethod = MethodRelated + 410;
  int AbstractMethodCannotBeOverridden = MethodRelated + 411;
  int OverridingDeprecatedMethod = MethodRelated + 412;
  /** @since 2.1 */
  int IncompatibleReturnTypeForNonInheritedInterfaceMethod = MethodRelated + 413;
  /** @since 2.1 */
  int IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod = MethodRelated + 414;
  /** @since 3.1 */
  int IllegalVararg = MethodRelated + 415;
  /** @since 3.3 */
  int OverridingMethodWithoutSuperInvocation = MethodRelated + 416;
  /** @since 3.5 */
  int MissingSynchronizedModifierInInheritedMethod = MethodRelated + 417;
  /** @since 3.5 */
  int AbstractMethodMustBeImplementedOverConcreteMethod = MethodRelated + 418;
  /** @since 3.5 */
  int InheritedIncompatibleReturnType = MethodRelated + 419;

  // code snippet support
  int CodeSnippetMissingClass = Internal + 420;
  int CodeSnippetMissingMethod = Internal + 421;
  int CannotUseSuperInCodeSnippet = Internal + 422;

  // constant pool
  int TooManyConstantsInConstantPool = Internal + 430;
  /** @since 2.1 */
  int TooManyBytesForStringConstant = Internal + 431;

  // static constraints
  /** @since 2.1 */
  int TooManyFields = Internal + 432;
  /** @since 2.1 */
  int TooManyMethods = Internal + 433;
  /** @since 3.7 */
  int TooManyParametersForSyntheticMethod = Internal + 434;

  // 1.4 features
  // assertion warning
  int UseAssertAsAnIdentifier = Internal + 440;

  // 1.5 features
  int UseEnumAsAnIdentifier = Internal + 441;
  /** @since 3.2 */
  int EnumConstantsCannotBeSurroundedByParenthesis = Syntax + Internal + 442;

  /** @since 3.10 */
  int IllegalUseOfUnderscoreAsAnIdentifier = Syntax + Internal + 443;
  /** @since 3.10 */
  int UninternedIdentityComparison = Syntax + Internal + 444;

  // detected task
  /** @since 2.1 */
  int Task = Internal + 450;

  // local variables related problems, cont'd
  /** @since 3.3 */
  int NullLocalVariableReference = Internal + 451;
  /** @since 3.3 */
  int PotentialNullLocalVariableReference = Internal + 452;
  /** @since 3.3 */
  int RedundantNullCheckOnNullLocalVariable = Internal + 453;
  /** @since 3.3 */
  int NullLocalVariableComparisonYieldsFalse = Internal + 454;
  /** @since 3.3 */
  int RedundantLocalVariableNullAssignment = Internal + 455;
  /** @since 3.3 */
  int NullLocalVariableInstanceofYieldsFalse = Internal + 456;
  /** @since 3.3 */
  int RedundantNullCheckOnNonNullLocalVariable = Internal + 457;
  /** @since 3.3 */
  int NonNullLocalVariableComparisonYieldsFalse = Internal + 458;
  /** @since 3.9 */
  int PotentialNullUnboxing = Internal + 459;
  /** @since 3.9 */
  int NullUnboxing = Internal + 461;

  // block
  /** @since 3.0 */
  int UndocumentedEmptyBlock = Internal + 460;

  /*
   * Javadoc comments
   */
  /**
   * Problem signaled on an invalid URL reference. Valid syntax example: @see
   * "http://www.eclipse.org/"
   *
   * @since 3.4
   */
  int JavadocInvalidSeeUrlReference = Javadoc + Internal + 462;
  /**
   * Problem warned on missing tag description.
   *
   * @since 3.4
   */
  int JavadocMissingTagDescription = Javadoc + Internal + 463;
  /**
   * Problem warned on duplicated tag.
   *
   * @since 3.3
   */
  int JavadocDuplicateTag = Javadoc + Internal + 464;
  /**
   * Problem signaled on an hidden reference due to a too low visibility level.
   *
   * @since 3.3
   */
  int JavadocHiddenReference = Javadoc + Internal + 465;
  /**
   * Problem signaled on an invalid qualification for member type reference.
   *
   * @since 3.3
   */
  int JavadocInvalidMemberTypeQualification = Javadoc + Internal + 466;
  /** @since 3.2 */
  int JavadocMissingIdentifier = Javadoc + Internal + 467;
  /** @since 3.2 */
  int JavadocNonStaticTypeFromStaticInvocation = Javadoc + Internal + 468;
  /** @since 3.1 */
  int JavadocInvalidParamTagTypeParameter = Javadoc + Internal + 469;
  /** @since 3.0 */
  int JavadocUnexpectedTag = Javadoc + Internal + 470;
  /** @since 3.0 */
  int JavadocMissingParamTag = Javadoc + Internal + 471;
  /** @since 3.0 */
  int JavadocMissingParamName = Javadoc + Internal + 472;
  /** @since 3.0 */
  int JavadocDuplicateParamName = Javadoc + Internal + 473;
  /** @since 3.0 */
  int JavadocInvalidParamName = Javadoc + Internal + 474;
  /** @since 3.0 */
  int JavadocMissingReturnTag = Javadoc + Internal + 475;
  /** @since 3.0 */
  int JavadocDuplicateReturnTag = Javadoc + Internal + 476;
  /** @since 3.0 */
  int JavadocMissingThrowsTag = Javadoc + Internal + 477;
  /** @since 3.0 */
  int JavadocMissingThrowsClassName = Javadoc + Internal + 478;
  /** @since 3.0 */
  int JavadocInvalidThrowsClass = Javadoc + Internal + 479;
  /** @since 3.0 */
  int JavadocDuplicateThrowsClassName = Javadoc + Internal + 480;
  /** @since 3.0 */
  int JavadocInvalidThrowsClassName = Javadoc + Internal + 481;
  /** @since 3.0 */
  int JavadocMissingSeeReference = Javadoc + Internal + 482;
  /** @since 3.0 */
  int JavadocInvalidSeeReference = Javadoc + Internal + 483;
  /**
   * Problem signaled on an invalid URL reference that does not conform to the href syntax. Valid
   * syntax example: @see <a href="http://www.eclipse.org/">Eclipse Home Page</a>
   *
   * @since 3.0
   */
  int JavadocInvalidSeeHref = Javadoc + Internal + 484;
  /** @since 3.0 */
  int JavadocInvalidSeeArgs = Javadoc + Internal + 485;
  /** @since 3.0 */
  int JavadocMissing = Javadoc + Internal + 486;
  /** @since 3.0 */
  int JavadocInvalidTag = Javadoc + Internal + 487;
  /*
   * ID for field errors in Javadoc
   */
  /** @since 3.0 */
  int JavadocUndefinedField = Javadoc + Internal + 488;
  /** @since 3.0 */
  int JavadocNotVisibleField = Javadoc + Internal + 489;
  /** @since 3.0 */
  int JavadocAmbiguousField = Javadoc + Internal + 490;
  /** @since 3.0 */
  int JavadocUsingDeprecatedField = Javadoc + Internal + 491;
  /*
   * IDs for constructor errors in Javadoc
   */
  /** @since 3.0 */
  int JavadocUndefinedConstructor = Javadoc + Internal + 492;
  /** @since 3.0 */
  int JavadocNotVisibleConstructor = Javadoc + Internal + 493;
  /** @since 3.0 */
  int JavadocAmbiguousConstructor = Javadoc + Internal + 494;
  /** @since 3.0 */
  int JavadocUsingDeprecatedConstructor = Javadoc + Internal + 495;
  /*
   * IDs for method errors in Javadoc
   */
  /** @since 3.0 */
  int JavadocUndefinedMethod = Javadoc + Internal + 496;
  /** @since 3.0 */
  int JavadocNotVisibleMethod = Javadoc + Internal + 497;
  /** @since 3.0 */
  int JavadocAmbiguousMethod = Javadoc + Internal + 498;
  /** @since 3.0 */
  int JavadocUsingDeprecatedMethod = Javadoc + Internal + 499;
  /** @since 3.0 */
  int JavadocNoMessageSendOnBaseType = Javadoc + Internal + 500;
  /** @since 3.0 */
  int JavadocParameterMismatch = Javadoc + Internal + 501;
  /** @since 3.0 */
  int JavadocNoMessageSendOnArrayType = Javadoc + Internal + 502;
  /*
   * IDs for type errors in Javadoc
   */
  /** @since 3.0 */
  int JavadocUndefinedType = Javadoc + Internal + 503;
  /** @since 3.0 */
  int JavadocNotVisibleType = Javadoc + Internal + 504;
  /** @since 3.0 */
  int JavadocAmbiguousType = Javadoc + Internal + 505;
  /** @since 3.0 */
  int JavadocUsingDeprecatedType = Javadoc + Internal + 506;
  /** @since 3.0 */
  int JavadocInternalTypeNameProvided = Javadoc + Internal + 507;
  /** @since 3.0 */
  int JavadocInheritedMethodHidesEnclosingName = Javadoc + Internal + 508;
  /** @since 3.0 */
  int JavadocInheritedFieldHidesEnclosingName = Javadoc + Internal + 509;
  /** @since 3.0 */
  int JavadocInheritedNameHidesEnclosingTypeName = Javadoc + Internal + 510;
  /** @since 3.0 */
  int JavadocAmbiguousMethodReference = Javadoc + Internal + 511;
  /** @since 3.0 */
  int JavadocUnterminatedInlineTag = Javadoc + Internal + 512;
  /** @since 3.0 */
  int JavadocMalformedSeeReference = Javadoc + Internal + 513;
  /** @since 3.0 */
  int JavadocMessagePrefix = Internal + 514;

  /** @since 3.1 */
  int JavadocMissingHashCharacter = Javadoc + Internal + 515;
  /** @since 3.1 */
  int JavadocEmptyReturnTag = Javadoc + Internal + 516;
  /** @since 3.1 */
  int JavadocInvalidValueReference = Javadoc + Internal + 517;
  /** @since 3.1 */
  int JavadocUnexpectedText = Javadoc + Internal + 518;
  /** @since 3.1 */
  int JavadocInvalidParamTagName = Javadoc + Internal + 519;

  /** Generics */
  /** @since 3.1 */
  int DuplicateTypeVariable = Internal + 520;
  /** @since 3.1 */
  int IllegalTypeVariableSuperReference = Internal + 521;
  /** @since 3.1 */
  int NonStaticTypeFromStaticInvocation = Internal + 522;
  /** @since 3.1 */
  int ObjectCannotBeGeneric = Internal + 523;
  /** @since 3.1 */
  int NonGenericType = TypeRelated + 524;
  /** @since 3.1 */
  int IncorrectArityForParameterizedType = TypeRelated + 525;
  /** @since 3.1 */
  int TypeArgumentMismatch = TypeRelated + 526;
  /** @since 3.1 */
  int DuplicateMethodErasure = TypeRelated + 527;
  /** @since 3.1 */
  int ReferenceToForwardTypeVariable = TypeRelated + 528;
  /** @since 3.1 */
  int BoundMustBeAnInterface = TypeRelated + 529;
  /** @since 3.1 */
  int UnsafeRawConstructorInvocation = TypeRelated + 530;
  /** @since 3.1 */
  int UnsafeRawMethodInvocation = TypeRelated + 531;
  /** @since 3.1 */
  int UnsafeTypeConversion = TypeRelated + 532;
  /** @since 3.1 */
  int InvalidTypeVariableExceptionType = TypeRelated + 533;
  /** @since 3.1 */
  int InvalidParameterizedExceptionType = TypeRelated + 534;
  /** @since 3.1 */
  int IllegalGenericArray = TypeRelated + 535;
  /** @since 3.1 */
  int UnsafeRawFieldAssignment = TypeRelated + 536;
  /** @since 3.1 */
  int FinalBoundForTypeVariable = TypeRelated + 537;
  /** @since 3.1 */
  int UndefinedTypeVariable = Internal + 538;
  /** @since 3.1 */
  int SuperInterfacesCollide = TypeRelated + 539;
  /** @since 3.1 */
  int WildcardConstructorInvocation = TypeRelated + 540;
  /** @since 3.1 */
  int WildcardMethodInvocation = TypeRelated + 541;
  /** @since 3.1 */
  int WildcardFieldAssignment = TypeRelated + 542;
  /** @since 3.1 */
  int GenericMethodTypeArgumentMismatch = TypeRelated + 543;
  /** @since 3.1 */
  int GenericConstructorTypeArgumentMismatch = TypeRelated + 544;
  /** @since 3.1 */
  int UnsafeGenericCast = TypeRelated + 545;
  /** @since 3.1 */
  int IllegalInstanceofParameterizedType = Internal + 546;
  /** @since 3.1 */
  int IllegalInstanceofTypeParameter = Internal + 547;
  /** @since 3.1 */
  int NonGenericMethod = TypeRelated + 548;
  /** @since 3.1 */
  int IncorrectArityForParameterizedMethod = TypeRelated + 549;
  /** @since 3.1 */
  int ParameterizedMethodArgumentTypeMismatch = TypeRelated + 550;
  /** @since 3.1 */
  int NonGenericConstructor = TypeRelated + 551;
  /** @since 3.1 */
  int IncorrectArityForParameterizedConstructor = TypeRelated + 552;
  /** @since 3.1 */
  int ParameterizedConstructorArgumentTypeMismatch = TypeRelated + 553;
  /** @since 3.1 */
  int TypeArgumentsForRawGenericMethod = TypeRelated + 554;
  /** @since 3.1 */
  int TypeArgumentsForRawGenericConstructor = TypeRelated + 555;
  /** @since 3.1 */
  int SuperTypeUsingWildcard = TypeRelated + 556;
  /** @since 3.1 */
  int GenericTypeCannotExtendThrowable = TypeRelated + 557;
  /** @since 3.1 */
  int IllegalClassLiteralForTypeVariable = TypeRelated + 558;
  /** @since 3.1 */
  int UnsafeReturnTypeOverride = MethodRelated + 559;
  /** @since 3.1 */
  int MethodNameClash = MethodRelated + 560;
  /** @since 3.1 */
  int RawMemberTypeCannotBeParameterized = TypeRelated + 561;
  /** @since 3.1 */
  int MissingArgumentsForParameterizedMemberType = TypeRelated + 562;
  /** @since 3.1 */
  int StaticMemberOfParameterizedType = TypeRelated + 563;
  /** @since 3.1 */
  int BoundHasConflictingArguments = TypeRelated + 564;
  /** @since 3.1 */
  int DuplicateParameterizedMethods = MethodRelated + 565;
  /** @since 3.1 */
  int IllegalQualifiedParameterizedTypeAllocation = TypeRelated + 566;
  /** @since 3.1 */
  int DuplicateBounds = TypeRelated + 567;
  /** @since 3.1 */
  int BoundCannotBeArray = TypeRelated + 568;
  /** @since 3.1 */
  int UnsafeRawGenericConstructorInvocation = TypeRelated + 569;
  /** @since 3.1 */
  int UnsafeRawGenericMethodInvocation = TypeRelated + 570;
  /** @since 3.1 */
  int TypeParameterHidingType = TypeRelated + 571;
  /** @since 3.2 */
  int RawTypeReference = TypeRelated + 572;
  /** @since 3.2 */
  int NoAdditionalBoundAfterTypeVariable = TypeRelated + 573;
  /** @since 3.2 */
  int UnsafeGenericArrayForVarargs = MethodRelated + 574;
  /** @since 3.2 */
  int IllegalAccessFromTypeVariable = TypeRelated + 575;
  /** @since 3.3 */
  int TypeHidingTypeParameterFromType = TypeRelated + 576;
  /** @since 3.3 */
  int TypeHidingTypeParameterFromMethod = TypeRelated + 577;
  /** @since 3.3 */
  int InvalidUsageOfWildcard = Syntax + Internal + 578;
  /** @since 3.4 */
  int UnusedTypeArgumentsForMethodInvocation = MethodRelated + 579;

  /** Foreach */
  /** @since 3.1 */
  int IncompatibleTypesInForeach = TypeRelated + 580;
  /** @since 3.1 */
  int InvalidTypeForCollection = Internal + 581;
  /** @since 3.6 */
  int InvalidTypeForCollectionTarget14 = Internal + 582;

  /** @since 3.7.1 */
  int DuplicateInheritedMethods = MethodRelated + 583;
  /** @since 3.8 */
  int MethodNameClashHidden = MethodRelated + 584;

  /** @since 3.9 */
  int UnsafeElementTypeConversion = TypeRelated + 585;

  /** 1.5 Syntax errors (when source level < 1.5) */
  /** @since 3.1 */
  int InvalidUsageOfTypeParameters = Syntax + Internal + 590;
  /** @since 3.1 */
  int InvalidUsageOfStaticImports = Syntax + Internal + 591;
  /** @since 3.1 */
  int InvalidUsageOfForeachStatements = Syntax + Internal + 592;
  /** @since 3.1 */
  int InvalidUsageOfTypeArguments = Syntax + Internal + 593;
  /** @since 3.1 */
  int InvalidUsageOfEnumDeclarations = Syntax + Internal + 594;
  /** @since 3.1 */
  int InvalidUsageOfVarargs = Syntax + Internal + 595;
  /** @since 3.1 */
  int InvalidUsageOfAnnotations = Syntax + Internal + 596;
  /** @since 3.1 */
  int InvalidUsageOfAnnotationDeclarations = Syntax + Internal + 597;
  /** @since 3.4 */
  int InvalidUsageOfTypeParametersForAnnotationDeclaration = Syntax + Internal + 598;
  /** @since 3.4 */
  int InvalidUsageOfTypeParametersForEnumDeclaration = Syntax + Internal + 599;
  /** Annotation */
  /** @since 3.1 */
  int IllegalModifierForAnnotationMethod = MethodRelated + 600;
  /** @since 3.1 */
  int IllegalExtendedDimensions = MethodRelated + 601;
  /** @since 3.1 */
  int InvalidFileNameForPackageAnnotations = Syntax + Internal + 602;
  /** @since 3.1 */
  int IllegalModifierForAnnotationType = TypeRelated + 603;
  /** @since 3.1 */
  int IllegalModifierForAnnotationMemberType = TypeRelated + 604;
  /** @since 3.1 */
  int InvalidAnnotationMemberType = TypeRelated + 605;
  /** @since 3.1 */
  int AnnotationCircularitySelfReference = TypeRelated + 606;
  /** @since 3.1 */
  int AnnotationCircularity = TypeRelated + 607;
  /** @since 3.1 */
  int DuplicateAnnotation = TypeRelated + 608;
  /** @since 3.1 */
  int MissingValueForAnnotationMember = TypeRelated + 609;
  /** @since 3.1 */
  int DuplicateAnnotationMember = Internal + 610;
  /** @since 3.1 */
  int UndefinedAnnotationMember = MethodRelated + 611;
  /** @since 3.1 */
  int AnnotationValueMustBeClassLiteral = Internal + 612;
  /** @since 3.1 */
  int AnnotationValueMustBeConstant = Internal + 613;
  /**
   * @deprecated - problem is no longer generated (code is legite)
   * @since 3.1
   */
  int AnnotationFieldNeedConstantInitialization = Internal + 614;
  /** @since 3.1 */
  int IllegalModifierForAnnotationField = Internal + 615;
  /** @since 3.1 */
  int AnnotationCannotOverrideMethod = MethodRelated + 616;
  /** @since 3.1 */
  int AnnotationMembersCannotHaveParameters = Syntax + Internal + 617;
  /** @since 3.1 */
  int AnnotationMembersCannotHaveTypeParameters = Syntax + Internal + 618;
  /** @since 3.1 */
  int AnnotationTypeDeclarationCannotHaveSuperclass = Syntax + Internal + 619;
  /** @since 3.1 */
  int AnnotationTypeDeclarationCannotHaveSuperinterfaces = Syntax + Internal + 620;
  /** @since 3.1 */
  int DuplicateTargetInTargetAnnotation = Internal + 621;
  /** @since 3.1 */
  int DisallowedTargetForAnnotation = TypeRelated + 622;
  /** @since 3.1 */
  int MethodMustOverride = MethodRelated + 623;
  /** @since 3.1 */
  int AnnotationTypeDeclarationCannotHaveConstructor = Syntax + Internal + 624;
  /** @since 3.1 */
  int AnnotationValueMustBeAnnotation = Internal + 625;
  /** @since 3.1 */
  int AnnotationTypeUsedAsSuperInterface = TypeRelated + 626;
  /** @since 3.1 */
  int MissingOverrideAnnotation = MethodRelated + 627;
  /** @since 3.1 */
  int FieldMissingDeprecatedAnnotation = Internal + 628;
  /** @since 3.1 */
  int MethodMissingDeprecatedAnnotation = Internal + 629;
  /** @since 3.1 */
  int TypeMissingDeprecatedAnnotation = Internal + 630;
  /** @since 3.1 */
  int UnhandledWarningToken = Internal + 631;
  /** @since 3.2 */
  int AnnotationValueMustBeArrayInitializer = Internal + 632;
  /** @since 3.3 */
  int AnnotationValueMustBeAnEnumConstant = Internal + 633;
  /** @since 3.3 */
  int MethodMustOverrideOrImplement = MethodRelated + 634;
  /** @since 3.4 */
  int UnusedWarningToken = Internal + 635;
  /** @since 3.6 */
  int MissingOverrideAnnotationForInterfaceMethodImplementation = MethodRelated + 636;
  /** @since 3.10 */
  int InvalidUsageOfTypeAnnotations = Syntax + Internal + 637;
  /** @since 3.10 */
  int DisallowedExplicitThisParameter = Syntax + Internal + 638;
  /** @since 3.10 */
  int MisplacedTypeAnnotations = Syntax + Internal + 639;
  /** @since 3.10 */
  int IllegalTypeAnnotationsInStaticMemberAccess = Internal + Syntax + 640;
  /** @since 3.10 */
  int IllegalUsageOfTypeAnnotations = Internal + Syntax + 641;
  /** @since 3.10 */
  int IllegalDeclarationOfThisParameter = Internal + Syntax + 642;
  /** @since 3.10 */
  int ExplicitThisParameterNotBelow18 = Internal + Syntax + 643;
  /** @since 3.10 */
  int DefaultMethodNotBelow18 = Internal + Syntax + 644;
  /** @since 3.10 */
  int LambdaExpressionNotBelow18 = Internal + Syntax + 645;
  /** @since 3.10 */
  int MethodReferenceNotBelow18 = Internal + Syntax + 646;
  /** @since 3.10 */
  int ConstructorReferenceNotBelow18 = Internal + Syntax + 647;
  /** @since 3.10 */
  int ExplicitThisParameterNotInLambda = Internal + Syntax + 648;
  /** @since 3.10 */
  int ExplicitAnnotationTargetRequired = TypeRelated + 649;
  /** @since 3.10 */
  int IllegalTypeForExplicitThis = Internal + Syntax + 650;
  /** @since 3.10 */
  int IllegalQualifierForExplicitThis = Internal + Syntax + 651;
  /** @since 3.10 */
  int IllegalQualifierForExplicitThis2 = Internal + Syntax + 652;
  /** @since 3.10 */
  int TargetTypeNotAFunctionalInterface = Internal + TypeRelated + 653;
  /** @since 3.10 */
  int IllegalVarargInLambda = Internal + TypeRelated + 654;
  /** @since 3.10 */
  int illFormedParameterizationOfFunctionalInterface = Internal + TypeRelated + 655;
  /** @since 3.10 */
  int lambdaSignatureMismatched = Internal + TypeRelated + 656;
  /** @since 3.10 */
  int lambdaParameterTypeMismatched = Internal + TypeRelated + 657;
  /** @since 3.10 */
  int IncompatibleLambdaParameterType = Internal + TypeRelated + 658;
  /** @since 3.10 */
  int NoGenericLambda = Internal + TypeRelated + 659;
  /** More problems in generics */
  /** @since 3.4 */
  int UnusedTypeArgumentsForConstructorInvocation = MethodRelated + 660;
  /** @since 3.9 */
  int UnusedTypeParameter = TypeRelated + 661;
  /** @since 3.9 */
  int IllegalArrayOfUnionType = TypeRelated + 662;
  /** @since 3.10 */
  int OuterLocalMustBeEffectivelyFinal = Internal + 663;
  /** @since 3.10 */
  int InterfaceNotFunctionalInterface = Internal + TypeRelated + 664;
  /** @since 3.10 */
  int ConstructionTypeMismatch = Internal + TypeRelated + 665;
  /** @since 3.10 */
  int ToleratedMisplacedTypeAnnotations = Syntax + Internal + 666;

  /** Null analysis for other kinds of expressions, syntactically nonnull */
  /** @since 3.9 */
  int NonNullExpressionComparisonYieldsFalse = Internal + 670;
  /** @since 3.9 */
  int RedundantNullCheckOnNonNullExpression = Internal + 671;
  /** @since 3.9 */
  int NullExpressionReference = Internal + 672;
  /** @since 3.9 */
  int PotentialNullExpressionReference = Internal + 673;

  /** Corrupted binaries */
  /** @since 3.1 */
  int CorruptedSignature = Internal + 700;
  /** Corrupted source */
  /** @since 3.2 */
  int InvalidEncoding = Internal + 701;
  /** @since 3.2 */
  int CannotReadSource = Internal + 702;

  /** Autoboxing */
  /** @since 3.1 */
  int BoxingConversion = Internal + 720;
  /** @since 3.1 */
  int UnboxingConversion = Internal + 721;

  /** Enum */
  /** @since 3.1 */
  int IllegalModifierForEnum = TypeRelated + 750;
  /** @since 3.1 */
  int IllegalModifierForEnumConstant = FieldRelated + 751;
  /**
   * @deprecated - problem could not be reported, enums cannot be local takes precedence
   * @since 3.1
   */
  int IllegalModifierForLocalEnum = TypeRelated + 752;
  /** @since 3.1 */
  int IllegalModifierForMemberEnum = TypeRelated + 753;
  /** @since 3.1 */
  int CannotDeclareEnumSpecialMethod = MethodRelated + 754;
  /** @since 3.1 */
  int IllegalQualifiedEnumConstantLabel = FieldRelated + 755;
  /** @since 3.1 */
  int CannotExtendEnum = TypeRelated + 756;
  /** @since 3.1 */
  int CannotInvokeSuperConstructorInEnum = MethodRelated + 757;
  /** @since 3.1 */
  int EnumAbstractMethodMustBeImplemented = MethodRelated + 758;
  /** @since 3.1 */
  int EnumSwitchCannotTargetField = FieldRelated + 759;
  /** @since 3.1 */
  int IllegalModifierForEnumConstructor = MethodRelated + 760;
  /** @since 3.1 */
  int MissingEnumConstantCase = FieldRelated + 761;
  /** @since 3.2 */
  // TODO need to fix 3.1.1 contribution (inline this constant on client side)
  int EnumStaticFieldInInInitializerContext = FieldRelated + 762;
  /** @since 3.4 */
  int EnumConstantMustImplementAbstractMethod = MethodRelated + 763;
  /** @since 3.5 */
  int EnumConstantCannotDefineAbstractMethod = MethodRelated + 764;
  /** @since 3.5 */
  int AbstractMethodInEnum = MethodRelated + 765;
  /** @since 3.8 */
  int MissingEnumDefaultCase = Internal + 766;
  /** @since 3.8 */
  int MissingDefaultCase = Internal + 767;
  /** @since 3.8 */
  int MissingEnumConstantCaseDespiteDefault = FieldRelated + 768;
  /** @since 3.8 */
  int UninitializedLocalVariableHintMissingDefault = Internal + 769;
  /** @since 3.8 */
  int UninitializedBlankFinalFieldHintMissingDefault = FieldRelated + 770;
  /** @since 3.8 */
  int ShouldReturnValueHintMissingDefault = MethodRelated + 771;

  /** Var args */
  /** @since 3.1 */
  int IllegalExtendedDimensionsForVarArgs = Syntax + Internal + 800;
  /** @since 3.1 */
  int MethodVarargsArgumentNeedCast = MethodRelated + 801;
  /** @since 3.1 */
  int ConstructorVarargsArgumentNeedCast = ConstructorRelated + 802;
  /** @since 3.1 */
  int VarargsConflict = MethodRelated + 803;
  /** @since 3.7.1 */
  int SafeVarargsOnFixedArityMethod = MethodRelated + 804;
  /** @since 3.7.1 */
  int SafeVarargsOnNonFinalInstanceMethod = MethodRelated + 805;
  /** @since 3.7.1 */
  int PotentialHeapPollutionFromVararg = MethodRelated + 806;
  /** @since 3.8 */
  int VarargsElementTypeNotVisible = MethodRelated + 807;
  /** @since 3.8 */
  int VarargsElementTypeNotVisibleForConstructor = ConstructorRelated + 808;
  /** @since 3.10 */
  int ApplicableMethodOverriddenByInapplicable = MethodRelated + 809;

  /** Javadoc Generic */
  /** @since 3.1 */
  int JavadocGenericMethodTypeArgumentMismatch = Javadoc + Internal + 850;
  /** @since 3.1 */
  int JavadocNonGenericMethod = Javadoc + Internal + 851;
  /** @since 3.1 */
  int JavadocIncorrectArityForParameterizedMethod = Javadoc + Internal + 852;
  /** @since 3.1 */
  int JavadocParameterizedMethodArgumentTypeMismatch = Javadoc + Internal + 853;
  /** @since 3.1 */
  int JavadocTypeArgumentsForRawGenericMethod = Javadoc + Internal + 854;
  /** @since 3.1 */
  int JavadocGenericConstructorTypeArgumentMismatch = Javadoc + Internal + 855;
  /** @since 3.1 */
  int JavadocNonGenericConstructor = Javadoc + Internal + 856;
  /** @since 3.1 */
  int JavadocIncorrectArityForParameterizedConstructor = Javadoc + Internal + 857;
  /** @since 3.1 */
  int JavadocParameterizedConstructorArgumentTypeMismatch = Javadoc + Internal + 858;
  /** @since 3.1 */
  int JavadocTypeArgumentsForRawGenericConstructor = Javadoc + Internal + 859;

  /** Java 7 errors */
  /** @since 3.7.1 */
  int AssignmentToMultiCatchParameter = Internal + 870;
  /** @since 3.7.1 */
  int ResourceHasToImplementAutoCloseable = TypeRelated + 871;
  /** @since 3.7.1 */
  int AssignmentToResource = Internal + 872;
  /** @since 3.7.1 */
  int InvalidUnionTypeReferenceSequence = Internal + TypeRelated + 873;
  /** @since 3.7.1 */
  int AutoManagedResourceNotBelow17 = Syntax + Internal + 874;
  /** @since 3.7.1 */
  int MultiCatchNotBelow17 = Syntax + Internal + 875;
  /** @since 3.7.1 */
  int PolymorphicMethodNotBelow17 = MethodRelated + 876;
  /** @since 3.7.1 */
  int IncorrectSwitchType17 = TypeRelated + 877;
  /** @since 3.7.1 */
  int CannotInferElidedTypes = TypeRelated + 878;
  /** @since 3.7.1 */
  int CannotUseDiamondWithExplicitTypeArguments = TypeRelated + 879;
  /** @since 3.7.1 */
  int CannotUseDiamondWithAnonymousClasses = TypeRelated + 880;
  /** @since 3.7.1 */
  int SwitchOnStringsNotBelow17 =
      TypeRelated + 881; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=348492
  /** @since 3.7.1 */
  int UnhandledExceptionOnAutoClose = TypeRelated + 882;
  /** @since 3.7.1 */
  int DiamondNotBelow17 = TypeRelated + 883;
  /** @since 3.7.1 */
  int RedundantSpecificationOfTypeArguments = TypeRelated + 884;
  /** @since 3.8 */
  int PotentiallyUnclosedCloseable = Internal + 885;
  /** @since 3.8 */
  int PotentiallyUnclosedCloseableAtExit = Internal + 886;
  /** @since 3.8 */
  int UnclosedCloseable = Internal + 887;
  /** @since 3.8 */
  int UnclosedCloseableAtExit = Internal + 888;
  /** @since 3.8 */
  int ExplicitlyClosedAutoCloseable = Internal + 889;
  /** @since 3.8 */
  int SwitchOnEnumNotBelow15 =
      TypeRelated + 890; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=360317
  /** @since 3.10 */
  int IntersectionCastNotBelow18 = TypeRelated + 891;
  /** @since 3.10 */
  int IllegalBasetypeInIntersectionCast = TypeRelated + 892;
  /** @since 3.10 */
  int IllegalArrayTypeInIntersectionCast = TypeRelated + 893;
  /** @since 3.10 */
  int DuplicateBoundInIntersectionCast = TypeRelated + 894;
  /** @since 3.10 */
  int MultipleFunctionalInterfaces = TypeRelated + 895;
  /** @since 3.10 */
  int StaticInterfaceMethodNotBelow18 = Internal + Syntax + 896;
  /** @since 3.10 */
  int DuplicateAnnotationNotMarkedRepeatable = TypeRelated + 897;
  /** @since 3.10 */
  int DisallowedTargetForContainerAnnotationType = TypeRelated + 898;
  /** @since 3.10 */
  int RepeatedAnnotationWithContainerAnnotation = TypeRelated + 899;

  /** External problems -- These are problems defined by other plugins */

  /** @since 3.2 */
  int ExternalProblemNotFixable = 900;

  // indicates an externally defined problem that has a quick-assist processor
  // associated with it
  /** @since 3.2 */
  int ExternalProblemFixable = 901;

  /** @since 3.10 */
  int ContainerAnnotationTypeHasWrongValueType = TypeRelated + 902;
  /** @since 3.10 */
  int ContainerAnnotationTypeMustHaveValue = TypeRelated + 903;
  /** @since 3.10 */
  int ContainerAnnotationTypeHasNonDefaultMembers = TypeRelated + 904;
  /** @since 3.10 */
  int ContainerAnnotationTypeHasShorterRetention = TypeRelated + 905;
  /** @since 3.10 */
  int RepeatableAnnotationTypeTargetMismatch = TypeRelated + 906;
  /** @since 3.10 */
  int RepeatableAnnotationTypeIsDocumented = TypeRelated + 907;
  /** @since 3.10 */
  int RepeatableAnnotationTypeIsInherited = TypeRelated + 908;
  /** @since 3.10 */
  int RepeatableAnnotationWithRepeatingContainerAnnotation = TypeRelated + 909;

  /** Errors/warnings from annotation based null analysis */
  /** @since 3.8 */
  int RequiredNonNullButProvidedNull = TypeRelated + 910;
  /** @since 3.8 */
  int RequiredNonNullButProvidedPotentialNull = TypeRelated + 911;
  /** @since 3.8 */
  int RequiredNonNullButProvidedUnknown = TypeRelated + 912;
  /** @since 3.8 */
  int MissingNonNullByDefaultAnnotationOnPackage =
      Internal + 913; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
  /** @since 3.8 */
  int IllegalReturnNullityRedefinition = MethodRelated + 914;
  /** @since 3.8 */
  int IllegalRedefinitionToNonNullParameter = MethodRelated + 915;
  /** @since 3.8 */
  int IllegalDefinitionToNonNullParameter = MethodRelated + 916;
  /** @since 3.8 */
  int ParameterLackingNonNullAnnotation = MethodRelated + 917;
  /** @since 3.8 */
  int ParameterLackingNullableAnnotation = MethodRelated + 918;
  /** @since 3.8 */
  int PotentialNullMessageSendReference = Internal + 919;
  /** @since 3.8 */
  int RedundantNullCheckOnNonNullMessageSend = Internal + 920;
  /** @since 3.8 */
  int CannotImplementIncompatibleNullness = Internal + 921;
  /** @since 3.8 */
  int RedundantNullAnnotation = MethodRelated + 922;
  /** @since 3.8 */
  int IllegalAnnotationForBaseType = TypeRelated + 923;
  /** @since 3.9 */
  int NullableFieldReference = FieldRelated + 924;
  /** @since 3.8 */
  int RedundantNullDefaultAnnotation =
      Internal + 925; // shouldn't actually occur any more after bug 366063
  /** @since 3.8 */
  int RedundantNullDefaultAnnotationPackage = Internal + 926;
  /** @since 3.8 */
  int RedundantNullDefaultAnnotationType = Internal + 927;
  /** @since 3.8 */
  int RedundantNullDefaultAnnotationMethod = Internal + 928;
  /** @since 3.8 */
  int ContradictoryNullAnnotations = Internal + 929;
  /** @since 3.8 */
  int MissingNonNullByDefaultAnnotationOnType =
      Internal + 930; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
  /** @since 3.8 */
  int RedundantNullCheckOnSpecdNonNullLocalVariable = Internal + 931;
  /** @since 3.8 */
  int SpecdNonNullLocalVariableComparisonYieldsFalse = Internal + 932;
  /** @since 3.8 */
  int RequiredNonNullButProvidedSpecdNullable = Internal + 933;
  /** @since 3.9 */
  int UninitializedNonNullField = FieldRelated + 934;
  /** @since 3.9 */
  int UninitializedNonNullFieldHintMissingDefault = FieldRelated + 935;
  /** @since 3.9 */
  int NonNullMessageSendComparisonYieldsFalse = Internal + 936;
  /** @since 3.9 */
  int RedundantNullCheckOnNonNullSpecdField = Internal + 937;
  /** @since 3.9 */
  int NonNullSpecdFieldComparisonYieldsFalse = Internal + 938;
  /** @since 3.9 */
  int ConflictingNullAnnotations = MethodRelated + 939;
  /** @since 3.9 */
  int ConflictingInheritedNullAnnotations = MethodRelated + 940;
  /** @since 3.10 */
  int RedundantNullCheckOnField = Internal + 941;
  /** @since 3.10 */
  int FieldComparisonYieldsFalse = Internal + 942;

  /** @since 3.10 */
  int ArrayReferencePotentialNullReference = Internal + 951;
  /** @since 3.10 */
  int DereferencingNullableExpression = Internal + 952;
  /** @since 3.10 */
  int NullityMismatchingTypeAnnotation = Internal + 953;
  /** @since 3.10 */
  int NullityMismatchingTypeAnnotationSuperHint = Internal + 954;
  /** @since 3.10 */
  int NullityUncheckedTypeAnnotationDetail = Internal + 955;
  /** @since 3.10 */
  int NullityUncheckedTypeAnnotationDetailSuperHint = Internal + 956;
  /** @since 3.10 */
  int ReferenceExpressionParameterNullityMismatch = MethodRelated + 957;
  /** @since 3.10 */
  int ReferenceExpressionParameterNullityUnchecked = MethodRelated + 958;
  /** @since 3.10 */
  int ReferenceExpressionReturnNullRedef = MethodRelated + 959;
  /** @since 3.10 */
  int ReferenceExpressionReturnNullRedefUnchecked = MethodRelated + 960;
  /** @since 3.10 */
  int RedundantNullCheckAgainstNonNullType = Internal + 961;
  /** @since 3.10 */
  int NullAnnotationUnsupportedLocation = Internal + 962;
  /** @since 3.10 */
  int NullAnnotationUnsupportedLocationAtType = Internal + 963;
  /** @since 3.10 */
  int NullityMismatchTypeArgument = Internal + 964;
  /** @since 3.10 */
  int ContradictoryNullAnnotationsOnBound = Internal + 965;
  /** @since 3.10 */
  int ContradictoryNullAnnotationsInferred = Internal + 966;
  /** @since 3.10 */
  int UnsafeNullnessCast = Internal + 967;
  /** @since 3.10 */
  int NonNullDefaultDetailIsNotEvaluated = 968; // no longer reported
  /** @since 3.10 */
  int NullNotCompatibleToFreeTypeVariable = 969;
  /** @since 3.10 */
  int NullityMismatchAgainstFreeTypeVariable = 970;

  // Java 8 work
  /** @since 3.10 */
  int IllegalModifiersForElidedType = Internal + 1001;
  /** @since 3.10 */
  int IllegalModifiers = Internal + 1002;

  /** @since 3.10 */
  int IllegalTypeArgumentsInRawConstructorReference = TypeRelated + 1003;

  // default methods:
  /** @since 3.10 */
  int IllegalModifierForInterfaceMethod18 = MethodRelated + 1050;

  /** @since 3.10 */
  int DefaultMethodOverridesObjectMethod = MethodRelated + 1051;

  /** @since 3.10 */
  int InheritedDefaultMethodConflictsWithOtherInherited = MethodRelated + 1052;

  /** @since 3.10 */
  int DuplicateInheritedDefaultMethods = MethodRelated + 1053;

  /** @since 3.10 */
  int SuperAccessCannotBypassDirectSuper = TypeRelated + 1054;
  /** @since 3.10 */
  int SuperCallCannotBypassOverride = MethodRelated + 1055;
  /** @since 3.10 */
  int IllegalModifierCombinationForInterfaceMethod = MethodRelated + 1056;
  /** @since 3.10 */
  int IllegalStrictfpForAbstractInterfaceMethod = MethodRelated + 1057;
  /** @since 3.10 */
  int IllegalDefaultModifierSpecification = MethodRelated + 1058;

  /** @since 3.10 */
  int GenericInferenceError = 1100; // FIXME: This is just a stop-gap measure, be more specific via
  // https://bugs.eclipse.org/404675

  /** @since 3.10 */
  int LambdaShapeComputationError = 1101;
}
