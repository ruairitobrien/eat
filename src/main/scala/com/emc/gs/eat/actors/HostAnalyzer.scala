package com.emc.gs.eat.actors

import java.io.File

import akka.actor.Actor
import akka.event.Logging
import com.emc.gs.eat.host.{Host, HostAnalysisResult}

import scala.sys.process._

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

    if (host.os.toLowerCase == "esxi") {
      runEsxiGrab(host)
    } else if (host.os.toLowerCase == "windows") {
      runWindowsGrab(host)
    } else if (host.os.toLowerCase == "linux") {
      runLinuxGrab(host)
    } else {
      throw new RuntimeException("Invalid operating system provided")
    }

    /** val r = scala.util.Random
    Thread.sleep(r.nextInt(2000))
    if (r.nextBoolean())
      throw new RuntimeException("Imagine something bad happened connecting to a host or processing a grab")

    val output = new File(outputDir, "%s-%s-pretend-i-am-a-grab".format(index, host.address))
    output.createNewFile()
      */
  }

  def validateHost(host: Host): Unit = {
    // TODO: do I need to validate anything here?
  }

  def runEsxiGrab(host: Host): HostAnalysisResult = {
    val res: Int = "C:\\Users\\obrier3\\Desktop\\EAT-DEMO\\app\\EMC-ESXi-GRAB-1.3.6\\emcgrab.bat -host 10.73.58.3 -user root -password emc2002 -autoexec -legal -vmsupport –noclariion" !

    new HostAnalysisResult()
  }

  def runWindowsGrab(host: Host): HostAnalysisResult = {
    new HostAnalysisResult()
  }

  def runLinuxGrab(host: Host): HostAnalysisResult = {
    new HostAnalysisResult()
  }

}
