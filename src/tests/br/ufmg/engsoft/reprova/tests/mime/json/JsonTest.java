package br.ufmg.engsoft.reprova.tests.mime.json;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Question;


public class JsonTest {
  /**
   * Rendering then parsing should produce an equivalent object.
   */
  @Test
  @Disabled
  void question() {
    var question = new Question.Builder()
      .setId("id")
      .setTheme("theme")
      .setDescription("description")
      .setStatement("statement")
      // .record(
      //   Map.of(
      //     new Semester(2019, Semester.Reference._1), Map.of(
      //       "tw", 50.0f,
      //       "tz", 49.5f,
      //       "tx", 51.2f
      //     ),
      //     new Semester(2020, Semester.Reference._2), Collections.emptyMap()
      //   )
      // )
      .setPvt(false)
      .setEstimatedTime(8)
      .build();

    var formatter = new Json();

    var json = formatter.render(question);

    var questionCopy = formatter
      .parse(json, Question.Builder.class)
      .build();

    assertTrue(
      question.equals(questionCopy)
    );
  }
}
