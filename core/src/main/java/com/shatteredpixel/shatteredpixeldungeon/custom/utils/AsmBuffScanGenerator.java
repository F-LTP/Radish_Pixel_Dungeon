package com.shatteredpixel.shatteredpixeldungeon.custom.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * TODO 若要使用该脚本，请将下方ASM导入core，然后通过无头ASM构建获得BuffScanner.java<br>
 * TODO 然后将类似于，Buff$Buff改为，Buff.Buff<br>
 * TODO Import可以全部移除，因为已经是全包名<br>
 * TODO 若发现，Buff$1，Buff$2，则直接删除<br>
 * TODO 如发现私有buff，抽象等buff，则自行考虑做取舍<br>
 * <br>
 * 无头ASM依赖<br>
 * implementation 'org.ow2.asm:asm:9.7'<br>
 * implementation 'org.ow2.asm:asm-tree:9.7'
 */

public class AsmBuffScanGenerator {
    private static final String BUFF_INTERNAL = "com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Buff";
    private static final String CLASS_ROOT = "E:\\PD-Dev\\Radish_Pixel_Dungeon-C\\core\\build\\classes\\java\\main";
    private static final String OUTPUT_FILE = "BuffScanner.java";
    private static final String TARGET_PACKAGE = "com.shatteredpixel.shatteredpixeldungeon.custom.utils";

    private static final Set<String> buffClassInternalNames = new TreeSet<>();
    private static final Map<String, ClassNode> classCache = new HashMap<>();

    public static void main(String[] args) throws IOException {
        File rootDir = new File(CLASS_ROOT);
        System.out.println("开始扫描目录：" + rootDir.getAbsolutePath());
        scanDirectory(rootDir);

        Set<String> imports = new TreeSet<>();
        List<String> classRefs = new ArrayList<>();
        for (String internal : buffClassInternalNames) {
            String fullName = internal.replace("/", ".");
            imports.add("import " + fullName + ";");
            classRefs.add(fullName);
        }

        try (PrintWriter w = new PrintWriter(new FileOutputStream(OUTPUT_FILE))) {
            w.println("package " + TARGET_PACKAGE + ";");
            w.println();
            for (String im : imports) {
                w.println(im);
            }
            w.println("import java.util.ArrayList;");
            w.println();
            w.println("public class BuffScanner {");
            w.println("    public static ArrayList<Class<? extends Buff>> getAllBuffClasses() {");
            w.println("        ArrayList<Class<? extends Buff>> buffClasses = new ArrayList<>();");
            w.println();
            for (String cls : classRefs) {
                w.printf("        buffClasses.add(%s.class);%n", cls);
            }
            w.println();
            w.println("        return buffClasses;");
            w.println("    }");
            w.println("}");
        }
        System.out.println("扫描结束，Buff数量：" + buffClassInternalNames.size());
    }

    private static void scanDirectory(File dir) throws IOException {
        File[] listFiles = dir.listFiles();
        if (listFiles == null) return;
        for (File f : listFiles) {
            if (f.isDirectory()) {
                scanDirectory(f);
            } else if (f.getName().endsWith(".class")) {
                byte[] bytes = readFileBytes(f);
                ClassReader cr = new ClassReader(bytes);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
                // 加入缓存
                classCache.put(cn.name, cn);
                if (isBuffSubclass(cn)) {
                    buffClassInternalNames.add(cn.name);
                    System.out.println("找到Buff子类：" + cn.name);
                }
            }
        }
    }

    private static byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            return buffer;
        }
    }

    private static boolean isBuffSubclass(ClassNode cn) throws IOException {
        String superName = cn.superName;
        while (superName != null && !superName.equals("java/lang/Object")) {
            if (superName.equals(BUFF_INTERNAL)) {
                return true;
            }
            // 优先从缓存拿，不再重复读文件
            ClassNode supNode = classCache.get(superName);
            if (supNode == null) {
                File supFile = new File(CLASS_ROOT, superName + ".class");
                if (!supFile.exists()) {
                    break;
                }
                byte[] supBytes = readFileBytes(supFile);
                ClassReader crSup = new ClassReader(supBytes);
                supNode = new ClassNode();
                crSup.accept(supNode, 0);
                classCache.put(superName, supNode);
            }
            superName = supNode.superName;
        }
        return false;
    }
}
