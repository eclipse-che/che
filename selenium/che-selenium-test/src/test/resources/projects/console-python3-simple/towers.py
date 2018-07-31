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


def towers(i, start, finish, middle):
    if i > 0:
        towers(i-1, start, middle, finish)
        print('move disk from ', start, ' to ', finish)
        towers  ( i-1, middle, finish, start   )

towers  ( 5, 'X', 'Z', 'Y'      )