package com.emc.gs.eat.actors

import akka.actor.Actor
import akka.event.Logging


class HostAnalysisListener extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case AnalysisComplete(duration) =>
      println("\n\tAll done: \t\t\n\tRunning time: \t%s"
        .format(duration))
      context.system.shutdown()
    case _ => log.warning("An unexpected message was received by HostAnalysisListener")
  }
}
