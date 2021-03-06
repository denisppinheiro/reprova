package br.ufmg.engsoft.reprova.model.difficulty;

/* DifficultyFactory */
public class DifficultyFactory{
  
  /* getDifficulty */
  public IDifficultyGroup getDifficulty(int difficultiesCount) {
	  if (difficultiesCount == 3){
		  return new DifficultyGroup3();
	  }

	  return new DifficultyGroup5();  
  }
}