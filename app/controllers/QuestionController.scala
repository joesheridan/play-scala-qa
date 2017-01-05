package controllers

import javax.inject._

import anorm.SQL
import play.api.db.Database
import play.api.mvc._
import models.{Answer, AnswerService, Question, QuestionService}

import scala.util.{Failure, Success, Try}

/**
  * Created by joe on 30/12/2016.
  */
@Singleton
class QuestionController @Inject() (db: Database, questionService: QuestionService, answerService: AnswerService) extends Controller {

  // initialise the questionservice
  questionService.onStartUp

  def createQuestion() = Action { request =>
    println("creating question")
    (request.body).asFormUrlEncoded match {
      case Some(map) => {
         questionService.create(Question(1, map("question").head)) match {
           case Success(Some(qID)) => Redirect("/question/" + qID)
           case Success(None) => NotFound
           case Failure(e) => { println("error creating question:"+e); NotFound }
         }
      }
      case None => { NotFound }
    }
  }

  def displayAddNew = Action {
      Ok(views.html.questionAdd())
  }

  def list = Action {
    questionService.findAll match {
      case Success(qs) => Ok(views.html.questionList(qs))
      case Failure(e) => { println(e); NotFound }
    }
  }

  def view(questionID:Int) = Action {
    questionService.findByID(questionID) match {
      case Success(q) => {
        answerService.getAnswers(q) match {
          case Success(answers) => Ok(views.html.questionView(q, answers))
          case Failure(e) => Ok(views.html.questionView(q, null))
        }

      }
      case Failure(e) => { println(e); NotFound }
    }
  }
}
