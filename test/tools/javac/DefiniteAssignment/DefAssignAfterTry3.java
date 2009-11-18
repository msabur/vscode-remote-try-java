/*
 * Copyright 1999 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/**
 * @test
 * @bug 4240487
 * @summary Verify that we keep track of init/uninits in Try statement
 * without finalizer.
 *
 * @compile/fail DefAssignAfterTry3.java
 */

class E1 extends Exception {}
class E2 extends Exception {}

public class DefAssignAfterTry3 {
    public static void main(String argv[]) {
        boolean t = true;
        E1 se1 = new E1();
        E2 se2 = new E2();

        int i;
        try {
            i = 0;
            if (t)
                throw se1;
            else
                throw se2;
        } catch (E1 e) {
        } catch (E2 e) {
            i = 0;
        }
        // the following line should result in a compile-time error
        // variable i may not have been initialized
        System.out.println(i);
        System.out.println("Error : there should be compile-time errors");
    }
}
