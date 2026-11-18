package com.shatteredpixel.shatteredpixeldungeon.events.processor;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 编译期生成图鉴「效果百科」用的 Buff 类名清单 BuffClassIndex。
 * <br>
 * 为什么不在运行时扫描：安卓的 APK 里类都打进了 dex，没有一个个 .class 文件可列，
 * 按包名扫类路径在安卓上一个类也扫不到，效果百科页就是空的。
 * 在编译期列出来、生成成一个普通的类，桌面和安卓走的是同一条路，新增 Buff 也不用手动登记。
 * <br>
 * 这个处理器不能声明成 Gradle 的增量处理器：增量编译时只有改动过的文件会交给处理器，
 * 生成的清单会漏掉没改动的那些 Buff。
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
public class BuffClassIndexProcessor extends AbstractProcessor {

    private static final String BUFF = "com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff";
    private static final String INDEX_PACKAGE = "com.shatteredpixel.shatteredpixeldungeon.custom.utils";
    private static final String INDEX_NAME = "BuffClassIndex";

    private boolean generated = false;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (generated || roundEnv.processingOver()) {
            return false;
        }

        // 只在编译 Buff 自己所在的模块时生成：desktop 模块也挂了这个处理器，那里不该再生成一份
        TypeElement buff = null;
        for (Element root : roundEnv.getRootElements()) {
            if (root instanceof TypeElement && ((TypeElement) root).getQualifiedName().contentEquals(BUFF)) {
                buff = (TypeElement) root;
                break;
            }
        }
        if (buff == null) {
            return false;
        }

        Types types = processingEnv.getTypeUtils();
        TypeMirror buffType = types.erasure(buff.asType());
        // 按类名排序，生成结果不随编译顺序变化
        TreeSet<String> names = new TreeSet<>();
        for (Element root : roundEnv.getRootElements()) {
            collect(root, buffType, names);
        }

        try {
            write(names);
            generated = true;
        } catch (IOException e) {
            // 清单生成不出来就让编译失败，不能悄悄地带着一个空的效果百科出包
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate " + INDEX_NAME + ": " + e.getMessage());
        }

        // 不认领任何注解，其他处理器照常工作
        return false;
    }

    // 匿名类和方法里的局部类不会出现在这里，效果百科本来也不收它们
    private void collect(Element element, TypeMirror buffType, Set<String> names) {
        if (!(element instanceof TypeElement)) {
            return;
        }
        TypeElement type = (TypeElement) element;
        Types types = processingEnv.getTypeUtils();
        TypeMirror erased = types.erasure(type.asType());
        if (type.getKind() == ElementKind.CLASS
                && !type.getModifiers().contains(Modifier.ABSTRACT)
                && !types.isSameType(erased, buffType)
                && types.isSubtype(erased, buffType)) {
            // 二进制名（内部类用 $ 连接），运行时可以直接 Class.forName
            names.add(processingEnv.getElementUtils().getBinaryName(type).toString());
        }
        for (Element enclosed : type.getEnclosedElements()) {
            collect(enclosed, buffType, names);
        }
    }

    private void write(Set<String> names) throws IOException {
        try (Writer w = processingEnv.getFiler().createSourceFile(INDEX_PACKAGE + "." + INDEX_NAME).openWriter()) {
            w.write("package " + INDEX_PACKAGE + ";\n\n");
            w.write("/**\n");
            w.write(" * Auto-generated list of every concrete Buff class (binary names).\n");
            w.write(" * Do not modify this file manually - it is regenerated at compile time by BuffClassIndexProcessor.\n");
            w.write(" */\n");
            w.write("public final class " + INDEX_NAME + " {\n\n");
            w.write("    private " + INDEX_NAME + "() {}\n\n");
            w.write("    public static final String[] NAMES = {\n");
            for (String name : names) {
                w.write("        \"" + name + "\",\n");
            }
            w.write("    };\n");
            w.write("}\n");
        }
    }
}
