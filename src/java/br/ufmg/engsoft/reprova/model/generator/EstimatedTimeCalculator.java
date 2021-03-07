package br.ufmg.engsoft.reprova.model.generator;

import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Questionnaire;

public class EstimatedTimeCalculator extends ChainQuestionnaireGeneration {

	@Override
	public Questionnaire generate(Questionnaire questionnaire) {
		int totalEstimatedTime = 0;

		for (Question question : questionnaire.questions) {
			totalEstimatedTime += question.estimatedTime;
		}

		questionnaire.setTotalEstimatedTime(totalEstimatedTime);
		return handleGeneration(questionnaire);
	}
}