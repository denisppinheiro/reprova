package br.ufmg.engsoft.reprova.model.generator;

import br.ufmg.engsoft.reprova.model.Questionnaire;

abstract class ChainQuestionnaireGeneration {
	ChainQuestionnaireGeneration next;
	IQuestionnaireGenerator generator;

	public void setNext(ChainQuestionnaireGeneration next) {
		this.next = next;
	}

	Questionnaire generate(Questionnaire questionnaire) {
		throw new UnsupportedOperationException("Method not implemented on abstract level.");
	}

	Questionnaire handleGeneration(Questionnaire questionnaire) {
		if (next != null) {
			return next.generate(questionnaire);
		}
		return questionnaire;
	}
}