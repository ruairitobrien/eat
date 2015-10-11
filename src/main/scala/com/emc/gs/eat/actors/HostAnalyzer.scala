package com.emc.gs.eat.actors

import java.io.File

import akka.actor.Actor
import akka.event.Logging
import com.emc.gs.eat.host.{Host, HostAnalysisResult}


class HostAnalyzer extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case AnalyzeHost(host, outputDir) => sender ! Result(analyzeHost(host, outputDir))
    case _ => log.warning("An unexpected message was received by HostAnalyzer")
  }

  def analyzeHost(host: Host, outputDir: File): HostAnalysisResult = {
    new HostAnalysisResult()
  }

}
