package br.ufmg.engsoft.reprova.tests.mime.json;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Question;

public class JsonTest {
	/**
	 * Rendering then parsing should produce an equivalent object.
	 */
	@Test
	void question() {
		var question = new Question.Builder().id("id").theme("theme").description("description").statement("statement")
				.pvt(false).estimatedTime(8).build();

		var formatter = new Json();

		var json = formatter.render(question);

		var questionCopy = formatter.parse(json, Question.Builder.class).build();

		assertTrue(question.equals(questionCopy));
	}
}
