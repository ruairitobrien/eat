package com.emc.gs.eat.clients

import java.util.UUID

import com.emc.gs.eat.actors.AnalyzeHostResult
import com.emc.gs.eat.host.Host
import com.emc.gs.eat.util.FileUtil

class EsxiClient(esxiGrabLocation: String, outputDir: String) {

  def runEsxiGrab(host: Host, index: Int): AnalyzeHostResult = {
    println("running ESXi")
    val temp = FileUtil.createTempDir(UUID.randomUUID().toString)
    try {
      temp.mkdir()
      val res: Int = sys.process.Process(Seq(
        esxiGrabLocation,
        "-host", host.address,
        "-user", host.username,
        "-password", host.password,
        "-autoexec", "-legal", "-noclariion", "-quiet",
        "-outDir", outputDir), temp).!

      if (res != 0) throw new RuntimeException("ESXi Grab returned an error.")

    } finally {
      if (temp.exists()) temp.delete()
    }

    new AnalyzeHostResult(host, "%s grab processed successfully using ESXi Grab".format(host.address))
  }

}
