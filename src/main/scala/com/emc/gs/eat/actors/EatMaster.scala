package com.emc.gs.eat.actors

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.routing.RoundRobinRouter
import com.emc.gs.eat.Config
import com.emc.gs.eat.host.Host
import com.emc.gs.eat.input.HostInputParser
import com.github.tototoshi.csv.CSVWriter

import scala.concurrent.duration._

/**
 * Master actor to coordinate the parsing of input, analyses of host and providing results.
 *
 * @param config the configuration for running the program
 * @param listener the listener actor that handles the final output
 */
class EatMaster(config: Config, listener: ActorRef)
  extends Actor {

  val log = Logging(context.system, this)
  val start: Long = System.currentTimeMillis
  val hostAnalysisRouter = context.actorOf(
    Props[HostAnalyzer].withRouter(RoundRobinRouter(config.workers)), name = "hostAnalysisRouter")
  var nrOfHosts: Int = _
  var nrOfHostsProcessed: Int = _
  var errors: Seq[List[String]] = Seq()

  def receive = {
    case ProcessHosts =>
      parseHostDataAndInitiateAnalysis()
    case AnalyzeHostResult(host, message) =>
      processResult(host, message)
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
      error.message + " - cause: " + error.error.getOrElse("Unknown error")
    )
    incrementAndCheckProcessedHosts()
  }

  /**
   * Handle results from individual host analysis
   *
   * @param host the host for which the result is from
   * @param message message about the analysisi result
   */
  def processResult(host: Host, message: String): Unit = {
    println(message)
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
      val errorOutputFile = new File(config.out, hostErrorFileName)
      if (errorOutputFile.exists()) errorOutputFile.delete()
      val writer = CSVWriter.open(errorOutputFile)
      try {
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
      val hosts = HostInputParser.parseInputToHosts(config.hostFiles)
      nrOfHosts = hosts.length
      if (nrOfHosts == 0) listener ! AnalysisFailed("No valid hosts were provided.")
      for ((host, index) <- hosts.zipWithIndex) hostAnalysisRouter ! AnalyzeHost(host, index, config)
    } catch {
      case e: Exception =>
        listener ! AnalysisFailed("An error occurred processing the Host input CSV: \n\t%s".format(e.getMessage))
        context.stop(self)
    }
  }

}
