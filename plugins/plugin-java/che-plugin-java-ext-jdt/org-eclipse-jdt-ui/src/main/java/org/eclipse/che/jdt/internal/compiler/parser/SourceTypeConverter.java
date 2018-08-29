/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.internal.compiler.parser;

/**
 * Converter from source element type to parsed compilation unit.
 *
 * <p>Limitation: | The source element field does not carry any information for its constant part,
 * thus | the converted parse tree will not include any field initializations. | Therefore, any
 * binary produced by compiling against converted source elements will | not take advantage of
 * remote field constant inlining. | Given the intended purpose of the conversion is to resolve
 * references, this is not | a problem.
 */
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ISourceImport;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.TypeConverter;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.ImportDeclaration;
import org.eclipse.jdt.internal.core.InitializerElementInfo;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.SourceAnnotationMethodInfo;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceFieldElementInfo;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceMethodElementInfo;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.util.Util;

public class SourceTypeConverter extends TypeConverter {

  /*
   * Exception thrown while converting an anonymous type of a member type
   * in this case, we must parse the source as the enclosing instance cannot be recreated
   * from the model
   */
  static class AnonymousMemberFound extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  public static final int FIELD = 0x01;
  public static final int CONSTRUCTOR = 0x02;
  public static final int METHOD = 0x04;
  public static final int MEMBER_TYPE = 0x08;
  public static final int FIELD_INITIALIZATION = 0x10;
  public static final int FIELD_AND_METHOD = FIELD | CONSTRUCTOR | METHOD;
  public static final int LOCAL_TYPE = 0x20;
  public static final int NONE = 0;

  private int flags;
  private CompilationUnitDeclaration unit;
  private Parser parser;
  private ICompilationUnit cu;
  private char[] source;

  private SourceTypeConverter(int flags, ProblemReporter problemReporter) {
    super(problemReporter, Signature.C_DOT);
    this.flags = flags;
  }

  /*
   * Convert a set of source element types into a parsed compilation unit declaration
   * The argument types are then all grouped in the same unit. The argument types must
   * at least contain one type.
   * Can optionally ignore fields & methods or member types or field initialization
   */
  public static CompilationUnitDeclaration buildCompilationUnit(
      ISourceType[] sourceTypes,
      int flags,
      ProblemReporter problemReporter,
      CompilationResult compilationResult) {

    //		long start = System.currentTimeMillis();
    SourceTypeConverter converter = new SourceTypeConverter(flags, problemReporter);
    try {
      return converter.convert(sourceTypes, compilationResult);
    } catch (JavaModelException e) {
      return null;
      /*		} finally {
      			System.out.println("Spent " + (System.currentTimeMillis() - start) + "ms to convert " + ((JavaElement) converter.cu)
      			.toStringWithAncestors());
      */
    }
  }

  /*
   * Convert a set of source element types into a parsed compilation unit declaration
   * The argument types are then all grouped in the same unit. The argument types must
   * at least contain one type.
   */
  private CompilationUnitDeclaration convert(
      ISourceType[] sourceTypes, CompilationResult compilationResult) throws JavaModelException {
    this.unit = new CompilationUnitDeclaration(this.problemReporter, compilationResult, 0);
    // not filled at this point

    if (sourceTypes.length == 0) return this.unit;
    SourceTypeElementInfo topLevelTypeInfo = (SourceTypeElementInfo) sourceTypes[0];
    org.eclipse.jdt.core.ICompilationUnit cuHandle =
        topLevelTypeInfo.getHandle().getCompilationUnit();
    this.cu = (ICompilationUnit) cuHandle;

    final CompilationUnitElementInfo compilationUnitElementInfo =
        (CompilationUnitElementInfo) ((JavaElement) this.cu).getElementInfo();
    if (this.has1_5Compliance
        && (compilationUnitElementInfo.annotationNumber
                >= CompilationUnitElementInfo.ANNOTATION_THRESHOLD_FOR_DIET_PARSE
            || (compilationUnitElementInfo.hasFunctionalTypes && (this.flags & LOCAL_TYPE) != 0))) {
      // If more than 10 annotations, diet parse as this is faster, but not if
      // the client wants local and anonymous types to be converted
      // (https://bugs.eclipse.org/bugs/show_bug.cgi?id=254738)
      // Also see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=405843
      if ((this.flags & LOCAL_TYPE) == 0) {
        return new Parser(this.problemReporter, true).dietParse(this.cu, compilationResult);
      } else {
        return new Parser(this.problemReporter, true).parse(this.cu, compilationResult);
      }
    }

    /* only positions available */
    int start = topLevelTypeInfo.getNameSourceStart();
    int end = topLevelTypeInfo.getNameSourceEnd();

    /* convert package and imports */
    String[] packageName = ((PackageFragment) cuHandle.getParent()).names;
    if (packageName.length > 0)
      // if its null then it is defined in the default package
      this.unit.currentPackage =
          createImportReference(packageName, start, end, false, ClassFileConstants.AccDefault);
    IImportDeclaration[] importDeclarations =
        topLevelTypeInfo.getHandle().getCompilationUnit().getImports();
    int importCount = importDeclarations.length;
    this.unit.imports = new ImportReference[importCount];
    for (int i = 0; i < importCount; i++) {
      ImportDeclaration importDeclaration = (ImportDeclaration) importDeclarations[i];
      ISourceImport sourceImport = (ISourceImport) importDeclaration.getElementInfo();
      String nameWithoutStar = importDeclaration.getNameWithoutStar();
      this.unit.imports[i] =
          createImportReference(
              Util.splitOn('.', nameWithoutStar, 0, nameWithoutStar.length()),
              sourceImport.getDeclarationSourceStart(),
              sourceImport.getDeclarationSourceEnd(),
              importDeclaration.isOnDemand(),
              sourceImport.getModifiers());
    }
    /* convert type(s) */
    try {
      int typeCount = sourceTypes.length;
      final TypeDeclaration[] types = new TypeDeclaration[typeCount];
      /*
       * We used a temporary types collection to prevent this.unit.types from being null during a call to
       * convert(...) when the source is syntactically incorrect and the parser is flushing the unit's types.
       * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=97466
       */
      for (int i = 0; i < typeCount; i++) {
        SourceTypeElementInfo typeInfo = (SourceTypeElementInfo) sourceTypes[i];
        types[i] = convert((SourceType) typeInfo.getHandle(), compilationResult);
      }
      this.unit.types = types;
      return this.unit;
    } catch (AnonymousMemberFound e) {
      return new Parser(this.problemReporter, true).parse(this.cu, compilationResult);
    }
  }

  /*
   * Convert an initializerinfo into a parsed initializer declaration
   */
  private Initializer convert(
      InitializerElementInfo initializerInfo, CompilationResult compilationResult)
      throws JavaModelException {

    Block block = new Block(0);
    Initializer initializer = new Initializer(block, ClassFileConstants.AccDefault);

    int start = initializerInfo.getDeclarationSourceStart();
    int end = initializerInfo.getDeclarationSourceEnd();

    initializer.sourceStart = initializer.declarationSourceStart = start;
    initializer.sourceEnd = initializer.declarationSourceEnd = end;
    initializer.modifiers = initializerInfo.getModifiers();

    /* convert local and anonymous types */
    IJavaElement[] children = initializerInfo.getChildren();
    int typesLength = children.length;
    if (typesLength > 0) {
      Statement[] statements = new Statement[typesLength];
      for (int i = 0; i < typesLength; i++) {
        SourceType type = (SourceType) children[i];
        TypeDeclaration localType = convert(type, compilationResult);
        if ((localType.bits & ASTNode.IsAnonymousType) != 0) {
          QualifiedAllocationExpression expression = new QualifiedAllocationExpression(localType);
          expression.type = localType.superclass;
          localType.superclass = null;
          localType.superInterfaces = null;
          localType.allocation = expression;
          statements[i] = expression;
        } else {
          statements[i] = localType;
        }
      }
      block.statements = statements;
    }

    return initializer;
  }

  /*
   * Convert a field source element into a parsed field declaration
   */
  private FieldDeclaration convert(
      SourceField fieldHandle, TypeDeclaration type, CompilationResult compilationResult)
      throws JavaModelException {

    SourceFieldElementInfo fieldInfo = (SourceFieldElementInfo) fieldHandle.getElementInfo();
    FieldDeclaration field = new FieldDeclaration();

    int start = fieldInfo.getNameSourceStart();
    int end = fieldInfo.getNameSourceEnd();

    field.name = fieldHandle.getElementName().toCharArray();
    field.sourceStart = start;
    field.sourceEnd = end;
    field.declarationSourceStart = fieldInfo.getDeclarationSourceStart();
    field.declarationSourceEnd = fieldInfo.getDeclarationSourceEnd();
    int modifiers = fieldInfo.getModifiers();
    boolean isEnumConstant = (modifiers & ClassFileConstants.AccEnum) != 0;
    if (isEnumConstant) {
      field.modifiers =
          modifiers
              & ~ClassFileConstants.AccEnum; // clear AccEnum bit onto AST (binding will add it)
    } else {
      field.modifiers = modifiers;
      field.type = createTypeReference(fieldInfo.getTypeName(), start, end);
    }

    // convert 1.5 specific constructs only if compliance is 1.5 or above
    if (this.has1_5Compliance) {
      /* convert annotations */
      field.annotations = convertAnnotations(fieldHandle);
    }

    /* conversion of field constant */
    if ((this.flags & FIELD_INITIALIZATION) != 0) {
      char[] initializationSource = fieldInfo.getInitializationSource();
      if (initializationSource != null) {
        if (this.parser == null) {
          this.parser = new Parser(this.problemReporter, true);
        }
        this.parser.parse(field, type, this.unit, initializationSource);
      }
    }

    /* conversion of local and anonymous types */
    if ((this.flags & LOCAL_TYPE) != 0) {
      IJavaElement[] children = fieldInfo.getChildren();
      int childrenLength = children.length;
      if (childrenLength == 1) {
        field.initialization =
            convert(children[0], isEnumConstant ? field : null, compilationResult);
      } else if (childrenLength > 1) {
        ArrayInitializer initializer = new ArrayInitializer();
        field.initialization = initializer;
        Expression[] expressions = new Expression[childrenLength];
        initializer.expressions = expressions;
        for (int i = 0; i < childrenLength; i++) {
          expressions[i] = convert(children[i], isEnumConstant ? field : null, compilationResult);
        }
      }
    }
    return field;
  }

  private QualifiedAllocationExpression convert(
      IJavaElement localType, FieldDeclaration enumConstant, CompilationResult compilationResult)
      throws JavaModelException {
    TypeDeclaration anonymousLocalTypeDeclaration =
        convert((SourceType) localType, compilationResult);
    QualifiedAllocationExpression expression =
        new QualifiedAllocationExpression(anonymousLocalTypeDeclaration);
    expression.type = anonymousLocalTypeDeclaration.superclass;
    anonymousLocalTypeDeclaration.superclass = null;
    anonymousLocalTypeDeclaration.superInterfaces = null;
    anonymousLocalTypeDeclaration.allocation = expression;
    if (enumConstant != null) {
      anonymousLocalTypeDeclaration.modifiers &= ~ClassFileConstants.AccEnum;
      expression.enumConstant = enumConstant;
      expression.type = null;
    }
    return expression;
  }

  /*
   * Convert a method source element into a parsed method/constructor declaration
   */
  private AbstractMethodDeclaration convert(
      SourceMethod methodHandle,
      SourceMethodElementInfo methodInfo,
      CompilationResult compilationResult)
      throws JavaModelException {
    AbstractMethodDeclaration method;

    /* only source positions available */
    int start = methodInfo.getNameSourceStart();
    int end = methodInfo.getNameSourceEnd();

    /* https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850, Even when this type is being constructed
      on behalf of a 1.4 project we must internalize type variables properly in order to be able to
      recognize usages of them in the method signature, to apply substitutions and thus to be able to
      detect overriding in the presence of generics. If we simply drop them, when the method signature
      refers to the type parameter, we won't know it should be bound to the type parameter and perform
      incorrect lookup and may mistakenly end up with missing types
    */
    TypeParameter[] typeParams = null;
    char[][] typeParameterNames = methodInfo.getTypeParameterNames();
    if (typeParameterNames != null) {
      int parameterCount = typeParameterNames.length;
      if (parameterCount > 0) { // method's type parameters must be null if no type parameter
        char[][][] typeParameterBounds = methodInfo.getTypeParameterBounds();
        typeParams = new TypeParameter[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
          typeParams[i] =
              createTypeParameter(typeParameterNames[i], typeParameterBounds[i], start, end);
        }
      }
    }

    int modifiers = methodInfo.getModifiers();
    if (methodInfo.isConstructor()) {
      ConstructorDeclaration decl = new ConstructorDeclaration(compilationResult);
      decl.bits &= ~ASTNode.IsDefaultConstructor;
      method = decl;
      decl.typeParameters = typeParams;
    } else {
      MethodDeclaration decl;
      if (methodInfo.isAnnotationMethod()) {
        AnnotationMethodDeclaration annotationMethodDeclaration =
            new AnnotationMethodDeclaration(compilationResult);

        /* conversion of default value */
        SourceAnnotationMethodInfo annotationMethodInfo = (SourceAnnotationMethodInfo) methodInfo;
        boolean hasDefaultValue =
            annotationMethodInfo.defaultValueStart != -1
                || annotationMethodInfo.defaultValueEnd != -1;
        if ((this.flags & FIELD_INITIALIZATION) != 0) {
          if (hasDefaultValue) {
            char[] defaultValueSource =
                CharOperation.subarray(
                    getSource(),
                    annotationMethodInfo.defaultValueStart,
                    annotationMethodInfo.defaultValueEnd + 1);
            if (defaultValueSource != null) {
              Expression expression = parseMemberValue(defaultValueSource);
              if (expression != null) {
                annotationMethodDeclaration.defaultValue = expression;
              }
            } else {
              // could not retrieve the default value
              hasDefaultValue = false;
            }
          }
        }
        if (hasDefaultValue) modifiers |= ClassFileConstants.AccAnnotationDefault;
        decl = annotationMethodDeclaration;
      } else {
        decl = new MethodDeclaration(compilationResult);
      }

      // convert return type
      decl.returnType = createTypeReference(methodInfo.getReturnTypeName(), start, end);

      // type parameters
      decl.typeParameters = typeParams;

      method = decl;
    }
    method.selector = methodHandle.getElementName().toCharArray();
    boolean isVarargs = (modifiers & ClassFileConstants.AccVarargs) != 0;
    method.modifiers = modifiers & ~ClassFileConstants.AccVarargs;
    method.sourceStart = start;
    method.sourceEnd = end;
    method.declarationSourceStart = methodInfo.getDeclarationSourceStart();
    method.declarationSourceEnd = methodInfo.getDeclarationSourceEnd();

    // convert 1.5 specific constructs only if compliance is 1.5 or above
    if (this.has1_5Compliance) {
      /* convert annotations */
      method.annotations = convertAnnotations(methodHandle);
    }

    /* convert arguments */
    String[] argumentTypeSignatures = methodHandle.getParameterTypes();
    char[][] argumentNames = methodInfo.getArgumentNames();
    int argumentCount = argumentTypeSignatures == null ? 0 : argumentTypeSignatures.length;
    if (argumentCount > 0) {
      ILocalVariable[] parameters = methodHandle.getParameters();
      long position = ((long) start << 32) + end;
      method.arguments = new Argument[argumentCount];
      for (int i = 0; i < argumentCount; i++) {
        TypeReference typeReference = createTypeReference(argumentTypeSignatures[i], start, end);
        if (isVarargs && i == argumentCount - 1) {
          typeReference.bits |= ASTNode.IsVarArgs;
        }
        method.arguments[i] =
            new Argument(argumentNames[i], position, typeReference, ClassFileConstants.AccDefault);
        // do not care whether was final or not
        // convert 1.5 specific constructs only if compliance is 1.5 or above
        if (this.has1_5Compliance) {
          /* convert annotations */
          method.arguments[i].annotations = convertAnnotations(parameters[i]);
        }
      }
    }

    /* convert thrown exceptions */
    char[][] exceptionTypeNames = methodInfo.getExceptionTypeNames();
    int exceptionCount = exceptionTypeNames == null ? 0 : exceptionTypeNames.length;
    if (exceptionCount > 0) {
      method.thrownExceptions = new TypeReference[exceptionCount];
      for (int i = 0; i < exceptionCount; i++) {
        method.thrownExceptions[i] = createTypeReference(exceptionTypeNames[i], start, end);
      }
    }

    /* convert local and anonymous types */
    if ((this.flags & LOCAL_TYPE) != 0) {
      IJavaElement[] children = methodInfo.getChildren();
      int typesLength = children.length;
      if (typesLength != 0) {
        Statement[] statements = new Statement[typesLength];
        for (int i = 0; i < typesLength; i++) {
          SourceType type = (SourceType) children[i];
          TypeDeclaration localType = convert(type, compilationResult);
          if ((localType.bits & ASTNode.IsAnonymousType) != 0) {
            QualifiedAllocationExpression expression = new QualifiedAllocationExpression(localType);
            expression.type = localType.superclass;
            localType.superclass = null;
            localType.superInterfaces = null;
            localType.allocation = expression;
            statements[i] = expression;
          } else {
            statements[i] = localType;
          }
        }
        method.statements = statements;
      }
    }

    return method;
  }

  /*
   * Convert a source element type into a parsed type declaration
   */
  private TypeDeclaration convert(SourceType typeHandle, CompilationResult compilationResult)
      throws JavaModelException {
    SourceTypeElementInfo typeInfo = (SourceTypeElementInfo) typeHandle.getElementInfo();
    if (typeInfo.isAnonymousMember()) throw new AnonymousMemberFound();
    /* create type declaration - can be member type */
    TypeDeclaration type = new TypeDeclaration(compilationResult);
    if (typeInfo.getEnclosingType() == null) {
      if (typeHandle.isAnonymous()) {
        type.name = CharOperation.NO_CHAR;
        type.bits |= (ASTNode.IsAnonymousType | ASTNode.IsLocalType);
      } else {
        if (typeHandle.isLocal()) {
          type.bits |= ASTNode.IsLocalType;
        }
      }
    } else {
      type.bits |= ASTNode.IsMemberType;
    }
    if ((type.bits & ASTNode.IsAnonymousType) == 0) {
      type.name = typeInfo.getName();
    }
    type.name = typeInfo.getName();
    int start, end; // only positions available
    type.sourceStart = start = typeInfo.getNameSourceStart();
    type.sourceEnd = end = typeInfo.getNameSourceEnd();
    type.modifiers = typeInfo.getModifiers();
    type.declarationSourceStart = typeInfo.getDeclarationSourceStart();
    type.declarationSourceEnd = typeInfo.getDeclarationSourceEnd();
    type.bodyEnd = type.declarationSourceEnd;

    // convert 1.5 specific constructs only if compliance is 1.5 or above
    if (this.has1_5Compliance) {
      /* convert annotations */
      type.annotations = convertAnnotations(typeHandle);
    }
    /* https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850, even in a 1.4 project, we
      must internalize type variables and observe any parameterization of super class
      and/or super interfaces in order to be able to detect overriding in the presence
      of generics.
    */
    char[][] typeParameterNames = typeInfo.getTypeParameterNames();
    if (typeParameterNames.length > 0) {
      int parameterCount = typeParameterNames.length;
      char[][][] typeParameterBounds = typeInfo.getTypeParameterBounds();
      type.typeParameters = new TypeParameter[parameterCount];
      for (int i = 0; i < parameterCount; i++) {
        type.typeParameters[i] =
            createTypeParameter(typeParameterNames[i], typeParameterBounds[i], start, end);
      }
    }

    /* set superclass and superinterfaces */
    if (typeInfo.getSuperclassName() != null) {
      type.superclass =
          createTypeReference(
              typeInfo.getSuperclassName(), start, end, true /* include generics */);
      type.superclass.bits |= ASTNode.IsSuperType;
    }
    char[][] interfaceNames = typeInfo.getInterfaceNames();
    int interfaceCount = interfaceNames == null ? 0 : interfaceNames.length;
    if (interfaceCount > 0) {
      type.superInterfaces = new TypeReference[interfaceCount];
      for (int i = 0; i < interfaceCount; i++) {
        type.superInterfaces[i] =
            createTypeReference(interfaceNames[i], start, end, true /* include generics */);
        type.superInterfaces[i].bits |= ASTNode.IsSuperType;
      }
    }
    /* convert member types */
    if ((this.flags & MEMBER_TYPE) != 0) {
      SourceType[] sourceMemberTypes = typeInfo.getMemberTypeHandles();
      int sourceMemberTypeCount = sourceMemberTypes.length;
      type.memberTypes = new TypeDeclaration[sourceMemberTypeCount];
      for (int i = 0; i < sourceMemberTypeCount; i++) {
        type.memberTypes[i] = convert(sourceMemberTypes[i], compilationResult);
        type.memberTypes[i].enclosingType = type;
      }
    }

    /* convert intializers and fields*/
    InitializerElementInfo[] initializers = null;
    int initializerCount = 0;
    if ((this.flags & LOCAL_TYPE) != 0) {
      initializers = typeInfo.getInitializers();
      initializerCount = initializers.length;
    }
    SourceField[] sourceFields = null;
    int sourceFieldCount = 0;
    if ((this.flags & FIELD) != 0) {
      sourceFields = typeInfo.getFieldHandles();
      sourceFieldCount = sourceFields.length;
    }
    int length = initializerCount + sourceFieldCount;
    if (length > 0) {
      type.fields = new FieldDeclaration[length];
      for (int i = 0; i < initializerCount; i++) {
        type.fields[i] = convert(initializers[i], compilationResult);
      }
      int index = 0;
      for (int i = initializerCount; i < length; i++) {
        type.fields[i] = convert(sourceFields[index++], type, compilationResult);
      }
    }

    /* convert methods - need to add default constructor if necessary */
    boolean needConstructor = (this.flags & CONSTRUCTOR) != 0;
    boolean needMethod = (this.flags & METHOD) != 0;
    if (needConstructor || needMethod) {

      SourceMethod[] sourceMethods = typeInfo.getMethodHandles();
      int sourceMethodCount = sourceMethods.length;

      /* source type has a constructor ?           */
      /* by default, we assume that one is needed. */
      int extraConstructor = 0;
      int methodCount = 0;
      int kind = TypeDeclaration.kind(type.modifiers);
      boolean isAbstract =
          kind == TypeDeclaration.INTERFACE_DECL || kind == TypeDeclaration.ANNOTATION_TYPE_DECL;
      if (!isAbstract) {
        extraConstructor = needConstructor ? 1 : 0;
        for (int i = 0; i < sourceMethodCount; i++) {
          if (sourceMethods[i].isConstructor()) {
            if (needConstructor) {
              extraConstructor =
                  0; // Does not need the extra constructor since one constructor already exists.
              methodCount++;
            }
          } else if (needMethod) {
            methodCount++;
          }
        }
      } else {
        methodCount = needMethod ? sourceMethodCount : 0;
      }
      type.methods = new AbstractMethodDeclaration[methodCount + extraConstructor];
      if (extraConstructor != 0) { // add default constructor in first position
        type.methods[0] = type.createDefaultConstructor(false, false);
      }
      int index = 0;
      boolean hasAbstractMethods = false;
      for (int i = 0; i < sourceMethodCount; i++) {
        SourceMethod sourceMethod = sourceMethods[i];
        SourceMethodElementInfo methodInfo =
            (SourceMethodElementInfo) sourceMethod.getElementInfo();
        boolean isConstructor = methodInfo.isConstructor();
        if ((methodInfo.getModifiers() & ClassFileConstants.AccAbstract) != 0) {
          hasAbstractMethods = true;
        }
        if ((isConstructor && needConstructor) || (!isConstructor && needMethod)) {
          AbstractMethodDeclaration method = convert(sourceMethod, methodInfo, compilationResult);
          if (isAbstract || method.isAbstract()) { // fix-up flag
            method.modifiers |= ExtraCompilerModifiers.AccSemicolonBody;
          }
          type.methods[extraConstructor + index++] = method;
        }
      }
      if (hasAbstractMethods) type.bits |= ASTNode.HasAbstractMethods;
    }

    return type;
  }

  private Annotation[] convertAnnotations(IAnnotatable element) throws JavaModelException {
    IAnnotation[] annotations = element.getAnnotations();
    int length = annotations.length;
    Annotation[] astAnnotations = new Annotation[length];
    if (length > 0) {
      char[] cuSource = getSource();
      int recordedAnnotations = 0;
      for (int i = 0; i < length; i++) {
        ISourceRange positions = annotations[i].getSourceRange();
        int start = positions.getOffset();
        int end = start + positions.getLength();
        char[] annotationSource = CharOperation.subarray(cuSource, start, end);
        if (annotationSource != null) {
          Expression expression = parseMemberValue(annotationSource);
          /*
           * expression can be null or not an annotation if the source has changed between
           * the moment where the annotation source positions have been retrieved and the moment were
           * this parsing occurred.
           * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=90916
           */
          if (expression instanceof Annotation) {
            astAnnotations[recordedAnnotations++] = (Annotation) expression;
          }
        }
      }
      if (length != recordedAnnotations) {
        // resize to remove null annotations
        System.arraycopy(
            astAnnotations,
            0,
            (astAnnotations = new Annotation[recordedAnnotations]),
            0,
            recordedAnnotations);
      }
    }
    return astAnnotations;
  }

  private char[] getSource() {
    if (this.source == null) this.source = this.cu.getContents();
    return this.source;
  }

  private Expression parseMemberValue(char[] memberValue) {
    // memberValue must not be null
    if (this.parser == null) {
      this.parser = new Parser(this.problemReporter, true);
    }
    return this.parser.parseMemberValue(memberValue, 0, memberValue.length, this.unit);
  }
}
