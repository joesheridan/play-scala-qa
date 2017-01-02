package models

import javax.inject._
import anorm._

/**
  * Created by joe on 30/12/2016.
  */
case class Question(ID:Int, Description:String)

@Singleton
class QuestionService @Inject() (db: play.api.db.Database) {

  /**
    * now called from the question controller constructor
    * since the findall select query generated an injection error
    * when run from own constructor
    */
  def onStartUp = {
    // create the table if it doesn't exist
    println("creating table:"+createTable())

    // populate test question § §data if none exists
    if (findAll.length == 0) addDummyQuestionData

  }

  def addDummyQuestionData = {
    Seq(
      Question(1, "this is a question"),
      Question(2, "this is a question2"),
      Question(3, "this is a question3"),
      Question(4, "this is a question4")
    ).foreach(create)
  }

  def createTable(): Boolean = {
    db.withConnection { implicit connection =>
      SQL("CREATE TABLE IF NOT EXISTS QUESTIONS(ID INT PRIMARY KEY, Description VARCHAR(255))").execute()
    }
  }

  /**
  * Parse a Question class from a ResultSet
  */
  val questionParser: RowParser[Question] = Macro.namedParser[Question]

  /**
  * Retrieve all questions.
  */
  def findAll: Seq[Question] = {
    try {
      db.withConnection { implicit connection =>
        SQL("select * from QUESTIONS").as(questionParser.*)
      }
    } catch {
      case e: Exception => {
        println("exception "+e)
        return Seq.empty[Question]
      }
    }
  }

  /**
    * Retrieve a question by ID.
    */
  def findByID(questionID:Int): Option[Question] = {
    try {
      db.withConnection { implicit connection =>
        Some(SQL("select * from QUESTIONS where ID = {questionID}").on('questionID -> questionID).as(questionParser.single))
      }
    } catch {
      case e: Exception => {
        println("exception "+e)
        return None
      }
    }
  }

  /**
  * Create a Question.
  */
  def create(question: Question): Question = {
    println("creating question")
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into questions values (
            {ID}, {description}
          )
        """
      ).on(
        'ID -> question.ID,
        'description -> question.Description
      ).executeUpdate()

      question

    }
  }
}