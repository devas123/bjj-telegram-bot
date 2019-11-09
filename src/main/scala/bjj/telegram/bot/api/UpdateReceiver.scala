package bjj.telegram.bot.api

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import bjj.telegram.bot.model._
import com.google.common.cache.Cache

private[bot] class UpdateReceiver(token: String, private val idCache: Cache[String, String])
  extends GraphStage[FlowShape[Update, Option[Message]]] {
  val marker = "marker"
  val in: Inlet[Update] = Inlet[Update]("UpdateReceiver.in")
  val out: Outlet[Option[Message]] = Outlet[Option[Message]]("UpdateReceiver.out")
  override val shape = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    var message: Option[Message] = None
    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        pull(in)
      }
    })
    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val update = grab(in)
        idCache.asMap().compute(update.update_id.toString, (_, b) => {
          if (b == null) {
            message = update.message
            emit(out, message)
            marker
          } else {
            emit(out, None)
            b
          }
        })
      }
    })
  }
}
