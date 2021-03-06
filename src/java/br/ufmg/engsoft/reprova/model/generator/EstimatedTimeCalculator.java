package br.ufmg.engsoft.reprova.model.generator;

import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Questionnaire;

/*
* The type EstimatedTimeCalculator
*/
public class EstimatedTimeCalculator extends ChainQuestionnaireGeneration{

  /*
  * Generate Questionnaire
  */
  @Override
  public Questionnaire generate(Questionnaire questionnaire){
    int totalEstimatedTime = 0;
    
    for (Question question : questionnaire.questions){
      totalEstimatedTime += question.estimatedTime;
    }

    questionnaire.totalEstimatedTime = totalEstimatedTime;
    return handleGeneration(questionnaire);
  }
}
