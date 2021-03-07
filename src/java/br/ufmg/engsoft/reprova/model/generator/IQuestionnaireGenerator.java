package br.ufmg.engsoft.reprova.model.generator;

import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.model.Questionnaire;

public interface IQuestionnaireGenerator {
	public Questionnaire generate(QuestionsDAO questionsDAO, String averageDifficulty, int questionsCount,
			int totalEstimatedTime);
}
