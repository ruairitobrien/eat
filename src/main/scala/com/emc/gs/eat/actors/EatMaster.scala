package com.emc.gs.eat.actors

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.routing.RoundRobinRouter
import com.emc.gs.eat.host.{Host, HostAnalysisResult}
import com.emc.gs.eat.input.InputParser

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

  def receive = {
    case ProcessHosts =>
      parseHostDataAndInitiateAnalysis()
    case Result(hostAnalysisResult) =>
      processResult(hostAnalysisResult)
    case _ =>
      log.warning("Invalid message received by EatMaster")
  }

  /**
   * Handle results from individual host analysis
   *
   * @param hostAnalysisResult the result from a host analysis
   */
  def processResult(hostAnalysisResult: HostAnalysisResult): Unit = {
    nrOfHostsProcessed += 1
    log.info("Result received")
    if (nrOfHostsProcessed == nrOfHosts) {
      listener ! AnalysisComplete(duration = (System.currentTimeMillis - start).millis)
      context.stop(self)
    }
  }

  /**
   * Processes the host input data and creates an actor for each host provided which runs analysis on that host.
   */
  def parseHostDataAndInitiateAnalysis(): Unit = {
    val hosts = InputParser.parseCsvFiles(hostFiles).map(hostInfo => parseHost(hostInfo))
    nrOfHosts = hosts.length
    for (host <- hosts) hostAnalysisRouter ! AnalyzeHost(host, outputDir)
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
