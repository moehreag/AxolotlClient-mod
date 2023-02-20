package io.github.axolotlclient.api.requests;

import com.google.gson.JsonObject;
import io.github.axolotlclient.api.Request;

import java.util.function.Consumer;

public class Friends extends Request {

	public Friends(Consumer<JsonObject> consumer, String method, String uuid) {
		super("friends", consumer, "method", method, "uuid", uuid);
	}

	public Friends(Consumer<JsonObject> consumer, String method) {
		super("friends", consumer, "method", method);
	}
}
