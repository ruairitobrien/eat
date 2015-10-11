package com.emc.gs.eat.actors

import java.io.File

import com.emc.gs.eat.host.{Host, HostAnalysisResult}

import scala.concurrent.duration.Duration

sealed trait HostAnalysisMessage

case object ProcessHosts extends HostAnalysisMessage

case class AnalyzeHost(host: Host, out: File) extends HostAnalysisMessage

case class Result(hostAnalysisResult: HostAnalysisResult) extends HostAnalysisMessage

case class AnalysisComplete(duration: Duration) extends HostAnalysisMessage

