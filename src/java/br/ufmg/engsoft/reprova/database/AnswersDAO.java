package br.ufmg.engsoft.reprova.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

import org.bson.Document;
import org.bson.types.ObjectId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Answer;


/**
 * DAO for Question class on mongodb.
 */
public class AnswersDAO {
  /**
   * Logger instance.
   */
  protected static final Logger LOGGER = LoggerFactory.getLogger(AnswersDAO.class);

  /**
   * Json formatter.
   */
  protected final Json json;

  /**
   * Questions collection.
   */
  protected final MongoCollection<Document> collection;

  /**
   * Basic constructor.
   * @param db    the database, mustn't be null
   * @param json  the json formatter for the database's documents, mustn't be null
   * @throws IllegalArgumentException  if any parameter is null
   */
  public AnswersDAO(Mongo mongoDb, Json json) {
    if (mongoDb == null) {
      throw new IllegalArgumentException("db mustn't be null");
    }

    if (json == null) {
      throw new IllegalArgumentException("json mustn't be null");
    }

    this.collection = mongoDb.getCollection("answers");

    this.json = json;
  }



  /**
   * Parse the given document.
   * @param document  the answer document, mustn't be null
   * @throws IllegalArgumentException  if any parameter is null
   * @throws IllegalArgumentException  if the given document is an invalid Question
   */
  protected Answer parseDoc(Document document) {
    if (document == null) {
      throw new IllegalArgumentException("document mustn't be null");
    }

    var doc = document.toJson();

    LOGGER.info("Fetched answer: " + doc);

    try {
      var answer = json
        .parse(doc, Answer.Builder.class)
        .build();

      LOGGER.info("Parsed answer: " + answer);

      return answer;
    }
    catch (Exception e) {
      LOGGER.error("Invalid document in database!", e);
      throw new IllegalArgumentException(e);
    }
  }


  /**
   * Get the answer with the given id.
   * @param identifier  the answer's id in the database.
   * @return The answer, or null if no such question.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Answer get(String identifier) {
    if (identifier == null) {
      throw new IllegalArgumentException("id mustn't be null");
    }

    var answer = this.collection
      .find(eq(new ObjectId(identifier)))
      .map(this::parseDoc)
      .first();

    if (answer == null) {
      LOGGER.info("No such answer " + identifier);
    }

    return answer;
  }


  /**
   * List all the answers that match the given non-null parameters.
   * The question's statement is ommited.
   * @param theme      the expected theme, or null
   * @param pvt        the expected privacy, or null
   * @return The answers in the collection that match the given parameters, possibly
   *         empty.
   * @throws IllegalArgumentException  if there is an invalid Question
   */
  public Collection<Answer> list(String questionId, Boolean pvt) {
    var filters =
      Arrays.asList(
        questionId == null ? null : eq("questionId", questionId),
        pvt == null ? null : eq("pvt", pvt)
      )
      .stream()
      .filter(Objects::nonNull) // mongo won't allow null filters.
      .collect(Collectors.toList());

    var doc = filters.isEmpty() // mongo won't take null as a filter.
      ? this.collection.find()
      : this.collection.find(and(filters));

    var result = new ArrayList<Answer>();

    doc.projection(fields(exclude("statement")))
      .map(this::parseDoc)
      .into(result);

    return result;
  }


  /**
   * Adds or updates the given answer in the database.
   * If the given answer has an id, update, otherwise add.
   * @param answer  the answer to be stored
   * @param question the question for which the answer must be stored
   * @return Whether the answer was successfully added.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public boolean add(Answer answer, String questionId) {
    if (answer == null) {
      throw new IllegalArgumentException("answer mustn't be null");
    }
    
    if (questionId == null) {
        throw new IllegalArgumentException("questionId must be passed");
    }

    Document doc = new Document()
      .append("description", answer.getDescription())
      .append("pvt", answer.isPvt())
      .append("questionId", questionId);

    String identifier = answer.getId();
    if (identifier == null) {
    	this.collection.insertOne(doc);
    }
    else {
		var result = this.collection.replaceOne(
		        eq(new ObjectId(identifier)),
		        doc
		      );

      if (!result.wasAcknowledged()) {
        LOGGER.warn("Failed to replace answer " + identifier);
        return false;
      }
    }

    LOGGER.info("Stored answer " + doc.get("_id"));

    return true;
  }


  /**
   * Remove the answer with the given id from the collection.
   * @param identifier  the answer id
   * @return Whether the given question was removed.
   * @throws IllegalArgumentException  if any parameter is null
   */
  public boolean remove(String identifier) {
    if (identifier == null) {
      throw new IllegalArgumentException("id mustn't be null");
    }

    var result = this.collection.deleteOne(
      eq(new ObjectId(identifier))
    ).wasAcknowledged();

    if (result) {
      LOGGER.info("Deleted answer " + identifier);
    } else {
      LOGGER.warn("Failed to delete answer " + identifier);
    }

    return result;
  }
}
