#!/bin/bash
#
# Copyright (c) 2012-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#


javac -g org/eclipse/BreakpointsTest.java
javac -g org/eclipse/BreakpointsByConditionTest.java
javac -g org/eclipse/SuspendPolicyTest.java
javac -g org/eclipse/ThreadDumpTest.java
javac -g org/eclipse/StackFrameDumpTest.java
javac -g org/eclipse/GetValueTest.java
javac -g org/eclipse/EvaluateExpressionTest.java
javac -g org/eclipse/HelloWorld.java

DEBUG_OPT="-Xdebug -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=y"

java ${DEBUG_OPT} org.eclipse.BreakpointsTest
java ${DEBUG_OPT} org.eclipse.BreakpointsByConditionTest
java ${DEBUG_OPT} org.eclipse.ThreadDumpTest
java ${DEBUG_OPT} org.eclipse.SuspendPolicyTest
java ${DEBUG_OPT} org.eclipse.StackFrameDumpTest
java ${DEBUG_OPT} org.eclipse.GetValueTest
java ${DEBUG_OPT} org.eclipse.EvaluateExpressionTest
java ${DEBUG_OPT} org.eclipse.HelloWorld
