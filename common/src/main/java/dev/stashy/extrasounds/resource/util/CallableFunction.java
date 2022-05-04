package dev.stashy.extrasounds.resource.util;

public interface CallableFunction<A, B> {
	B get(A a) throws Exception;
}
