package vanilla.java.collections;

/*
 * Copyright 2011 Peter Lawrey
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifierClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;

public class ClassNodeTest {
    @Test
    public void test() throws IOException {
        ClassReader cr = new ClassReader(ClassNodeTest.class.getName());
        ASMifierClassVisitor cv = new ASMifierClassVisitor(new PrintWriter(System.out));
        cr.accept(cv, 0);
    }
}
