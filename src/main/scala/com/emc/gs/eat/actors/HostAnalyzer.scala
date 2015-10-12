package com.emc.gs.eat.actors

import java.io.File

import akka.actor.Actor
import akka.event.Logging
import com.emc.gs.eat.host.{Host, HostAnalysisResult}

/**
 * This actor is in charge of connecting to a host and doing any analysis i.e. executing a grab.
 * Any errors will be reported back to the Master which is responsible for reporting that error.
 */
class HostAnalyzer extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case AnalyzeHost(host, index, outputDir) =>
      try {
        sender ! AnalyzeHostResult(analyzeHost(host, index, outputDir))
      } catch {
        case t: Throwable =>
          sender ! AnalyzeHostError(host, "An error occurred processing the host", Some(t))
      }
    case _ => log.warning("An unexpected message was received by HostAnalyzer")
  }

  /**
   * Run analysis on a host and output the data.
   *
   * @param host the host connection details
   * @param index the index used to ensure uniqueness of output file name
   * @param outputDir the directory to write out files to
   * @return the host analysis result for feedback to the parent actor
   */
  def analyzeHost(host: Host, index: Int, outputDir: File): HostAnalysisResult = {
    validateHost(host)
    val r = scala.util.Random
    Thread.sleep(r.nextInt(2000))
    if (r.nextBoolean())
      throw new RuntimeException("Imagine something bad happened connecting to a host or processing a grab")

    val output = new File(outputDir, "%s-%s-pretend-i-am-a-grab".format(index, host.address))
    output.createNewFile()

    new HostAnalysisResult()
  }

  def validateHost(host: Host): Unit = {
    // TODO: do I need to validate anything here?
  }

}
