package controllers

import javax.inject._
import anorm.SQL
import play.api.db.Database
import play.api.mvc._

import models.{Question, QuestionService}

/**
  * Created by joe on 30/12/2016.
  */
@Singleton
class QuestionController @Inject() (db: Database, questionService: QuestionService) extends Controller {

  // initialise the questionservice
  questionService.onStartUp

  def add(question:Question) = Action {
    db.withConnection { implicit c =>
      val id: Option[Long] = SQL("Insert into Questions (description, created) values ({description}, {created})")
                              .on('description -> question.Description ).executeInsert()
      Ok(id.getOrElse("adsf").toString())
    }
  }

  def list = Action {
    Ok(views.html.questionList(questionService.findAll))
  }

  def view(questionID:Int) = Action {
    questionService.findByID(questionID) match {
      case Some(q) => Ok(views.html.questionView(q))
      case None => NotFound
    }
  }
}
