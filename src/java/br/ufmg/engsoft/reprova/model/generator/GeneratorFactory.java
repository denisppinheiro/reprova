package br.ufmg.engsoft.reprova.model.generator;

/*
* The type GeneratorFactory
*/
public class GeneratorFactory{
  /*
   * GeneratorFactory Constructor
   */
  public IQuestionnaireGenerator getGenerator(int difficultiesCount) {
	  if (difficultiesCount != 0){
		  return new DifficultyGroupGenerator();
	  }

	  return new DefaultGenerator();  
  }
}
