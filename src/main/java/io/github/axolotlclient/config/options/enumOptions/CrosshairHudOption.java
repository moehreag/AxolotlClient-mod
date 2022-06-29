package io.github.axolotlclient.config.options.enumOptions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.axolotlclient.config.options.EnumOption;
import io.github.axolotlclient.config.options.OptionBase;
import org.jetbrains.annotations.NotNull;

public class CrosshairHudOption extends OptionBase implements EnumOption {

    private CrosshairOption value;

    public CrosshairHudOption(String name) {
        super(name);
        value=CrosshairOption.CROSS;
    }

    @Override
    public void setValueFromJsonElement(@NotNull JsonElement element) {
	    switch (element.getAsString()) {
		    case ("CROSS") -> this.value = CrosshairOption.CROSS;
		    case ("DOT") -> this.value = CrosshairOption.DOT;
		    case ("TEXTURE") -> this.value = CrosshairOption.TEXTURE;
			case ("DIRECTION") -> this.value = CrosshairOption.DIRECTION;
	    }
    }

    @Override
    public void setDefaults() {
        value=CrosshairOption.CROSS;
    }

    @Override
    public JsonElement getJson() {
        return new JsonPrimitive(value.toString());
    }

    @Override
    public Enum<?> get() {
        return value;
    }

    @Override
    public Enum<?> next() {
	    switch (value) {
		    case CROSS -> value = CrosshairOption.DOT;
		    case DOT -> value = CrosshairOption.TEXTURE;
		    case TEXTURE -> value = CrosshairOption.DIRECTION;
		    case DIRECTION -> value = CrosshairOption.CROSS;
	    }

        return value;
    }

    public enum CrosshairOption{
        CROSS,
        DOT,
        TEXTURE,
	    DIRECTION
    }
}