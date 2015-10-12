package com.emc.gs.eat.actors

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.routing.RoundRobinRouter
import com.emc.gs.eat.host.{Host, HostAnalysisResult}
import com.emc.gs.eat.input.HostInputParser
import com.github.tototoshi.csv.CSVWriter

import scala.concurrent.duration._

/**
 * Master actor to coordinate the parsing of input, analyses of host and providing results.
 *
 * @param hostFiles one or more input files containing host data
 * @param outputDir where all output should be written
 * @param listener the listener actor that handles the final output
 */
class EatMaster(hostFiles: Seq[File], outputDir: File, listener: ActorRef)
  extends Actor {

  val log = Logging(context.system, this)
  val start: Long = System.currentTimeMillis
  val hostAnalysisRouter = context.actorOf(
    Props[HostAnalyzer].withRouter(RoundRobinRouter(4)), name = "hostAnalysisRouter")
  var nrOfHosts: Int = _
  var nrOfHostsProcessed: Int = _
  var errors: Seq[List[String]] = Seq()

  def receive = {
    case ProcessHosts =>
      parseHostDataAndInitiateAnalysis()
    case AnalyzeHostResult(hostAnalysisResult) =>
      processResult(hostAnalysisResult)
    case error@AnalyzeHostError(host, message, thrown) => processError(error)
    case _ =>
      log.warning("Invalid message received by EatMaster")
  }

  /**
   * If an error occurs, store it for logging
   *
   * @param error the error information for logging
   */
  def processError(error: AnalyzeHostError): Unit = {
    log.error("An error occurred connecting to %s".format(error.host.address))
    errors = errors :+ List(error.host.address)
    incrementAndCheckProcessedHosts()
  }

  /**
   * Handle results from individual host analysis
   *
   * @param hostAnalysisResult the result from a host analysis
   */
  def processResult(hostAnalysisResult: HostAnalysisResult): Unit = {
    log.info("Result received")
    incrementAndCheckProcessedHosts()
  }

  def incrementAndCheckProcessedHosts(): Unit = {
    nrOfHostsProcessed += 1
    if (nrOfHostsProcessed == nrOfHosts) {
      if (errors.nonEmpty) {
        try {
          val errorOutputFile = new File(outputDir, "hostErrors.csv")
          val writer = CSVWriter.open(errorOutputFile)
          try {
            writer.writeAll(errors)
          } finally {
            writer.close()
          }
        } catch {
          case e: Exception => log.error("An error occurred writing the error log file", e)
        }
      }
      listener ! AnalysisComplete(duration = (System.currentTimeMillis - start).millis)
      context.stop(self)
    }
  }

  /**
   * Processes the host input data and creates an actor for each host provided which runs analysis on that host.
   */
  def parseHostDataAndInitiateAnalysis(): Unit = {
    try {

      val hosts = HostInputParser.parseCsvFiles(hostFiles).map(hostInfo => parseHost(hostInfo))
      nrOfHosts = hosts.length
      for ((host, index) <- hosts.zipWithIndex) hostAnalysisRouter ! AnalyzeHost(host, index, outputDir)

    } catch {
      case e: Exception =>
        listener ! AnalysisFailed("An error occurred processing the Host input CSV: \n\t%s".format(e.getMessage))
        context.stop(self)
    }
  }

  /**
   * Convert a line of CSV to a host
   *
   * @param hostInfo the host values contained in a line of CSV
   * @return a host populated with values form CSV
   */
  def parseHost(hostInfo: List[String]): Host = {
    val address = hostInfo.head
    new Host(address)
  }

}
