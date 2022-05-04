package dev.stashy.extrasounds.config.category;

public interface IInventoryCategory extends ICategory {
    boolean isClickEnabled();

    boolean isDragEnabled();

    boolean isDropEnabled();

    boolean isOpenEnabled();
}
