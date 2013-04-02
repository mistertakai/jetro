package net.sf.jetro.tree;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.NoSuchElementException;

import net.sf.jetro.path.JsonPath;

import org.testng.annotations.Test;

public class JsonArrayTest {

	/**
	 * Test that toJson on JsonArray actually returns String correctly representing the JsonArray.
	 */
	@Test
	public void shouldRenderItself() {
		// simple array of string types
		JsonArray jsonArray = new JsonArray();
		jsonArray.add(new JsonString("hello"));
		jsonArray.add(new JsonString("goodbye"));
		jsonArray.add(new JsonString("thank you"));
		jsonArray.add(new JsonString("welcome"));

		// call toJson on JsonArray
		String actual = jsonArray.toJson();
		String expected = "[\"hello\",\"goodbye\",\"thank you\",\"welcome\"]";

		// Assert
		assertEquals(actual, expected);
	}

	/**
	 * Test for toJson(JsonRenderer r) using a mocked JsonRenderer. 
	 */
	@Test
	public void shouldRenderItselfWithRenderer() {
		JsonArray jsonArray = new JsonArray();

		String expected = String.valueOf(System.currentTimeMillis());
		JsonRenderer mockedRenderer = mock(JsonRenderer.class);
		when(mockedRenderer.render(any(JsonElement.class))).thenReturn(expected);

		// call toJson on JsonArray with a JsonRenderer
		String actual = jsonArray.toJson(mockedRenderer);
		verify(mockedRenderer).render(jsonArray);

		// Assert
		assertEquals(actual, expected);
	}

	/**
	 * Test that getChildElementAt returns String representing the correct element.
	 */
	// TODO when internal path setting is implemented remove the expectedExceptions directive
	@Test(expectedExceptions = NoSuchElementException.class)
	public void shouldGetChildElementAt() {
		// Setup JSON tree representing {"foo":[1,"two",{"bar":true}]}
		JsonObject root = new JsonObject();
		JsonProperty foo = new JsonProperty("foo");
		JsonArray jsonArray = new JsonArray();
		jsonArray.add(new JsonNumber(1));
		jsonArray.add(new JsonString("two"));
		JsonObject barObject = new JsonObject();
		JsonProperty bar = new JsonProperty("bar");
		bar.setValue(new JsonBoolean(true));
		barObject.add(bar);
		jsonArray.add(barObject);
		foo.setValue(jsonArray);
		root.add(foo);

		// define path for third element
		JsonPath jsonPath = JsonPath.compile("$.foo[2]");

		// call getElementAt on JsonArray
		String actual = jsonArray.getElementAt(jsonPath).toString();
		String expected = "{\"bar\":true}";

		// Assert
		assertEquals(actual, expected);
	}
}