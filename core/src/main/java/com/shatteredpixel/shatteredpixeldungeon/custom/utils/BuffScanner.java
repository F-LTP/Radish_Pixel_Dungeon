package com.shatteredpixel.shatteredpixeldungeon.custom.utils;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 基于 Java 原生反射的 Buff 扫描器。
 * <br>
 * 在运行时扫描 com.shatteredpixel.shatteredpixeldungeon 包（及其子包）下的所有类，
 * 找出 Buff 的全部非抽象、可实例化子类，并只保留 icon() 有实际图标的 Buff。
 * <br>
 * 相比旧的 ASM 方案：无需编译期生成代码、无硬编码绝对路径、
 * 新增 Buff 类后无需重新生成列表，天然覆盖全部 Buff。
 */
public class BuffScanner {

	private static final String BASE_PACKAGE = "com.shatteredpixel.shatteredpixeldungeon";
	private static final String BASE_PATH = BASE_PACKAGE.replace('.', '/');

	private static ArrayList<Class<? extends Buff>> cachedBuffClasses = null;

	public static ArrayList<Class<? extends Buff>> getAllBuffClasses() {
		if (cachedBuffClasses == null) {
			cachedBuffClasses = scanBuffClasses();
		}
		return new ArrayList<>(cachedBuffClasses);
	}

	private static ArrayList<Class<? extends Buff>> scanBuffClasses() {
		Set<String> classNames = new HashSet<>();
		try {
			Enumeration<URL> resources = BuffScanner.class.getClassLoader().getResources(BASE_PATH);
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					scanDirectory(new File(URLDecoder.decode(url.getFile(), "UTF-8")), BASE_PACKAGE, classNames);
				} else if ("jar".equals(protocol)) {
					scanJar(url, classNames);
				}
			}
		} catch (Exception e) {
			// 扫描失败时返回空列表，图鉴该页为空但不会崩溃
		}

		ArrayList<Class<? extends Buff>> result = new ArrayList<>();
		for (String className : classNames) {
			Class<?> cls;
			try {
				// 不初始化类，避免触发静态代码块
				cls = Class.forName(className, false, BuffScanner.class.getClassLoader());
			} catch (Throwable t) {
				continue;
			}
			if (!Buff.class.isAssignableFrom(cls) || cls == Buff.class) {
				continue;
			}
			// 跳过接口与抽象类
			if (cls.isInterface() || java.lang.reflect.Modifier.isAbstract(cls.getModifiers())) {
				continue;
			}
			// 跳过匿名内部类（如 Buff$1）
			if (cls.getSimpleName().isEmpty() || cls.getName().matches(".*\\$\\d+.*")) {
				continue;
			}
			// 必须能通过无参构造实例化（WndJournal 需要 newInstance 展示图鉴）
			try {
				cls.getDeclaredConstructor();
			} catch (Throwable t) {
				continue;
			}
			// 只收录有图标的 Buff
			try {
				Buff buff = (Buff) cls.getDeclaredConstructor().newInstance();
				Object icon = buff.icon();
				if (icon == null || icon.equals(BuffIndicator.NONE)) {
					continue;
				}
			} catch (Throwable t) {
				continue;
			}
			result.add(cls.asSubclass(Buff.class));
		}
		return result;
	}

	private static void scanDirectory(File dir, String packageName, Set<String> classNames) {
		File[] files = dir.listFiles();
		if (files == null) return;
		for (File file : files) {
			if (file.isDirectory()) {
				scanDirectory(file, packageName + "." + file.getName(), classNames);
			} else if (file.getName().endsWith(".class")) {
				classNames.add(packageName + "." + file.getName().substring(0, file.getName().length() - 6));
			}
		}
	}

	private static void scanJar(URL url, Set<String> classNames) throws Exception {
		JarURLConnection conn = (JarURLConnection) url.openConnection();
		try (JarFile jarFile = conn.getJarFile()) {
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.startsWith(BASE_PATH) && name.endsWith(".class")) {
					classNames.add(name.substring(0, name.length() - 6).replace('/', '.'));
				}
			}
		}
	}
}
