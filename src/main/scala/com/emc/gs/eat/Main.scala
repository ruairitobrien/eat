package com.emc.gs.eat

import akka.actor.{ActorSystem, Props}
import com.emc.gs.eat.actors.{EatMaster, HostAnalysisListener, ProcessHosts}
import com.emc.gs.eat.input.CommandLineInput

/**
 * Entry point for EAT application.
 * Parses any command line options and either prints help information or executes configured work based on arguments.
 */
object Main extends App {

  initialize()

  def initialize(): Unit = {
    val parser = CommandLineInput.createCommandLineOptionParser()
    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
      case Some(config) => processHosts(config)

      case None => println()
      // arguments are bad, error message will have been displayed
    }
  }

  def processHosts(config: Config): Unit = {
    val system = ActorSystem("EatSystem")

    val listener = system.actorOf(Props[HostAnalysisListener], name = "hostAnalysisListener")

    val master = system.actorOf(Props(new EatMaster(config, listener)),
      name = "eatMaster")

    master ! ProcessHosts
  }

}
