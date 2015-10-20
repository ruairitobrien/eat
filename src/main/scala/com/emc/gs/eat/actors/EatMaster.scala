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
    errors = errors :+ List(
      error.host.address,
      error.host.os,
      error.host.username,
      error.host.password,
      error.message
    )
    incrementAndCheckProcessedHosts()
  }

  /**
   * Handle results from individual host analysis
   *
   * @param hostAnalysisResult the result from a host analysis
   */
  def processResult(hostAnalysisResult: HostAnalysisResult): Unit = {
    incrementAndCheckProcessedHosts()
  }

  /**
   * Increments the number of processed hosts.
   * If the number of processed hosts is equal to the total number of hosts, the finishing protocol will be executed.
   */
  def incrementAndCheckProcessedHosts(): Unit = {
    nrOfHostsProcessed += 1
    if (nrOfHostsProcessed == nrOfHosts) {
      if (errors.nonEmpty) {
        writeErrorHostRecord(errors)
      }
      listener ! AnalysisComplete(duration = (System.currentTimeMillis - start).millis)
      context.stop(self)
    }
  }

  /**
   * Any errors that were recorded will be written to a CSV file for inspection.
   *
   * @param hostErrors recorded errors which should have host data and an error message
   */
  def writeErrorHostRecord(hostErrors: Seq[List[String]]): Unit = {
    val hostErrorFileName = "hostErrors.csv"
    try {
      val errorOutputFile = new File(outputDir, hostErrorFileName)
      val writer = CSVWriter.open(errorOutputFile)
      try {
        writer.writeRow(List(Host.ADDRESS_KEY, Host.OS_KEY, Host.PROTOCOL_KEY, Host.USERNAME_KEY, Host.PASSWORD_KEY, Host.ERROR_KEY))
        writer.writeAll(hostErrors)
      } finally {
        writer.close()
      }
    } catch {
      case e: Exception => log.error("An error occurred writing the error log file", e)
    }
  }

  /**
   * Processes the host input data and creates an actor for each host provided which runs analysis on that host.
   */
  def parseHostDataAndInitiateAnalysis(): Unit = {
    try {
      val hosts = HostInputParser.parseInputToHosts(hostFiles)
      nrOfHosts = hosts.length
      for ((host, index) <- hosts.zipWithIndex) hostAnalysisRouter ! AnalyzeHost(host, index, outputDir)

    } catch {
      case e: Exception =>
        listener ! AnalysisFailed("An error occurred processing the Host input CSV: \n\t%s".format(e.getMessage))
        context.stop(self)
    }
  }

}
