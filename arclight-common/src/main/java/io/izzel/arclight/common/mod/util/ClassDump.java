package io.izzel.arclight.common.mod.util;

import java.io.PrintWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

public class ClassDump {

  public static void print(ClassNode classNode) {
    classNode.accept(new TraceClassVisitor(new PrintWriter(System.out)));
  }
}
