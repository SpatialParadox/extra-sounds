package dev.stashy.extrasounds.resource.json.models;

import dev.stashy.extrasounds.resource.json.loot.JCondition;
import dev.stashy.extrasounds.resource.util.BaseClonable;
import net.minecraft.util.Identifier;

public class JOverride extends BaseClonable<JOverride> {
	public final JCondition predicate;
	public final String model;

	/**
	 * @see JModel#override(JCondition, Identifier)
	 */
	public JOverride(JCondition condition, String model) {
		this.predicate = condition;
		this.model = model;
	}
}
