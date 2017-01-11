/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.jdb.server.expression;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** @author andrew00x */
public class Evaluator {
    private static final Logger      LOG             = LoggerFactory.getLogger(Evaluator.class);
    private static final Set<String> PRIMITIVE_TYPES = new HashSet<>(8);

    static {
        PRIMITIVE_TYPES.add("boolean");
        PRIMITIVE_TYPES.add("byte");
        PRIMITIVE_TYPES.add("char");
        PRIMITIVE_TYPES.add("short");
        PRIMITIVE_TYPES.add("int");
        PRIMITIVE_TYPES.add("long");
        PRIMITIVE_TYPES.add("float");
        PRIMITIVE_TYPES.add("double");
    }

    private final VirtualMachine  vm;
    private final ThreadReference thread;

    public Evaluator(VirtualMachine vm, ThreadReference thread) {
        this.vm = vm;
        this.thread = thread;
    }

    private static boolean isPrimitive(Type type) {
        return PRIMITIVE_TYPES.contains(type.name());
    }

    public ThreadReference getThread() {
        return thread;
    }

    public ExpressionValue booleanValue(String text) {
        return value(Boolean.parseBoolean(text));
    }

    public ExpressionValue byteValue(String text) {
        return value(Byte.parseByte(text));
    }

    public ExpressionValue shortValue(String text) {
        return value(Short.parseShort(text));
    }

    public ExpressionValue intValue(String text) {
        return value(Integer.parseInt(text));
    }

    public ExpressionValue hexValue(String text) {
        int length = text.length();
        if (length > 1 && ('l' == text.charAt(length - 1) || 'L' == text.charAt(length - 1))) {
            return value(Long.parseLong(text.substring(2, length - 1), 16));
        }
        return value(Integer.parseInt(text.substring(2), 16));
    }

    public ExpressionValue octalValue(String text) {
        int length = text.length();
        if (length > 1 && ('l' == text.charAt(length - 1) || 'L' == text.charAt(length - 1))) {
            return value(Long.parseLong(text.substring(1, length - 1), 8));
        }
        return value(Integer.parseInt(text.substring(1), 8));
    }

    public ExpressionValue decimalValue(String text) {
        int length = text.length();
        if (length > 1 && ('l' == text.charAt(length - 1) || 'L' == text.charAt(length - 1))) {
            return value(Long.parseLong(text.substring(0, length - 1)));
        }
        return value(Integer.parseInt(text));
    }

    public ExpressionValue floating_pointValue(String text) {
        int length = text.length();
        if ('f' == text.charAt(length - 1) || 'F' == text.charAt(length - 1)) {
            return value(Float.parseFloat(text));
        }
        return value(Double.parseDouble(text));
    }

//

    public ExpressionValue charValue(String text) {
        return value(text.charAt(0));
    }

    public ExpressionValue stringValue(String text) {
        return value(text.substring(1, text.length() - 1));
    }

    public ExpressionValue nullValue() {
        return new ReadOnlyValue(null);
    }

    public ExpressionValue value(boolean v) {
        return new ReadOnlyValue(vm.mirrorOf(v));
    }

    public ExpressionValue value(byte v) {
        return new ReadOnlyValue(vm.mirrorOf(v));
    }

    public ExpressionValue value(short v) {
        return new ReadOnlyValue(vm.mirrorOf(v));
    }

    public ExpressionValue value(int v) {
        return new ReadOnlyValue(vm.mirrorOf(v));
    }

    public ExpressionValue value(long v) {
        return new ReadOnlyValue(vm.mirrorOf(v));
    }

    public ExpressionValue value(float v) {
        return new ReadOnlyValue(vm.mirrorOf(v));
    }

//

    public ExpressionValue value(double v) {
        return new ReadOnlyValue(vm.mirrorOf(v));
    }

/*
   public ExpressionValue getField(String text)
   {
      ExpressionValue value = null;
      try
      {
         StackFrame frame = thread.frame(0);
         ObjectReference object = frame.thisObject();
         if (object == null)
         {
            ReferenceType type = frame.location().declaringType();
            Field field = type.fieldByName(text);
            if (field != null)
            {
               value = new StaticValue(type, field);
            }
         }
         else
         {
            Field field = object.referenceType().fieldByName(text);
            if (field != null)
            {
               value = new InstanceValue(object, field);
            }
         }
      }
      catch (IncompatibleThreadStateException e)
      {
         throw new ExpressionException(e.getMessage(), e);
      }
      catch (InvalidStackFrameException e)
      {
         throw new ExpressionException(e.getMessage(), e);
      }
      catch (ClassNotPreparedException e)
      {
         throw new ExpressionException(e.getMessage(), e);
      }
      LOG.debug("GET field {} {} ", text, value);
      return value;
   }
*/

    public ExpressionValue value(char v) {
        return new ReadOnlyValue(vm.mirrorOf(v));
    }

    public ExpressionValue value(String v) {
        return new ReadOnlyValue(vm.mirrorOf(v));
    }

    public ExpressionValue getThisObject() {
        try {
            return new ReadOnlyValue(thread.frame(0).thisObject());
        } catch (IncompatibleThreadStateException e) {
            throw new ExpressionException(e.getMessage(), e);
        }
    }

    public ExpressionValue getField(Value parent, String name) {
        if (!(parent instanceof ObjectReference)) {
            throw new ExpressionException("Value is not object. Cannot invoke method " + name);
        }
        ExpressionValue value = null;
        try {
            ObjectReference object = (ObjectReference)parent;
            Field field = object.referenceType().fieldByName(name);
            if (field != null) {
                value = new InstanceValue(object, field);
            }
        } catch (ClassNotPreparedException e) {
            throw new ExpressionException(e.getMessage(), e);
        }
        LOG.debug("GET field {} {} ", name, value);
        return value;
    }

    public ExpressionValue getLocalVariable(String text) {
        ExpressionValue value = null;
        try {
            StackFrame frame = thread.frame(0);
            LocalVariable var = frame.visibleVariableByName(text);
            if (var != null) {
                value = new LocalValue(thread, var);
            }
        } catch (IncompatibleThreadStateException | AbsentInformationException | InvalidStackFrameException | NativeMethodException e) {
            throw new ExpressionException(e.getMessage(), e);
        }
        LOG.debug("GET local variable {} {} ", text, value);
        return value;
    }

    public ExpressionValue getArrayElement(Value arrayValue, Value indexValue) {
        if (!(arrayValue instanceof ArrayReference)) {
            throw new ExpressionException("Cannot get array element. Object is not array. ");
        }
        if (!(indexValue instanceof IntegerValue)) {
            throw new ExpressionException("Invalid array index. ");
        }
        return new ArrayElement((ArrayReference)arrayValue, ((IntegerValue)indexValue).value());
    }

    private Method findMethod(List<Method> methods, List<Value> arguments) {
        Method m = null;
        for (Method mm : methods) {
            List<Type> argumentTypes;
            try {
                argumentTypes = mm.argumentTypes();
            } catch (ClassNotLoadedException e) {
                continue;
            }

            ARGUMENT_MATCHING argumentMatching = argumentsMatching(argumentTypes, arguments);
            if (argumentMatching == ARGUMENT_MATCHING.MATCH) {
                m = mm;
                break;
            } else if (argumentMatching == ARGUMENT_MATCHING.ASSIGNABLE) {
                if (m == null) {
                    m = mm;
                } else {
                    throw new ExpressionException("Multiple methods with name " + mm.name() + " matched to specified arguments. ");
                }
            }
        }
        return m;
    }

    private ARGUMENT_MATCHING argumentsMatching(List<Type> argumentTypes, List<Value> arguments) {
        if (argumentTypes.size() == arguments.size()) {
            Iterator<Value> argumentIterator = arguments.iterator();
            Iterator<Type> argumentTypesIterator = argumentTypes.iterator();
            ARGUMENT_MATCHING result = ARGUMENT_MATCHING.MATCH;
            while (argumentIterator.hasNext() && result != ARGUMENT_MATCHING.NOT_MATCH) {
                Value argumentValue = argumentIterator.next();
                Type argumentType = argumentTypesIterator.next();
                if (argumentValue == null) {
                    if (isPrimitive(argumentType)) {
                        // Null may not be used as value if argument type is primitive.
                        result = ARGUMENT_MATCHING.NOT_MATCH;
                    }
                } else {
                    if (!(argumentValue.type().equals(argumentType))) {
                        result = isAssignable(argumentValue.type(), argumentType)
                                 ? ARGUMENT_MATCHING.ASSIGNABLE : ARGUMENT_MATCHING.NOT_MATCH;
                    }
                }
            }
            return result;
        }
        return ARGUMENT_MATCHING.NOT_MATCH;
    }

    private boolean isAssignable(Type from, Type to) {
        if (from.equals(to)) {
            return true;
        }

        if (from instanceof BooleanType) {
            return to instanceof BooleanType;
        }
        if (to instanceof BooleanType) {
            return false;
        }

        if (from instanceof PrimitiveType) {
            return to instanceof PrimitiveType;
        }
        if (to instanceof PrimitiveType) {
            return false;
        }

        if (from instanceof ArrayType) {
            if (to instanceof ArrayType) {
                Type fromArrayComponent;
                Type toArrayComponent;
                try {
                    fromArrayComponent = ((ArrayType)from).componentType();
                    toArrayComponent = ((ArrayType)to).componentType();
                } catch (ClassNotLoadedException e) {
                    return false;
                }
                if (fromArrayComponent instanceof PrimitiveType) {
                    return fromArrayComponent.equals(toArrayComponent);
                }
                return !(toArrayComponent instanceof PrimitiveType) && isAssignable(fromArrayComponent, toArrayComponent);
            }
            return to.name().equals("java.lang.Object");
        }

        if (from instanceof ClassType) {
            ClassType superClass = ((ClassType)from).superclass();
            if (superClass != null && isAssignable(superClass, to)) {
                return true;
            }
            for (InterfaceType interfaceType : ((ClassType)from).interfaces()) {
                if (isAssignable(interfaceType, to)) {
                    return true;
                }
            }
        }

        for (InterfaceType interfaceType : ((InterfaceType)from).subinterfaces()) {
            if (isAssignable(interfaceType, to)) {
                return true;
            }
        }

        return false;
    }

    public ExpressionValue invokeMethod(Value value, String name, List<Value> arguments) {
        if (!(value instanceof ObjectReference)) {
            throw new ExpressionException("Value is not object. Cannot invoke method " + name);
        }
        ObjectReference object = (ObjectReference)value;
        ReferenceType type = object.referenceType();
        List<Method> methods = type.methodsByName(name);
        Method method = findMethod(methods, arguments);
        if (method == null) {
            throw new ExpressionException("No method with name " + name + " matched to specified arguments for " + type.name());
        }
        try {
            return new ReadOnlyValue(object.invokeMethod(thread, method, arguments, 0));
        } catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException | InvocationException e) {
            throw new ExpressionException(e.getMessage(), e);
        }
    }

    public ExpressionValue unaryOperation(ExpressionValue expression, int op) {
        Value value = expression.getValue();
        if (!(value instanceof PrimitiveValue)) {
            throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + value);
        }

        if (value instanceof BooleanValue) {
            if (op == JavaParser.LOGICAL_NOT) {
                return value(!((BooleanValue)value).value());
            }
        }

        // TODO add support for other unary operations
        PrimitiveValue pv = (PrimitiveValue)value;
        if (pv instanceof DoubleValue) {
            switch (op) {
                case JavaParser.UNARY_PLUS:
                    return value(pv.doubleValue());
                case JavaParser.UNARY_MINUS:
                    return value(-pv.doubleValue());
                default:
                    throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + value);
            }
        }

        if (pv instanceof FloatValue) {
            switch (op) {
                case JavaParser.UNARY_PLUS:
                    return value(pv.floatValue());
                case JavaParser.UNARY_MINUS:
                    return value(-pv.floatValue());
                default:
                    throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + value);
            }

        }

        if (pv instanceof LongValue) {
            switch (op) {
                case JavaParser.NOT:
                    return value(~pv.longValue());
                case JavaParser.UNARY_PLUS:
                    return value(pv.longValue());
                case JavaParser.UNARY_MINUS:
                    return value(-pv.longValue());
                default:
                    throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + value);
            }
        }

        switch (op) {
            case JavaParser.NOT:
                return value(~pv.intValue());
            case JavaParser.UNARY_PLUS:
                return value(pv.intValue());
            case JavaParser.UNARY_MINUS:
                return value(-pv.intValue());
            default:
                throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + value);
        }
    }

    public ExpressionValue operation(ExpressionValue leftExpression, ExpressionValue rightExpression, int op) {
        if (JavaParser.ASSIGN == op) {
            leftExpression.setValue(rightExpression.getValue());
            return leftExpression;
        }

        Value leftValue = leftExpression.getValue();
        Value rightValue = rightExpression.getValue();
        if (leftValue instanceof StringReference || rightValue instanceof StringReference) {
            if (JavaParser.PLUS == op) {
                return value(valueToString(leftValue) + valueToString(rightValue));
            }
        }

        if (leftValue instanceof ObjectReference || rightValue instanceof ObjectReference) {
            switch (op) {
                case JavaParser.EQUAL:
                    return value(leftValue != null ? leftValue.equals(rightValue) : rightValue == null);
                case JavaParser.NOT_EQUAL:
                    return value(!(leftValue != null ? leftValue.equals(rightValue) : rightValue == null));
                default:
                    throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + leftValue
                                                  + " and " + rightValue);
            }
        }

        if (leftValue == null || rightValue == null) {
            // Neither one is object and operation is not assignation.
            throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + leftValue
                                          + " and " + rightValue);
        }

        PrimitiveValue lp = (PrimitiveValue)leftValue;
        PrimitiveValue rp = (PrimitiveValue)rightValue;

        if (lp instanceof BooleanValue && rp instanceof BooleanValue) {
            switch (op) {
                case JavaParser.LOGICAL_AND:
                    return value(lp.booleanValue() && rp.booleanValue());
                case JavaParser.LOGICAL_OR:
                    return value(lp.booleanValue() || rp.booleanValue());
                case JavaParser.EQUAL:
                    return value(lp.booleanValue() == rp.booleanValue());
                case JavaParser.NOT_EQUAL:
                    return value(lp.booleanValue() != rp.booleanValue());
                case JavaParser.OR:
                    return value(lp.booleanValue() | rp.booleanValue());
                case JavaParser.XOR:
                    return value(lp.booleanValue() ^ rp.booleanValue());
                case JavaParser.AND:
                    return value(lp.booleanValue() & rp.booleanValue());
                case JavaParser.OR_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.booleanValue() | rp.booleanValue()));
                    return leftExpression;
                case JavaParser.XOR_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.booleanValue() ^ rp.booleanValue()));
                    return leftExpression;
                case JavaParser.AND_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.booleanValue() & rp.booleanValue()));
                    return leftExpression;
                default:
                    throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + leftValue
                                                  + " and " + rightValue);
            }
        }

        switch (op) {
            case JavaParser.EQUAL:
                return value(lp.doubleValue() == rp.doubleValue());
            case JavaParser.NOT_EQUAL:
                return value(lp.doubleValue() != rp.doubleValue());
            case JavaParser.GREATER_THAN:
                return value(lp.doubleValue() > rp.doubleValue());
            case JavaParser.LESS_THAN:
                return value(lp.doubleValue() < rp.doubleValue());
            case JavaParser.GREATER_OR_EQUAL:
                return value(lp.doubleValue() >= rp.doubleValue());
            case JavaParser.LESS_OR_EQUAL:
                return value(lp.doubleValue() <= rp.doubleValue());
        }

        if (lp instanceof DoubleValue || rp instanceof DoubleValue) {
            switch (op) {
                case JavaParser.PLUS:
                    return value(lp.doubleValue() + rp.doubleValue());
                case JavaParser.MINUS:
                    return value(lp.doubleValue() - rp.doubleValue());
                case JavaParser.STAR:
                    return value(lp.doubleValue() * rp.doubleValue());
                case JavaParser.DIV:
                    return value(lp.doubleValue() / rp.doubleValue());
                case JavaParser.MOD:
                    return value(lp.doubleValue() % rp.doubleValue());
                case JavaParser.PLUS_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.doubleValue() + rp.doubleValue()));
                    return leftExpression;
                case JavaParser.MINUS_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.doubleValue() - rp.doubleValue()));
                    return leftExpression;
                case JavaParser.STAR_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.doubleValue() * rp.doubleValue()));
                    return leftExpression;
                case JavaParser.DIV_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.doubleValue() / rp.doubleValue()));
                    return leftExpression;
                case JavaParser.MOD_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.doubleValue() % rp.doubleValue()));
                    return leftExpression;
                default:
                    throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + leftValue
                                                  + " and " + rightValue);
            }
        }
        if (lp instanceof FloatValue || rp instanceof FloatValue) {
            switch (op) {
                case JavaParser.PLUS:
                    return value(lp.floatValue() + rp.floatValue());
                case JavaParser.MINUS:
                    return value(lp.floatValue() - rp.floatValue());
                case JavaParser.STAR:
                    return value(lp.floatValue() * rp.floatValue());
                case JavaParser.DIV:
                    return value(lp.floatValue() / rp.floatValue());
                case JavaParser.MOD:
                    return value(lp.floatValue() % rp.floatValue());
                case JavaParser.PLUS_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.floatValue() + rp.floatValue()));
                    return leftExpression;
                case JavaParser.MINUS_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.floatValue() - rp.floatValue()));
                    return leftExpression;
                case JavaParser.STAR_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.floatValue() * rp.longValue()));
                    return leftExpression;
                case JavaParser.DIV_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.floatValue() / rp.floatValue()));
                    return leftExpression;
                case JavaParser.MOD_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.floatValue() % rp.floatValue()));
                    return leftExpression;
                default:
                    throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + leftValue
                                                  + " and " + rightValue);
            }
        }
        if (lp instanceof LongValue || rp instanceof LongValue) {
            switch (op) {
                case JavaParser.PLUS:
                    return value(lp.longValue() + rp.longValue());
                case JavaParser.MINUS:
                    return value(lp.longValue() - rp.longValue());
                case JavaParser.STAR:
                    return value(lp.longValue() * rp.longValue());
                case JavaParser.DIV:
                    return value(lp.longValue() / rp.longValue());
                case JavaParser.MOD:
                    return value(lp.longValue() % rp.longValue());
                case JavaParser.SHIFT_LEFT:
                    return value(lp.longValue() << rp.longValue());
                case JavaParser.SHIFT_RIGHT:
                    return value(lp.longValue() >> rp.longValue());
                case JavaParser.BIT_SHIFT_RIGHT:
                    return value(lp.longValue() >>> rp.longValue());
                case JavaParser.OR:
                    return value(lp.longValue() | rp.longValue());
                case JavaParser.XOR:
                    return value(lp.longValue() ^ rp.longValue());
                case JavaParser.AND:
                    return value(lp.longValue() & rp.longValue());
                case JavaParser.PLUS_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() + rp.longValue()));
                    return leftExpression;
                case JavaParser.MINUS_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() - rp.longValue()));
                    return leftExpression;
                case JavaParser.STAR_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() * rp.longValue()));
                    return leftExpression;
                case JavaParser.DIV_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() / rp.longValue()));
                    return leftExpression;
                case JavaParser.OR_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() | rp.longValue()));
                    return leftExpression;
                case JavaParser.XOR_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() ^ rp.longValue()));
                    return leftExpression;
                case JavaParser.AND_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() & rp.longValue()));
                    return leftExpression;
                case JavaParser.SHIFT_LEFT_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() << rp.longValue()));
                    return leftExpression;
                case JavaParser.SHIFT_RIGHT_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() >> rp.longValue()));
                    return leftExpression;
                case JavaParser.BIT_SHIFT_RIGHT_ASSIGN:
                    leftExpression.setValue(vm.mirrorOf(lp.longValue() >>> rp.longValue()));
                    return leftExpression;
                default:
                    throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + leftValue
                                                  + " and " + rightValue);
            }
        }
        switch (op) {
            case JavaParser.PLUS:
                return value(lp.intValue() + rp.intValue());
            case JavaParser.MINUS:
                return value(lp.intValue() - rp.intValue());
            case JavaParser.STAR:
                return value(lp.intValue() * rp.intValue());
            case JavaParser.DIV:
                return value(lp.intValue() / rp.intValue());
            case JavaParser.MOD:
                return value(lp.intValue() % rp.intValue());
            case JavaParser.SHIFT_LEFT:
                return value(lp.intValue() << rp.intValue());
            case JavaParser.SHIFT_RIGHT:
                return value(lp.intValue() >> rp.intValue());
            case JavaParser.BIT_SHIFT_RIGHT:
                return value(lp.intValue() >>> rp.intValue());
            case JavaParser.OR:
                return value(lp.intValue() | rp.intValue());
            case JavaParser.XOR:
                return value(lp.intValue() ^ rp.intValue());
            case JavaParser.AND:
                return value(lp.intValue() & rp.intValue());
            case JavaParser.PLUS_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() + rp.intValue()));
                return leftExpression;
            case JavaParser.MINUS_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() - rp.intValue()));
                return leftExpression;
            case JavaParser.STAR_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() * rp.intValue()));
                return leftExpression;
            case JavaParser.DIV_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() / rp.intValue()));
                return leftExpression;
            case JavaParser.OR_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() | rp.intValue()));
                return leftExpression;
            case JavaParser.XOR_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() ^ rp.intValue()));
                return leftExpression;
            case JavaParser.AND_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() & rp.intValue()));
                return leftExpression;
            case JavaParser.SHIFT_LEFT_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() << rp.intValue()));
                return leftExpression;
            case JavaParser.SHIFT_RIGHT_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() >> rp.intValue()));
                return leftExpression;
            case JavaParser.BIT_SHIFT_RIGHT_ASSIGN:
                leftExpression.setValue(vm.mirrorOf(lp.intValue() >>> rp.intValue()));
                return leftExpression;
            default:
                throw new ExpressionException("Unsupported operation " + JavaParser.tokenNames[op] + " for " + leftValue
                                              + " and " + rightValue);
        }
    }

    public ExpressionValue ternaryOperator(ExpressionValue testCondition, ExpressionValue value1, ExpressionValue value2) {
        Value test = testCondition.getValue();
        if (!(test instanceof BooleanValue)) {
            throw new ExpressionException("Invalid ternary operator. ");
        }
        return ((BooleanValue)test).booleanValue() ? value1 : value2;
    }

    private String valueToString(Value value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof StringReference) {
            return ((StringReference)value).value();
        }
        if (value instanceof ObjectReference) {
            StringReference stringValue = (StringReference)invokeMethod(value, "toString", Collections.<Value>emptyList()).getValue();
            // XXX Can result be null ?
            return stringValue == null ? "null" : stringValue.value();
        }
        return value.toString();
    }

    private enum ARGUMENT_MATCHING {
        MATCH, ASSIGNABLE, NOT_MATCH
    }
}
