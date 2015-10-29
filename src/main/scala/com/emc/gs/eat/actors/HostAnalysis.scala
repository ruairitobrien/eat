package com.emc.gs.eat.actors

import com.emc.gs.eat.Config
import com.emc.gs.eat.host.Host

import scala.concurrent.duration.Duration

sealed trait HostAnalysisMessage

case object ProcessHosts extends HostAnalysisMessage

case class AnalyzeHost(host: Host, index: Int, config: Config) extends HostAnalysisMessage

case class AnalyzeHostResult(host: Host, message: String) extends HostAnalysisMessage

case class AnalyzeHostError(host: Host, message: String, error: Option[Throwable]) extends HostAnalysisMessage

case class AnalysisComplete(duration: Duration) extends HostAnalysisMessage

case class AnalysisFailed(message: String) extends HostAnalysisMessage

