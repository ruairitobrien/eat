package com.emc.gs.eat.clients

import java.util.UUID

import com.emc.gs.eat.actors.{AnalyzeHostError, AnalyzeHostResult}
import com.emc.gs.eat.host.Host
import com.emc.gs.eat.util.FileUtil

class WindowsClient(wmiClientLocation: String, outputDir: String) {

  def runWindowsGrab(host: Host, index: Int): Either[AnalyzeHostError, AnalyzeHostResult] = {
    println("running Windows")
    val temp = FileUtil.createTempDir(UUID.randomUUID().toString)
    try {
      temp.mkdir()
      val res: Int = sys.process.Process(Seq(
        wmiClientLocation,
        host.address,
        host.username,
        host.password,
        outputDir), temp).!
      if (res != 0) throw new RuntimeException("WMI client returned an error.")
    } finally {
      if (temp.exists()) temp.delete()
    }

    Right(new AnalyzeHostResult(host, "%s grab processed successfully using windows client".format(host.address)))
  }

}
