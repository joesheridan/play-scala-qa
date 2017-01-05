package controllers

import javax.inject._

import anorm._
import play.api.db.Database
import play.api.mvc._
import models.{Answer, AnswerService}
import scala.util.{Failure, Success, Try}

/**
  * Created by joe on 30/12/2016.
  */
@Singleton
class AnswerController @Inject() (db: Database, answerService: AnswerService) extends Controller {

  // initialise the answerservice
  answerService.onStartUp

  def add(questionID:Int) = Action { request =>
    println("posting answer on questionID: "+questionID)
    val ansStr = (request.body).asFormUrlEncoded match {
      case Some(map) => Some(map("answer").head)
      case None => None
    }
    answerService.create(Answer(6, ansStr.getOrElse("asdfasdfs"), questionID)) match {
      case Success(Some(a)) => Redirect("/question/"+questionID)
      case Success(None) => { NotFound }
      case Failure(e) => { println("error creating answer:"+e); NotFound }
    }

  }

}
