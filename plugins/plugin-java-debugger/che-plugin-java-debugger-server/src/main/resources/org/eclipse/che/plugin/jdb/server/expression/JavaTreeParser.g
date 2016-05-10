/**
 * For more information see the head comment within the 'java.g' grammar file
 * that defines the input for this tree grammar.
 *
 * BSD licence
 * 
 * Copyright (c) 2007-2008 by HABELITZ Software Developments
 *
 * All rights reserved.
 * 
 * http://www.habelitz.com
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY HABELITZ SOFTWARE DEVELOPMENTS ('HSD') ``AS IS'' 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL 'HSD' BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
tree grammar JavaTreeParser;

options {
  backtrack    = true;
  memoize      = true;
  tokenVocab   = Java;
  ASTLabelType = CommonTree;
}

@treeparser::header {
package org.eclipse.che.ide.ext.java.jdi.server.expression;
}

@treeparser::members {
/*
public static com.sun.jdi.Value evaluate(String expr, Evaluator ev) {
   try
   {
      JavaLexer lex = new JavaLexer(new ANTLRStringStream(expr));
      CommonTokenStream tokens = new CommonTokenStream(lex);
      JavaParser parser = new JavaParser(tokens);
      CommonTreeNodeStream nodes = new CommonTreeNodeStream(parser.expression().getTree());
      JavaTreeParser walker = new JavaTreeParser(nodes, ev);
      return walker.evaluate();
   }
   catch (RecognitionException e)
   {
      throw new ExpressionException(e.getMessage(), e);
   }
}
*/
boolean mMessageCollectionEnabled = false;
private boolean mHasErrors = false;
List<String> mMessages;

private Evaluator ev;
private ExpressionValue latest;

public JavaTreeParser(TreeNodeStream input, Evaluator ev) {
	this(input);
	this.ev = ev;
}

/**
 *  Switches error message collection on or of.
 *
 *  The standard destination for parser error messages is <code>System.err</code>.
 *  However, if <code>true</code> gets passed to this method this default
 *  behaviour will be switched off and all error messages will be collected
 *  instead of written to anywhere.
 *
 *  The default value is <code>false</code>.
 *
 *  @param pNewState  <code>true</code> if error messages should be collected.
 */
public void enableErrorMessageCollection(boolean pNewState) {
	mMessageCollectionEnabled = pNewState;
	if (mMessages == null && mMessageCollectionEnabled) {
		mMessages = new ArrayList<String>();
	}
}

/**
 *  Collects an error message or passes the error message to <code>
 *  super.emitErrorMessage(...)</code>.
 *
 *  The actual behaviour depends on whether collecting error messages
 *  has been enabled or not.
 *
 *  @param pMessage  The error message.
 */
@Override
public void emitErrorMessage(String pMessage) {
	if (mMessageCollectionEnabled) {
		mMessages.add(pMessage);
	} else {
		super.emitErrorMessage(pMessage);
	}
}

/**
 *  Returns collected error messages.
 *
 *  @return  A list holding collected error messages or <code>null</code> if
 *           collecting error messages hasn't been enabled. Of course, this
 *           list may be empty if no error message has been emited.
 */
public List<String> getMessages() {
	return mMessages;
}

/**
 *  Tells if parsing a Java source has caused any error messages.
 *
 *  @return  <code>true</code> if parsing a Java source has caused at least one error message.
 */
public boolean hasErrors() {
	return mHasErrors;
}
}

// Starting point for parsing a Java file.

javaSource
  :
  ^(JAVA_SOURCE annotationList packageDeclaration? importDeclaration* typeDeclaration*)
  ;

packageDeclaration
  :
  ^(PACKAGE qualifiedIdentifier)
  ;

importDeclaration
  :
  ^(IMPORT STATIC? qualifiedIdentifier DOTSTAR?)
  ;

typeDeclaration
  :
  ^(CLASS modifierList IDENT genericTypeParameterList? extendsClause? implementsClause? classTopLevelScope)
  |
  ^(INTERFACE modifierList IDENT genericTypeParameterList? extendsClause? interfaceTopLevelScope)
  |
  ^(ENUM modifierList IDENT implementsClause? enumTopLevelScope)
  |
  ^(AT modifierList IDENT annotationTopLevelScope)
  ;

extendsClause // actually 'type' for classes and 'type+' for interfaces, but this has
  // been resolved by the parser grammar.
  :
  ^(EXTENDS_CLAUSE type+)
  ;

implementsClause
  :
  ^(IMPLEMENTS_CLAUSE type+)
  ;

genericTypeParameterList
  :
  ^(GENERIC_TYPE_PARAM_LIST genericTypeParameter+)
  ;

genericTypeParameter
  :
  ^(IDENT bound?)
  ;

bound
  :
  ^(EXTENDS_BOUND_LIST type+)
  ;

enumTopLevelScope
  :
  ^(ENUM_TOP_LEVEL_SCOPE enumConstant+ classTopLevelScope?)
  ;

enumConstant
  :
  ^(IDENT annotationList arguments? classTopLevelScope?)
  ;

classTopLevelScope
  :
  ^(CLASS_TOP_LEVEL_SCOPE classScopeDeclarations*)
  ;

classScopeDeclarations
  :
  ^(CLASS_INSTANCE_INITIALIZER block)
  |
  ^(CLASS_STATIC_INITIALIZER block)
  |
  ^(FUNCTION_METHOD_DECL modifierList genericTypeParameterList? type IDENT formalParameterList arrayDeclaratorList? throwsClause? block?)
  |
  ^(VOID_METHOD_DECL modifierList genericTypeParameterList? IDENT formalParameterList throwsClause? block?)
  |
  ^(VAR_DECLARATION modifierList type variableDeclaratorList)
  |
  ^(CONSTRUCTOR_DECL modifierList genericTypeParameterList? formalParameterList throwsClause? block)
  | typeDeclaration
  ;

interfaceTopLevelScope
  :
  ^(INTERFACE_TOP_LEVEL_SCOPE interfaceScopeDeclarations*)
  ;

interfaceScopeDeclarations
  :
  ^(FUNCTION_METHOD_DECL modifierList genericTypeParameterList? type IDENT formalParameterList arrayDeclaratorList? throwsClause?)
  |
  ^(VOID_METHOD_DECL modifierList genericTypeParameterList? IDENT formalParameterList throwsClause?)
  // Interface constant declarations have been switched to variable
  // declarations by 'java.g'; the parser has already checked that
  // there's an obligatory initializer.
  |
  ^(VAR_DECLARATION modifierList type variableDeclaratorList)
  | typeDeclaration
  ;

variableDeclaratorList
  :
  ^(VAR_DECLARATOR_LIST variableDeclarator+)
  ;

variableDeclarator
  :
  ^(VAR_DECLARATOR variableDeclaratorId variableInitializer?)
  ;

variableDeclaratorId
  :
  ^(IDENT arrayDeclaratorList?)
  ;

variableInitializer
  :
  arrayInitializer
  | expression
  ;

arrayDeclarator
  :
  LBRACK RBRACK
  ;

arrayDeclaratorList
  :
  ^(ARRAY_DECLARATOR_LIST ARRAY_DECLARATOR*)
  ;

arrayInitializer
  :
  ^(ARRAY_INITIALIZER variableInitializer*)
  ;

throwsClause
  :
  ^(THROWS_CLAUSE qualifiedIdentifier+)
  ;

modifierList
  :
  ^(MODIFIER_LIST modifier*)
  ;

modifier
  :
  PUBLIC
  | PROTECTED
  | PRIVATE
  | STATIC
  | ABSTRACT
  | NATIVE
  | SYNCHRONIZED
  | TRANSIENT
  | VOLATILE
  | STRICTFP
  | localModifier
  ;

localModifierList
  :
  ^(LOCAL_MODIFIER_LIST localModifier*)
  ;

localModifier
  :
  FINAL
  | annotation
  ;

type
  :
  ^(
    TYPE
    (
      primitiveType
      | qualifiedTypeIdent
    )
    arrayDeclaratorList?
   )
  ;

qualifiedTypeIdent
  :
  ^(QUALIFIED_TYPE_IDENT typeIdent+)
  ;

typeIdent
  :
  ^(IDENT genericTypeArgumentList?)
  ;

primitiveType
  :
  BOOLEAN
  | CHAR
  | BYTE
  | SHORT
  | INT
  | LONG
  | FLOAT
  | DOUBLE
  ;

genericTypeArgumentList
  :
  ^(GENERIC_TYPE_ARG_LIST genericTypeArgument+)
  ;

genericTypeArgument
  :
  type
  |
  ^(QUESTION genericWildcardBoundType?)
  ;

genericWildcardBoundType
  :
  ^(EXTENDS type)
  |
  ^(SUPER type)
  ;

formalParameterList
  :
  ^(FORMAL_PARAM_LIST formalParameterStandardDecl* formalParameterVarargDecl?)
  ;

formalParameterStandardDecl
  :
  ^(FORMAL_PARAM_STD_DECL localModifierList type variableDeclaratorId)
  ;

formalParameterVarargDecl
  :
  ^(FORMAL_PARAM_VARARG_DECL localModifierList type variableDeclaratorId)
  ;

qualifiedIdentifier
  :
  IDENT
  |
  ^(DOT qualifiedIdentifier IDENT)
  ;

// ANNOTATIONS

annotationList
  :
  ^(ANNOTATION_LIST annotation*)
  ;

annotation
  :
  ^(AT qualifiedIdentifier annotationInit?)
  ;

annotationInit
  :
  ^(ANNOTATION_INIT_BLOCK annotationInitializers)
  ;

annotationInitializers
  :
  ^(ANNOTATION_INIT_KEY_LIST annotationInitializer+)
  |
  ^(ANNOTATION_INIT_DEFAULT_KEY annotationElementValue)
  ;

annotationInitializer
  :
  ^(IDENT annotationElementValue)
  ;

annotationElementValue
  :
  ^(ANNOTATION_INIT_ARRAY_ELEMENT annotationElementValue*)
  | annotation
  | expression
  ;

annotationTopLevelScope
  :
  ^(ANNOTATION_TOP_LEVEL_SCOPE annotationScopeDeclarations*)
  ;

annotationScopeDeclarations
  :
  ^(ANNOTATION_METHOD_DECL modifierList type IDENT annotationDefaultValue?)
  |
  ^(VAR_DECLARATION modifierList type variableDeclaratorList)
  | typeDeclaration
  ;

annotationDefaultValue
  :
  ^(DEFAULT annotationElementValue)
  ;

// STATEMENTS / BLOCKS

block
  :
  ^(BLOCK_SCOPE blockStatement*)
  ;

blockStatement
  :
  localVariableDeclaration
  | typeDeclaration
  | statement
  ;

localVariableDeclaration
  :
  ^(VAR_DECLARATION localModifierList type variableDeclaratorList)
  ;

statement
  :
  block
  |
  ^(ASSERT expression expression?)
  |
  ^(IF parenthesizedExpression statement statement?)
  |
  ^(FOR forInit forCondition forUpdater statement)
  |
  ^(FOR_EACH localModifierList type IDENT expression statement)
  |
  ^(WHILE parenthesizedExpression statement)
  |
  ^(DO statement parenthesizedExpression)
  |
  ^(TRY block catches? block?) // The second optional block is the optional finally block.
  |
  ^(SWITCH parenthesizedExpression switchBlockLabels)
  |
  ^(SYNCHRONIZED parenthesizedExpression block)
  |
  ^(RETURN expression?)
  |
  ^(THROW expression)
  |
  ^(BREAK IDENT?)
  |
  ^(CONTINUE IDENT?)
  |
  ^(LABELED_STATEMENT IDENT statement)
  | expression
  | SEMI // Empty statement.
  ;

catches
  :
  ^(CATCH_CLAUSE_LIST catchClause+)
  ;

catchClause
  :
  ^(CATCH formalParameterStandardDecl block)
  ;

switchBlockLabels
  :
  ^(SWITCH_BLOCK_LABEL_LIST switchCaseLabel* switchDefaultLabel? switchCaseLabel*)
  ;

switchCaseLabel
  :
  ^(CASE expression blockStatement*)
  ;

switchDefaultLabel
  :
  ^(DEFAULT blockStatement*)
  ;

forInit
  :
  ^(
    FOR_INIT
    (
      localVariableDeclaration
      | expression*
    )?
   )
  ;

forCondition
  :
  ^(FOR_CONDITION expression?)
  ;

forUpdater
  :
  ^(FOR_UPDATE expression*)
  ;

// EXPRESSIONS

evaluate returns [com.sun.jdi.Value value]
  :
  expression 
             {
               $value = $expression.value.getValue();
             }
  ;

parenthesizedExpression returns [ExpressionValue value]
  :
  ^(PARENTESIZED_EXPR expression)
															  {
															    $value = $expression.value;
															  }
  ;

expression returns [ExpressionValue value]
  :
  ^(EXPR expr)
							{
							  $value = $expr.value;
							}
  ;

expr returns [ExpressionValue value]
                                            @init {
                                               latest = ev.getThisObject();
                                            }
  :
  ^(ASSIGN a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $ASSIGN.type);
                                            }
  |
  ^(PLUS_ASSIGN a=expr b=expr)
														  {
															  $value = ev.operation($a.value, $b.value, $PLUS_ASSIGN.type);
														  }
  |
  ^(MINUS_ASSIGN a=expr b=expr)
														  {
															  $value = ev.operation($a.value, $b.value, $MINUS_ASSIGN.type);
														  }
  |
  ^(STAR_ASSIGN a=expr b=expr)
														  {
															  $value = ev.operation($a.value, $b.value, $STAR_ASSIGN.type);
														  }
  |
  ^(DIV_ASSIGN a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $DIV_ASSIGN.type);
														  }
  |
  ^(AND_ASSIGN a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $AND_ASSIGN.type);
                                            }
  |
  ^(OR_ASSIGN a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $OR_ASSIGN.type);
                                            }
  |
  ^(XOR_ASSIGN a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $XOR_ASSIGN.type);
                                            }
  |
  ^(MOD_ASSIGN a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $MOD_ASSIGN.type);
                                            }
  |
  ^(BIT_SHIFT_RIGHT_ASSIGN a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $BIT_SHIFT_RIGHT_ASSIGN.type);
                                            }
  |
  ^(SHIFT_RIGHT_ASSIGN a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $SHIFT_RIGHT_ASSIGN.type);
                                            }
  |
  ^(SHIFT_LEFT_ASSIGN a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $SHIFT_LEFT_ASSIGN.type);
                                            }
  |
  ^(QUESTION test=expr a=expr b=expr)
                                            {
                                              $value = ev.ternaryOperator($test.value, $a.value, $b.value);
                                            }
  |
  ^(LOGICAL_OR a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $LOGICAL_OR.type);
                                            }
  |
  ^(LOGICAL_AND a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $LOGICAL_AND.type);
                                            }
  |
  ^(OR a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $OR.type);
                                            }
  |
  ^(XOR a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $XOR.type);
                                            }
  |
  ^(AND a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $AND.type);
                                            }
  |
  ^(EQUAL a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $EQUAL.type);
                                            }
  |
  ^(NOT_EQUAL a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $NOT_EQUAL.type);
                                            }
  |
  ^(INSTANCEOF expr type)
                                            {
                                              throw new ExpressionException("Operation 'instanceof' is not supported yet. ");
                                            }
  |
  ^(LESS_OR_EQUAL a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $LESS_OR_EQUAL.type);
                                            }
  |
  ^(GREATER_OR_EQUAL a=expr b=expr)
                                            {
                                              $value = ev.operation($a.value, $b.value, $GREATER_OR_EQUAL.type);
                                            }
  |
  ^(BIT_SHIFT_RIGHT a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $BIT_SHIFT_RIGHT.type);
														  }
  |
  ^(SHIFT_RIGHT a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $SHIFT_RIGHT.type);
														  }
  |
  ^(GREATER_THAN a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $GREATER_THAN.type);
														  }
  |
  ^(SHIFT_LEFT a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $SHIFT_LEFT.type);
														  }
  |
  ^(LESS_THAN a=expr b=expr)
														  {
															  $value = ev.operation($a.value, $b.value, $LESS_THAN.type);
														  }
  |
  ^(PLUS a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $PLUS.type);
														  }
  |
  ^(MINUS a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $MINUS.type);
														  }
  |
  ^(STAR a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $STAR.type);
														  }
  |
  ^(DIV a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $DIV.type);
														  }
  |
  ^(MOD a=expr b=expr)
														  {
														    $value = ev.operation($a.value, $b.value, $MOD.type);
														  }
  |
  ^(UNARY_PLUS a=expr)
														  {
														    $value = ev.unaryOperation($a.value, $UNARY_PLUS.type);
														  }
  |
  ^(UNARY_MINUS a=expr)
														  {
														    $value = ev.unaryOperation($a.value, $UNARY_MINUS.type);
														  }
  |
  ^(PRE_INC a=expr)
														  {
														    throw new ExpressionException("Operation '++' is not supported yet. ");
														  }
  |
  ^(PRE_DEC expr)
														  {
														    throw new ExpressionException("Operation '--' is not supported yet. ");
														  }
  |
  ^(POST_INC a=expr)
														  {
														    throw new ExpressionException("Operation '++' is not supported yet. ");
														  }
  |
  ^(POST_DEC expr)
														  {
														    throw new ExpressionException("Operation '--' is not supported yet. ");
														  }
  |
  ^(NOT a=expr)
														  {
														    $value = ev.unaryOperation($a.value, $NOT.type);
														  }
  |
  ^(LOGICAL_NOT a=expr)
														  {
														    $value = ev.unaryOperation($a.value, $LOGICAL_NOT.type);
														  }
  |
  ^(CAST_EXPR type expr)
														  {
														    throw new ExpressionException("Operation 'cast' is not supported yet. ");
														  }
  | primaryExpression 
														  {
														    $value = $primaryExpression.value;
														  }
  ;

primaryExpression returns [ExpressionValue value]
                                             @after {
      													    latest = $value;
                                             }
  :
  ^(
    DOT
    (
      e=primaryExpression
															{
															  $value = $e.value;
															}
      (
        IDENT 
															{
															  if ($start.getParent().getType() != METHOD_CALL) {
    													       $value = ev.getField(latest.getValue(), $IDENT.text);
	      												    if ($value == null) {
															      throw new ExpressionException("Unknown field " + $IDENT.text);
															    }
															  }
															}
        | THIS 
															{
															  $value = ev.getThisObject();
															}
        | SUPER
        | innerNewExpression 
                                             {
                                               throw new ExpressionException("Unable create new instance. Operation not supported yet. ");
                                             }
        | CLASS
      )
      | primitiveType CLASS
      | VOID CLASS
    )
   )
  | parenthesizedExpression 
															{
															  $value = $parenthesizedExpression.value;
															}
  | IDENT 
															{
																if ($start.getParent().getType() != METHOD_CALL) {
																	$value = ev.getLocalVariable($IDENT.text);
																	if ($value == null) {
																	  $value = ev.getField(latest.getValue(), $IDENT.text);
																	}
																	if ($value == null) {
                                                     throw new ExpressionException("Unknown local variable or field " + $IDENT.text);
                                                   }
																} else {
																   $value = ev.getThisObject();
																}

															}
  |
  ^(METHOD_CALL o=primaryExpression genericTypeArgumentList? arguments)
															{

															   String name = $o.start.getChildCount() == 0 ? $o.start.getText() : $o.start.getChild(1).getText();
																$value = ev.invokeMethod(o.value.getValue(), name, $arguments.args);
																
															}
  | explicitConstructorCall 
															{
															  throw new ExpressionException("Unable create new instance. Operation not supported yet. ");
															}
  |
  ^(ARRAY_ELEMENT_ACCESS arr=primaryExpression index=expression)
															{
															  $value = ev.getArrayElement($arr.value.getValue(), $index.value.getValue());
															}
  | literal 
															{
															  $value = $literal.value;
															}
  | newExpression 
															{
															  throw new ExpressionException("Unable create new instance. Operation not supported yet. ");
															}
  | THIS 
															{
															  $value = ev.getThisObject();
															}
  | arrayTypeDeclarator
  | SUPER 
															{
															  $value = ev.getThisObject();
															}
  ;

explicitConstructorCall
  :
  ^(THIS_CONSTRUCTOR_CALL genericTypeArgumentList? arguments)
  |
  ^(SUPER_CONSTRUCTOR_CALL primaryExpression? genericTypeArgumentList? arguments)
  ;

arrayTypeDeclarator
  :
  ^(
    ARRAY_DECLARATOR
    (
      arrayTypeDeclarator
      | qualifiedIdentifier
      | primitiveType
    )
   )
  ;

newExpression
  :
  ^(
    STATIC_ARRAY_CREATOR
    (
      primitiveType newArrayConstruction
      | genericTypeArgumentList? qualifiedTypeIdent newArrayConstruction
    )
   )
  |
  ^(CLASS_CONSTRUCTOR_CALL genericTypeArgumentList? qualifiedTypeIdent arguments classTopLevelScope?)
  ;

innerNewExpression // something like 'InnerType innerType = outer.new InnerType();'
  :
  ^(CLASS_CONSTRUCTOR_CALL genericTypeArgumentList? IDENT arguments classTopLevelScope?)
  ;

newArrayConstruction
  :
  arrayDeclaratorList arrayInitializer
  | expression+ arrayDeclaratorList?
  ;

arguments returns [List < com.sun.jdi.Value > args]
  :
														  {
														    $args = new ArrayList<com.sun.jdi.Value>();
														  }
  ^(
    ARGUMENT_LIST
    (
      e=expression 
															{
															  args.add($e.value.getValue());
															}
    )*
   )
  ;

literal returns [ExpressionValue value]
  :
  HEX_LITERAL 
															{
															  $value = ev.hexValue($HEX_LITERAL.text);
															}
  | OCTAL_LITERAL 
															{
															  $value = ev.octalValue($OCTAL_LITERAL.text);
															}
  | DECIMAL_LITERAL 
															{
															  $value = ev.decimalValue($DECIMAL_LITERAL.text);
															}
  | FLOATING_POINT_LITERAL 
															{
															  $value = ev.floating_pointValue($FLOATING_POINT_LITERAL.text);
															}
  | CHARACTER_LITERAL 
															{
															  $value = ev.charValue($CHARACTER_LITERAL.text);
															}
  | STRING_LITERAL 
															{
															  $value = ev.stringValue($STRING_LITERAL.text);
															}
  | TRUE 
															{
															  $value = ev.booleanValue($TRUE.text);
															}
  | FALSE 
															{
															  $value = ev.booleanValue($FALSE.text);
															}
  | NULL 
															{
															  $value = ev.nullValue();
															}
  ;
