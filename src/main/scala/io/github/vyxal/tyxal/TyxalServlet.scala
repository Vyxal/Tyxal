package io.github.vyxal.tyxal

import vyxal.Interpreter
import vyxal.parsing.Lexer
import vyxal.Context

import org.slf4j.{Logger, LoggerFactory}

import org.apache.pekko.actor.Actor
import org.apache.pekko.actor.ActorSystem
import sttp.client3._

import org.scalatra._
import org.json4s.jvalue2extractable
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

import org.scalatra.swagger._


import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import vyxal.MiscHelpers
import vyxal.Globals
import scala.collection.mutable.ArrayBuffer
import org.apache.pekko.actor.ActorRef
import org.apache.pekko.pattern.ask
import org.apache.pekko.pattern.AskTimeoutException
import scala.util.control.NonFatal
import vyxal.Inputs
import vyxal.Settings

case class RunVyxalMessage(
    val code: String,
    val inputs: List[String] = List.empty,
    val literate: Boolean = false,
    val flags: String = ""
)

case class RunVyxalResponse(
    val output: List[String],
    val error: Option[String]
)

class TyxalServlet(val system: ActorSystem, val runActor: ActorRef)(using val swagger: Swagger)
    extends ScalatraServlet
    with NativeJsonSupport
    with FutureSupport
    with SwaggerSupport {

  val logger = LoggerFactory.getLogger(getClass)
  protected val applicationDescription = "Tyxal: for all your Vyxal tooling needs."
  given jsonFormats: Formats = DefaultFormats
  given executor: ExecutionContext = system.dispatcher
  
  before() {
    contentType = formats("json")
  }

  get("/") {
    Map(
      "vyxal" -> Map(
        "version" -> "3.0.0",
        "awesome" -> true
      )
    )
  }

  post("/run", operation(
    apiOperation[RunVyxalResponse]("run")
    schemes "POST"
    summary "Run some Vyxal code"
    consumes "application/json"
    responseMessage ResponseMessage(400, "An error occured while running the program, which will be included in the `error` key of the response. The `output` key may have partial output.")
    responseMessage ResponseMessage(408, "Execution timed out. The `output` key will always be empty. The `error` key will always contain the text \"Execution timed out\".")
    parameter(bodyParam[String]("code").required description("The code to run") example("1 1 +"))
    parameter(bodyParam[List[String]]("inputs") description("Inputs to pass to the code"))
    parameter(bodyParam[Boolean]("literate") description("Enable literate mode"))
    parameter(bodyParam[String]("flags") description("Flags to pass to the interpreter"))
  )) {
    parsedBody.extractOpt[RunVyxalMessage] match
        case None => BadRequest
        case Some(message) => 
          if (message.code.length() > 1024)
            RequestEntityTooLarge
          else
            try
              runActor.ask(message)(10.seconds).asInstanceOf[Future[RunVyxalResponse]].map((result) =>
                result.error match
                  case Some(error) => BadRequest(result)
                  case None => result
              )
            catch
              case e: AskTimeoutException => RequestTimeout(RunVyxalResponse(List.empty, Some("Execution timed out")))
  }

  post("/deliterateify", operation(
    apiOperation[String]("deliterateify")
    schemes "POST"
    summary "Convert literate code into SBCS form"
    consumes "text/plain"
    produces "text/plain"
    responseMessage ResponseMessage(400, "The input code was invalid. The body will be the error message produced by the lexer.")
    parameter(bodyParam[String]("code").required description("The code to deliterateify"))
  )) {
    Lexer.lexLiterate(request.body) match
      case Left(error) => halt(400, error.msg)
      case Right(tokens) => Lexer.sbcsify(tokens)
  }

}

class RunVyxalActor extends Actor {
  val logger = LoggerFactory.getLogger(getClass)
  def receive: Actor.Receive =
    case RunVyxalMessage(code, inputs, literate, flags) =>
      val inputList = inputs.reverse.map(MiscHelpers.eval(_)(using Context()))
      val output = ArrayBuffer.empty[String]
      var error: Option[String] = None

      given vyCtx: Context = Context(
        inputs = inputList.toIndexedSeq,
        globals = Globals(
          settings = Settings(
            online = true,
            literate = literate
          ).withFlags(flags.toCharArray.toList),
          inputs = Inputs(inputList),
          printFn = output.addOne
        )
      )
      vyCtx.setVar("tyxal", "awesome")

      try Interpreter.execute(code)
      catch case NonFatal(e) => error = Some(e.getMessage)
      sender() ! RunVyxalResponse(output.toList, error)
}
