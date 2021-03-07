package br.ufmg.engsoft.reprova.model.generator;

public class GeneratorFactory {

	public IQuestionnaireGenerator getGenerator(int difficultiesCount) {
		if (difficultiesCount != 0) {
			return new DifficultyGroupGenerator();
		}

		return new DefaultGenerator();
	}
}