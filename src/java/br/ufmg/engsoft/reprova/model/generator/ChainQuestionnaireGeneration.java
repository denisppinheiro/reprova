package br.ufmg.engsoft.reprova.model.generator;

import br.ufmg.engsoft.reprova.model.Questionnaire;
import java.lang.UnsupportedOperationException;

/* The type ChainQuestionnaireGeneration */
abstract class ChainQuestionnaireGeneration {
  ChainQuestionnaireGeneration next;
  IQuestionnaireGenerator generator;

  /* Set next */
  public void setNext(ChainQuestionnaireGeneration next) {
    this.next = next;
  }

   /* Generate */
  Questionnaire generate(Questionnaire questionnaire) {
    throw new UnsupportedOperationException("Method not implemented on abstract level.");
  }
  
  /* Handle Generation */
  Questionnaire handleGeneration(Questionnaire questionnaire) {
    if (next != null) {
      return next.generate(questionnaire);
    }
    return questionnaire;
  }
}
