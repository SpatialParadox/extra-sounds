package dev.stashy.extrasounds.resource.json.loot;

import com.google.gson.*;
import dev.stashy.extrasounds.resource.RuntimeResourcePack;
import dev.stashy.extrasounds.resource.json.models.JModel;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;

public class JCondition implements Cloneable {
	private JsonObject parameters = new JsonObject();

	/**
	 * @see JLootTable#condition(String)
	 * @see JLootTable#predicate(String)
	 * @see JModel#condition()
	 */
	public JCondition(String condition) {
		if (condition != null) {
			this.condition(condition);
		}
	}
	
	public JCondition() {
	}

	public JCondition condition(String condition) {
		this.parameters.addProperty("condition", condition);
		return this;
	}

	public JCondition set(JsonObject parameters) {
		parameters.addProperty("condition", this.parameters.get("condition").getAsString());
		this.parameters = parameters;
		return this;
	}

	public JCondition parameter(String key, Number value) {
		return parameter(key, new JsonPrimitive(value));
	}

	public JCondition parameter(String key, JsonElement value) {
		this.parameters.add(key, value);
		return this;
	}

	public JCondition parameter(String key, Boolean value) {
		return parameter(key, new JsonPrimitive(value));
	}

	public JCondition parameter(String key, Character value) {
		return parameter(key, new JsonPrimitive(value));
	}

	public JCondition parameter(String key, Identifier value) {
		return parameter(key, value.toString());
	}

	public JCondition parameter(String key, String value) {
		return parameter(key, new JsonPrimitive(value));
	}

	/**
	 * "or"'s the conditions together
	 */
	public JCondition alternative(JCondition... conditions) {
		JsonArray array = new JsonArray();
		for (JCondition condition : conditions) {
			array.add(RuntimeResourcePack.GSON.toJsonTree(condition));
		}
		this.parameters.add("terms", array);
		return this;
	}

	@Override
	public JCondition clone() {
		try {
			return (JCondition) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	public static class Serializer implements JsonSerializer<JCondition> {
		@Override
		public JsonElement serialize(JCondition src, Type typeOfSrc, JsonSerializationContext context) {
			return src.parameters;
		}
	}
}
