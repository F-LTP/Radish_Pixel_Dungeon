package rpdtest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** 反射小工具：读写游戏里的 private / package-private 成员（测试代码不在游戏的包里）。 */
public final class Reflect {

	private Reflect() {}

	public static Field field( Class<?> cls, String name ) {
		for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
			try {
				Field f = c.getDeclaredField( name );
				f.setAccessible( true );
				return f;
			} catch (NoSuchFieldException ignored) {
			}
		}
		throw new HarnessError( "no field '" + name + "' in " + cls.getName() + " (game code changed?)" );
	}

	public static boolean hasField( Class<?> cls, String name ) {
		for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
			try {
				c.getDeclaredField( name );
				return true;
			} catch (NoSuchFieldException ignored) {
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getStatic( Class<?> cls, String name ) {
		try {
			return (T) field( cls, name ).get( null );
		} catch (IllegalAccessException e) {
			throw new HarnessError( "cannot read " + cls.getName() + "." + name, e );
		}
	}

	public static void setStatic( Class<?> cls, String name, Object value ) {
		try {
			field( cls, name ).set( null, value );
		} catch (IllegalAccessException e) {
			throw new HarnessError( "cannot write " + cls.getName() + "." + name, e );
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T get( Object obj, String name ) {
		try {
			return (T) field( obj.getClass(), name ).get( obj );
		} catch (IllegalAccessException e) {
			throw new HarnessError( "cannot read " + obj.getClass().getName() + "." + name, e );
		}
	}

	public static void set( Object obj, String name, Object value ) {
		try {
			field( obj.getClass(), name ).set( obj, value );
		} catch (IllegalAccessException e) {
			throw new HarnessError( "cannot write " + obj.getClass().getName() + "." + name, e );
		}
	}

	public static Method method( Class<?> cls, String name, Class<?>... types ) {
		for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
			try {
				Method m = c.getDeclaredMethod( name, types );
				m.setAccessible( true );
				return m;
			} catch (NoSuchMethodException ignored) {
			}
		}
		throw new HarnessError( "no method '" + name + "' in " + cls.getName() + " (game code changed?)" );
	}

	public static boolean hasMethod( Class<?> cls, String name, Class<?>... types ) {
		for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
			try {
				c.getDeclaredMethod( name, types );
				return true;
			} catch (NoSuchMethodException ignored) {
			}
		}
		return false;
	}

	/** 调用实例方法；被调方法抛出的异常原样（解包后）抛出。 */
	@SuppressWarnings("unchecked")
	public static <T> T call( Object obj, String name, Class<?>[] types, Object... args ) {
		Method m = method( obj.getClass(), name, types );
		try {
			return (T) m.invoke( obj, args );
		} catch (java.lang.reflect.InvocationTargetException e) {
			Throwable c = e.getCause();
			if (c instanceof RuntimeException) throw (RuntimeException) c;
			if (c instanceof Error) throw (Error) c;
			throw new RuntimeException( c );
		} catch (IllegalAccessException e) {
			throw new HarnessError( "cannot call " + obj.getClass().getName() + "." + name, e );
		}
	}
}
