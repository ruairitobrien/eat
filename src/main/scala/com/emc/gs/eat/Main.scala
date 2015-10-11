package com.emc.gs.eat

import java.io.File

import akka.actor.{ActorSystem, Props}
import com.emc.gs.eat.actors.{EatMaster, HostAnalysisListener, ProcessHosts}

/**
 * Entry point for EAT application.
 * Parses any command lien options and either prints help information or executes configured work based on arguments.
 */
object Main extends App {

  initialize()


  def initialize(): Unit = {
    val parser = new scopt.OptionParser[Config]("eat") {
      head("eat", "1.x")
      opt[File]('o', "out") required() valueName "<file>" action { (x, c) =>
        c.copy(out = x)
      } text "An output directory where grabs and other output files will be places. If the directory doesn't exist it will be created."
      opt[Seq[File]]('i', "in") required() valueName "<file1.csv>,<file2.csv>..." action { (x, c) =>
        c.copy(hostFiles = x)
      } text "Required csv host data to process. Can be provided in one or more files."
      opt[Unit]("verbose") action { (_, c) =>
        c.copy(verbose = true)
      } text "print verbose output"
      opt[Unit]("debug") hidden() action { (_, c) =>
        c.copy(debug = true)
      } text "start application in debug mode"
      help("help") text "display available commands and information"
    }
    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
      case Some(config) => processHosts(config)

      case None => println()
      // arguments are bad, error message will have been displayed
    }
  }

  def processHosts(config: Config): Unit = {
    println(config)
    val system = ActorSystem("EatSystem")

    val listener = system.actorOf(Props[HostAnalysisListener], name = "hostAnalysisListener")

    val master = system.actorOf(Props(new EatMaster(config.hostFiles, config.out, listener)),
      name = "eatMaster")

    master ! ProcessHosts

  }
}
