import io.github.vyxal.tyxal._
import org.scalatra._
import jakarta.servlet.ServletContext
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.Props
import org.scalatra.swagger.Swagger

class ScalatraBootstrap extends LifeCycle {
  private val system = ActorSystem()
  private val runActor = system.actorOf(Props.apply[RunVyxalActor])

  given Swagger = new TyxalSwagger()
  override def init(context: ServletContext): Unit = {
    context.mount(new TyxalServlet(system, runActor), "/", "tyxal")
    context.mount(new ResourcesApp, "/docs")
  }
}
