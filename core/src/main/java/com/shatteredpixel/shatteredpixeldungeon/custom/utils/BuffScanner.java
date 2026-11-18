package com.shatteredpixel.shatteredpixeldungeon.custom.utils;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import java.util.ArrayList;

/**
 * 图鉴「效果百科」用的 Buff 清单。
 * <br>
 * 从编译期生成的类名清单 BuffClassIndex 里，找出 Buff 的全部非抽象、可实例化子类，
 * 并只保留 icon() 有实际图标的 Buff。
 * <br>
 * BuffClassIndex 由 event-processor 模块的 BuffClassIndexProcessor 在每次编译时自动生成，
 * 新增 Buff 类后无需手动登记。
 * <br>
 * 不能在运行时按包名扫描类路径：安卓的 APK 里类都在 dex 里，没有 .class 文件可列，
 * 扫出来是空的，效果百科页在安卓上就一个条目都没有。
 */
public class BuffScanner {

	private static ArrayList<Class<? extends Buff>> cachedBuffClasses = null;

	public static ArrayList<Class<? extends Buff>> getAllBuffClasses() {
		if (cachedBuffClasses == null) {
			cachedBuffClasses = scanBuffClasses();
		}
		return new ArrayList<>(cachedBuffClasses);
	}

	private static ArrayList<Class<? extends Buff>> scanBuffClasses() {
		ArrayList<Class<? extends Buff>> result = new ArrayList<>();
		for (String className : BuffClassIndex.NAMES) {
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
}
