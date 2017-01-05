package models

import javax.inject.{Inject, Singleton}

import anorm.{Macro, RowParser, SQL}
import scala.util._

/**
  * Created by joe on 30/12/2016.
  */
case class Answer(ID:Int, Description:String, QID:Int)


@Singleton
class AnswerService @Inject() (db: play.api.db.Database) {

  /**
    * now called from the answer controller constructor
    * since the findall select query generated an injection error
    * when run from own constructor
    */
  def onStartUp = {
    // create the table if it doesn't exist
    println("creating answer table:" + createTable())

    // populate test answer § §data if none exists
    if (findAll.length == 0) addDummyAnswerData

  }

  def addDummyAnswerData = {
    Seq(
      Answer(1, "this is an answer", 1),
      Answer(2, "this is an answer2", 1),
      Answer(3, "this is an answer3", 1),
      Answer(4, "this is an answer", 2),
      Answer(5, "this is an answer2", 2),
      Answer(6, "this is an answer3", 2)
    ).foreach(create)
  }

  def createTable(): Boolean = {
    db.withConnection { implicit connection =>
      SQL("CREATE TABLE IF NOT EXISTS ANSWERS(ID INT AUTO_INCREMENT PRIMARY KEY, Description VARCHAR(255), QID INT)").execute()
    }
  }

  /**
    * Parse a answer class from a ResultSet
    */
  val answerParser: RowParser[Answer] = Macro.namedParser[Answer]

  /**
    * Retrieve all answers.
    */
  def findAll: Seq[Answer] = {
    try {
      db.withConnection { implicit connection =>
        SQL("select * from answerS").as(answerParser.*)
      }
    } catch {
      case e: Exception => {
        println("exception " + e)
        return Seq.empty[Answer]
      }
    }
  }

  /**
    * Retrieve a answer by ID.
    */
  def findByID(answerID: Int): Option[Answer] = {
    try {
      db.withConnection { implicit connection =>
        Some(SQL("select * from answers where ID = {answerID}").on('answerID -> answerID).as(answerParser.single))
      }
    } catch {
      case e: Exception => {
        println("exception " + e)
        return None
      }
    }
  }

  def getAnswers(q:Question): Try[Seq[Answer]] = {
    Try(db.withConnection { implicit connection =>
      SQL("select * from ANSWERS WHERE QID = {QID}").on('QID -> q.ID).as(answerParser.*)
    })
  }

  /**
    * Create a answer.
    */
  def create(answer: Answer): Try[Option[Long]] = {
    println("creating answer")
    Try(db.withConnection { implicit connection =>
        SQL(
          """
            insert into answers (Description, QID) values (
              {Description}, {QID}
            )
          """
        ).on(
          'Description -> answer.Description,
          'QID -> answer.QID
        ).executeInsert()
    })
  }
}
