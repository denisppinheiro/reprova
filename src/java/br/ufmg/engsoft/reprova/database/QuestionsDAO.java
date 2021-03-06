package br.ufmg.engsoft.reprova.database;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import br.ufmg.engsoft.reprova.model.Question.Builder;

/**
 * DAO for Question class on mongodb.
 */
public class QuestionsDAO extends MongoDAO<Question, Question.Builder> {
	

	@Override
	protected String getCollectionName() {
		return "questions";
	}

	@Override
	protected Class<Builder> getBuilderClass() {
		return Question.Builder.class;
	}
	
    /**
     * Basic constructor.
     *
     * @param db   the database, mustn't be null
     * @param json the json formatter for the database's documents, mustn't be null
     * @throws IllegalArgumentException if any parameter is null
     */
    public QuestionsDAO(Mongo db, Json json) {
        super(db, json);
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
        
        if(Environments.getInstance().getEnableQuestionStatistics()){
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

        if (Environments.getInstance().getEnableEstimatedTime()){
            doc = doc.append("estimatedTime", question.estimatedTime);
        }

        if (Environments.getInstance().getDifficultyGroup() != 0){
            doc = doc.append("difficulty", question.difficulty);
        }
        
        if (Environments.getInstance().getEnableMultipleChoice()) {
            doc = doc.append("choices", question.getChoices());
        }
        if (Environments.getInstance().getEnableQuestionStatistics()) {
            doc = doc.append("statistics", question.getStatistics());
        }

        return super.add(doc, question.getId());
    }

}
