package dev.stashy.extrasounds.resource.util;

public class BaseClonable<Self extends BaseClonable<Self>> implements Cloneable {
	@Override
	public Self clone() {
		try {
			return (Self) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
