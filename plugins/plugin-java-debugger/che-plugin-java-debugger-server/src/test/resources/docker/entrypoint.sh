#!/bin/bash
#
# Copyright (c) 2012-2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#


javac -g org/eclipse/BreakpointsTest.java
javac -g org/eclipse/BreakpointsByConditionTest.java
javac -g org/eclipse/ThreadDumpTest.java
javac -g org/eclipse/StackFrameDumpTest.java
javac -g org/eclipse/GetValueTest.java
javac -g org/eclipse/EvaluateExpressionTest.java
javac -g org/eclipse/HelloWorld.java

DEBUG_OPT="-Xdebug -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=y"

java ${DEBUG_OPT} org.eclipse.BreakpointsTest
java ${DEBUG_OPT} org.eclipse.BreakpointsByConditionTest

java ${DEBUG_OPT} org.eclipse.ThreadDumpTest
java ${DEBUG_OPT} org.eclipse.StackFrameDumpTest
java ${DEBUG_OPT} org.eclipse.GetValueTest
java ${DEBUG_OPT} org.eclipse.EvaluateExpressionTest
java ${DEBUG_OPT} org.eclipse.HelloWorld
