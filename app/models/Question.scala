package models

import javax.inject._
import anorm._
import scala.util.{Try, Success, Failure}

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
    println("creating questions table:"+createTable())

    // populate test question § §data if none exists
    findAll match {
      case Success(Nil) => { println("no questions found, adding dummy questions"); addDummyQuestionData }
      case Success(_) => { println("some questions found, doing nothing:"); }
      case Failure(e) => addDummyQuestionData
    }
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
      SQL("CREATE TABLE IF NOT EXISTS QUESTIONS(ID INT AUTO_INCREMENT PRIMARY KEY, Description VARCHAR(255))").execute()
    }
  }

  /**
  * Parse a Question class from a ResultSet
  */
  val questionParser: RowParser[Question] = Macro.namedParser[Question]

  /**
  * Retrieve all questions.
  */
  def findAll: Try[Seq[Question]] = {
      Try(db.withConnection { implicit connection =>
        SQL("select * from QUESTIONS").as(questionParser.*)
      })
  }

  /**
    * Retrieve a question by ID.
    */
  def findByID(questionID:Int): Try[Question] = {
    Try(db.withConnection { implicit connection =>
      SQL("select * from QUESTIONS where ID = {questionID}").on('questionID -> questionID).as(questionParser.single)
    })
  }

  /**
  * Create a Question.
  */
  def create(q: Question): Try[Option[Long]] = {
    println("creating question")
    Try(db.withConnection { implicit connection =>
      SQL(
        """
          insert into questions (Description) values (
             {description}
          )
        """
      ).on(
        'description -> q.Description
      ).executeInsert()

    })
  }
}