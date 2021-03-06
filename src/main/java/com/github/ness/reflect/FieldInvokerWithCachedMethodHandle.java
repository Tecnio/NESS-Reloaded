package com.github.ness.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * A field invoker wrapping another, which caches the value of {@link FieldInvoker#unreflectGetter()}
 * 
 * @author A248
 *
 * @param <T> the type of the field
 */
public class FieldInvokerWithCachedMethodHandle<T> implements FieldInvoker<T> {

	private final FieldInvoker<T> delegate;
	private final MethodHandle methodHandle;

	public FieldInvokerWithCachedMethodHandle(FieldInvoker<T> delegate, MethodHandle methodHandle) {
		this.delegate = Objects.requireNonNull(delegate);
		this.methodHandle = Objects.requireNonNull(methodHandle);
	}

	public FieldInvokerWithCachedMethodHandle(FieldInvoker<T> delegate) {
		this(delegate, delegate.unreflectGetter());
	}

	@Override
	public void set(Object object, T value) {
		delegate.set(object, value);
	}

	@Override
	public T get(Object object) {
		try {
			return (T) methodHandle.invoke(object);
		} catch (RuntimeException | Error ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new ReflectionException(ex);
		}
	}

	@Override
	public MethodHandle unreflectGetter() {
		return methodHandle;
	}

	@Override
	public Field reflect() {
		return delegate.reflect();
	}

	@Override
	public int hashCode() {
		return 31 + reflect().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return this == object || object instanceof FieldInvoker
				&& reflect().equals(((FieldInvoker<?>) object).reflect());
	}

	@Override
	public String toString() {
		return "FieldInvokerWithCachedMethodHandle [delegate=" + delegate + ", methodHandle=" + methodHandle + "]";
	}

}
