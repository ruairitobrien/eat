package com.emc.gs.eat.actors

import akka.actor.Actor
import akka.event.Logging
import com.emc.gs.eat.Config
import com.emc.gs.eat.clients.{EsxiClient, SSHClient, WindowsClient}
import com.emc.gs.eat.host.{Host, HostAnalysisResult}

/**
 * This actor is in charge of connecting to a host and doing any analysis i.e. executing a grab.
 * Any errors will be reported back to the Master which is responsible for reporting that error.
 */
class HostAnalyzer extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case AnalyzeHost(host, index, config) =>
      try {
        sender ! AnalyzeHostResult(analyzeHost(host, index, config))
      } catch {
        case t: Throwable =>
          println(t)
          sender ! AnalyzeHostError(host, "An error occurred processing the host", Some(t))
      }
    case _ => log.warning("An unexpected message was received by HostAnalyzer")
  }

  /**
   * Run analysis on a host and output the data.
   *
   * @param host the host connection details
   * @param index the index used to ensure uniqueness of output file name
   * @param config the configuration for host analysis
   * @return the host analysis result for feedback to the parent actor
   */
  def analyzeHost(host: Host, index: Int, config: Config): HostAnalysisResult = {
    validateHost(host)

    if (host.os.toLowerCase == "esxi") {
      new EsxiClient(config.esxiGrabLocation, config.out.getAbsolutePath).runEsxiGrab(host, index)
    } else if (host.os.toLowerCase == "windows") {
      new WindowsClient(config.wmiClientLocation, config.out.getAbsolutePath).runWindowsGrab(host, index)
    } else if (host.os.toLowerCase == "linux") {
      new SSHClient(config.nixGrabLocation, config.out.getAbsolutePath).runSSHGrab(host, index)
    } else {
      throw new RuntimeException("Invalid operating system provided")
    }
  }

  def validateHost(host: Host): Unit = {
    // TODO: do I need to validate anything here?
  }


}
