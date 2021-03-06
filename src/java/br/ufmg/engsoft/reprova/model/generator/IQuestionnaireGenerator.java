package br.ufmg.engsoft.reprova.model.generator;

import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;

/*
* The type IQuestionnaireGenerator
*/
public interface IQuestionnaireGenerator {
  /*
  * Generate IQuestionnaire
  */
  public Questionnaire generate(QuestionsDAO questionsDAO, String averageDifficulty, int questionsCount, int totalEstimatedTime);
}
