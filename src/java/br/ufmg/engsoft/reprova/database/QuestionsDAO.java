package br.ufmg.engsoft.reprova.database;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Question;

/**
 * DAO for Question class on mongodb.
 */
public class QuestionsDAO {
    /**
     * Logger instance.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(QuestionsDAO.class);

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
     *
     * @param mongoDB   the database, mustn't be null
     * @param json the json formatter for the database's documents, mustn't be null
     * @throws IllegalArgumentException if any parameter is null
     */
    public QuestionsDAO(Mongo mongoDB, Json json) {
        if (mongoDB == null) {
            throw new IllegalArgumentException("db mustn't be null");
        }

        if (json == null) {
            throw new IllegalArgumentException("json mustn't be null");
        }

        this.collection = mongoDB.getCollection("questions");

        this.json = json;
    }

    /**
     * Parse the given document.
     *
     * @param document the question document, mustn't be null
     * @throws IllegalArgumentException if any parameter is null
     * @throws IllegalArgumentException if the given document is an invalid Question
     */
    protected Question parseDoc(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document mustn't be null");
        }

        var doc = document.toJson();

        LOGGER.info("Fetched question: " + doc);

        try {
            var question = json.parse(doc, Question.Builder.class).build();

            return question;
        } catch (Exception e) {
            LOGGER.error("Invalid document in database!", e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Get the question with the given id.
     *
     * @param id the question's id in the database.
     * @return The question, or null if no such question.
     * @throws IllegalArgumentException if any parameter is null
     */
    public Question get(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id mustn't be null");
        }

        var question = this.collection.find(eq(new ObjectId(id))).map(this::parseDoc).first();

        if (question == null) {
            LOGGER.info("No such question " + id);
        }

        return question;
    }

    /**
     * List all the questions that match the given non-null parameters. The
     * question's statement is ommited.
     *
     * @param theme the expected theme, or null
     * @param pvt   the expected privacy, or null
     * @return The questions in the collection that match the given parameters,
     *         possibly empty.
     * @throws IllegalArgumentException if there is an invalid Question
     */
    public Collection<Question> list(String theme, Boolean pvt) {
        var filters = Arrays.asList(theme == null ? null : eq("theme", theme), pvt == null ? null : eq("pvt", pvt))
                .stream().filter(Objects::nonNull) // mongo won't allow null filters.
                .collect(Collectors.toList());

        var doc = filters.isEmpty() // mongo won't take null as a filter.
                ? this.collection.find()
                : this.collection.find(and(filters));

        var result = new ArrayList<Question>();

        doc.projection(fields(exclude("statement"))).map(this::parseDoc).into(result);
        
        if(Environments.getInstance().isEnableQuestionStatistics()){
            for (var question : result){
                question.getStatistics();
            }
        }

        return result;
    }

    /**
     * Adds or updates the given question in the database. If the given question has
     * an id, update, otherwise add.
     *
     * @param question the question to be stored
     * @return Whether the question was successfully added.
     * @throws IllegalArgumentException if any parameter is null
     */
    public boolean add(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("question mustn't be null");
        }

        question.calculateDifficulty();
        Map<String, Object> record = null;
        if (question.record != null) {
            record = question.record // Convert the keys to string,
                    .entrySet() // and values to object.
                    .stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
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

        if (Environments.getInstance().getDifficultyGroup() != 0){
            doc = doc.append("difficulty", question.difficulty);
        }
        
        if (Environments.getInstance().isEnableMultipleChoice()) {
            doc = doc.append("choices", question.getChoices());
        }
        if (Environments.getInstance().isEnableQuestionStatistics()) {
            doc = doc.append("statistics", question.getStatistics());
        }

        var id = question.id;
        if (id == null) {
        	this.collection.insertOne(doc);
        } else {
        	var result = this.collection.replaceOne(eq(new ObjectId(id)), doc);

            if (!result.wasAcknowledged()) {
                LOGGER.warn("Failed to replace question " + id);
                return false;
            }
        }

        LOGGER.info("Stored question " + doc.get("_id"));

        return true;
    }

    /**
     * Remove the question with the given id from the collection.
     *
     * @param id the question id
     * @return Whether the given question was removed.
     * @throws IllegalArgumentException if any parameter is null
     */
    public boolean remove(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id mustn't be null");
        }
        var result = this.collection.deleteOne(eq(new ObjectId(id))).wasAcknowledged();

        if (result) {
            LOGGER.info("Deleted question " + id);
        } else {
            LOGGER.warn("Failed to delete question " + id);
        }

        return result;
    }
}
