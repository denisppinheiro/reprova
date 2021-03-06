package br.ufmg.engsoft.reprova.database;

import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.fields;

import org.bson.Document;
import org.bson.types.ObjectId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Questionnaire;


/**
 * DAO for Questionnaire class on mongodb.
 */
public class QuestionnairesDAO {
  /**
   * Logger instance.
   */
  protected static final Logger LOGGER = LoggerFactory.getLogger(QuestionnairesDAO.class);

  /**
   * Json formatter.
   */
  protected final Json json;

  /**
   * Questionnaires collection.
   */
  protected final MongoCollection<Document> collection;


  /**
   * Basic constructor.
   * @param mongoDB    the database, mustn't be null
   * @param json  the json formatter for the database's documents, mustn't be null
   * @throws IllegalArgumentException  if any parameter is null
   */
  public QuestionnairesDAO(Mongo mongoDB, Json json) {
    if (mongoDB == null) {
      throw new IllegalArgumentException("db mustn't be null");
    }
    if (json == null) {
      throw new IllegalArgumentException("json mustn't be null");
    }
    this.collection = mongoDB.getCollection("questionnaires");

    this.json = json;
  }



  /**
   * Parse the given document.
   * @param document  the question document, mustn't be null
   * @throws IllegalArgumentException  if any parameter is null
   * @throws IllegalArgumentException  if the given document is an invalid Questionnaire
   */
  protected Questionnaire parseDoc(Document document) {
    if (document == null) {
      throw new IllegalArgumentException("document mustn't be null");
    }
    var doc = document.toJson();

    LOGGER.info("Fetched questionnaire: " + doc);

    try {
      var questionnaire = json
        .parse(doc, Questionnaire.Builder.class)
        .build();

      LOGGER.info("Parsed questionnaire: " + questionnaire);

      return questionnaire;
    }
    catch (Exception e) {
      LOGGER.error("Invalid document in database!", e);
      throw new IllegalArgumentException(e);
    }
  }


  /**
   * Get the questionnaire with the given id.
   * @param id  the questionnaire's id in the database.
   * @return The questionnaire, or null if no such questionnaire.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Questionnaire get(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id mustn't be null");
    }
    var questionnaire = this.collection
      .find(eq(new ObjectId(id)))
      .map(this::parseDoc)
      .first();

    if (questionnaire == null) {
      LOGGER.info("No such questionnaire " + id);
    }
    return questionnaire;
  }

  /**
   * List all the questionnaires that match the given non-null parameters.
   * The questionnaire's statement is ommited.
   * @return The questionnaires in the collection that match the given parameters, possibly
   *         empty.
   * @throws IllegalArgumentException  if there is an invalid Questionnaire
   */
  public Collection<Questionnaire> list() {
    var doc = this.collection.find();

    var result = new ArrayList<Questionnaire>();

    doc.projection(fields())
      .map(this::parseDoc)
      .into(result);

    return result;
  }

  /**
   * Adds or updates the given questionnaire in the database.
   * If the given questionnaire has an id, update, otherwise add.
   * @param questionnaire  the questionnaire to be stored
   * @return Whether the questionnaire was successfully added.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public boolean add(Questionnaire questionnaire) {
    if (questionnaire == null){
      throw new IllegalArgumentException("questionnaire mustn't be null");
    }

    ArrayList<Document> questions = new ArrayList<Document>();
    for (var question : questionnaire.questions){
      Map<String, Object> record = null;
      if (question.record != null){
        record = question.record // Convert the keys to string,
        .entrySet()                                // and values to object.
        .stream()
        .collect(
          Collectors.toMap(
            e -> e.getKey().toString(),
            Map.Entry::getValue
          )
        );
      }
          
      Document doc = new Document()
          .append("theme", question.theme)
          .append("description", question.description)
          .append("statement", question.statement)
          .append("record", record == null ? null : new Document(record))
          .append("pvt", question.pvt);
      
      if (Environments.getInstance().isEnableEstimatedTime()){
        doc = doc.append("estimatedTime", question.estimatedTime);
      }
      
      if (Environments.getInstance().isEnableMultipleChoice()){
        doc = doc.append("choices", question.getChoices());
      }

      if (Environments.getInstance().getDifficultyGroup() != 0){
        doc = doc.append("difficulty", question.difficulty);
      }
      
      questions.add(doc);
    }

    Document doc = new Document()
        .append("averageDifficulty", questionnaire.averageDifficulty)
        .append("questions", questions);

    if (Environments.getInstance().isEnableEstimatedTime()){
      doc = doc.append("totalEstimatedTime", questionnaire.totalEstimatedTime);
    }
    
    var id = questionnaire.id;
    if (id == null) {
    	this.collection.insertOne(doc);
    }
    else {
    	var result = this.collection.replaceOne(
    	        eq(new ObjectId(id)),
    	        doc
    	      );

	      if (!result.wasAcknowledged()) {
	        LOGGER.warn("Failed to replace questionnaire " + id);
	        return false;
	      }
    }
    LOGGER.info("Stored questionnaire " + doc.get("_id"));

    return true;
  }


  /**
   * Remove the questionnaire with the given id from the collection.
   * @param id  the questionnaire id
   * @return Whether the given questionnaire was removed.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public boolean remove(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id mustn't be null");
    }
    var result = this.collection.deleteOne(
      eq(new ObjectId(id))
    ).wasAcknowledged();

    if (result) {
      LOGGER.info("Deleted questionnaire " + id);
    }else {
      LOGGER.warn("Failed to delete questionnaire " + id);
    }
    return result;
  }
}
