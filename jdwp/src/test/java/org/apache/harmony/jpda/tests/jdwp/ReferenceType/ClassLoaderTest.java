/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Anatoly F. Bondarenko
 */

/**
 * Created on 16.02.2005
 */
package org.apache.harmony.jpda.tests.jdwp.ReferenceType;

import org.apache.harmony.jpda.tests.framework.jdwp.CommandPacket;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPCommands;
import org.apache.harmony.jpda.tests.framework.jdwp.ReplyPacket;
import org.apache.harmony.jpda.tests.jdwp.share.JDWPSyncTestCase;
import org.apache.harmony.jpda.tests.share.JPDADebuggeeSynchronizer;


/**
 * JDWP Unit test for ReferenceType.ClassLoader command.
 */
public class ClassLoaderTest extends JDWPSyncTestCase {

    static final int testStatusPassed = 0;
    static final int testStatusFailed = -1;
    static final String thisCommandName = "ReferenceType.ClassLoader command";
    static final String debuggeeSignature = "Lorg/apache/harmony/jpda/tests/jdwp/share/debuggee/HelloWorld;";

    protected String getDebuggeeClassName() {
        return "org.apache.harmony.jpda.tests.jdwp.share.debuggee.HelloWorld";
    }

    /**
     * This testcase exercises ReferenceType.ClassLoader command.
     * <BR>The test starts HelloWorld debuggee, requests referenceTypeId
     * for it by VirtualMachine.ClassesBySignature command, then
     * performs ReferenceType.ClassLoader command and checks that command
     * returns reply with some non-zero classLoaderID without any ERROR
     */
    public void testClassLoader001() {
        classLoaderTest("testClassLoader001", debuggeeSignature, false);
    }

    /**
     * Same as testClassLoader001, but expecting a zero classLoaderId for
     * a boot classpath class.
     * @see <a href="http://docs.oracle.com/javase/7/docs/platform/jpda/jdwp/jdwp-protocol.html#JDWP_ReferenceType_ClassLoader">ReferenceType.ClassLoader</a>
     * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/jpda/jdwp-spec.html">Common Data Types</a>
     */
    public void testClassLoader002() {
        classLoaderTest("testClassLoader002", "Ljava/lang/Object;", true);
    }

    /**
     * Implementation of the tests, using the given parameters.
     */
    private void classLoaderTest(String thisTestName, String signature, boolean expectZero) {
        logWriter.println("==> " + thisTestName + " for " + thisCommandName + ": START...");
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_READY);

        long refTypeID = getClassIDBySignature(signature);

        logWriter.println("=> Debuggee class = " + signature);
        logWriter.println("=> referenceTypeID for Debuggee class = " + refTypeID);
        logWriter.println("=> CHECK1: send " + thisCommandName + " and check reply for ERROR...");

        CommandPacket classLoaderCommand = new CommandPacket(
                JDWPCommands.ReferenceTypeCommandSet.CommandSetID,
                JDWPCommands.ReferenceTypeCommandSet.ClassLoaderCommand);
        classLoaderCommand.setNextValueAsReferenceTypeID(refTypeID);

        ReplyPacket classLoaderReply = debuggeeWrapper.vmMirror.performCommand(classLoaderCommand);
        classLoaderCommand = null;
        checkReplyPacket(classLoaderReply, thisCommandName);

        long returnedClassLoaderID = classLoaderReply.getNextValueAsObjectID();
        if (expectZero) {
            assertTrue("Should be boot classpath classloader", returnedClassLoaderID == 0);
        } else {
            assertTrue("Should not be boot classpath classloader", returnedClassLoaderID != 0);
        }
        logWriter.println("=> CHECK1: PASSED: Returned classLoaderID = " + returnedClassLoaderID);

        assertAllDataRead(classLoaderReply);

        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
        logWriter.println("==> " + thisTestName + " for " + thisCommandName + ": FINISH");
    }
}
