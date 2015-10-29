package com.emc.gs.eat.actors

import akka.actor.Actor
import akka.event.Logging

/**
 * Listens for the final outcome of all analysis and reports back to the user.
 */
class HostAnalysisListener extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case AnalysisComplete(duration) =>
      println("\n\tAll done: \t\t\n\tRunning time: \t%s"
        .format(duration))
      context.system.shutdown()
      System.exit(0)
    case AnalysisFailed(message) =>
      println(message)
      context.system.shutdown()
      System.exit(0)
    case _ =>
      log.warning("An unexpected message was received by HostAnalysisListener")
      System.exit(1)
  }
}
