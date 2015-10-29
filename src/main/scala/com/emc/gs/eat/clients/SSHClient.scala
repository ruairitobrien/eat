package com.emc.gs.eat.clients

import java.io.File
import java.util.UUID

import com.decodified.scalassh.HostKeyVerifiers.DontVerify
import com.decodified.scalassh._
import com.emc.gs.eat.host.{Host, HostAnalysisResult}

class SSHClient(nixGrabLocation: String, outputDir: String) {

  def runSSHGrab(host: Host, index: Int): HostAnalysisResult = {
    println("running Linux")
    val installDir = "/tmp"
    val uploadLocation = installDir + "/" + new File(nixGrabLocation).getName
    val grabId = UUID.randomUUID().toString
    val grabLocation = installDir + "/" + grabId
    val hostConfig = new HostConfig(new PasswordLogin(host.username, new SimplePasswordProducer(host.password)), host.address, 22, None, None, None, false, DontVerify)
    val grab = nixGrabLocation + File.separator + "emcgrab_Linux_v4.7.1.tar"

    val uploaded = SSH(host.address, hostConfig) { client =>
      try client.upload(grab, installDir + "/").right.flatMap { _ =>
        client.exec("ls " + installDir).right.map { result =>
          result.stdOutAsString()
        }
      } finally client.close()
    } match {
      case Right(x) => x.contains(new File(grab).getName)
      case Left(x) => throw new RuntimeException(x)
    }

    if (uploaded) {
      val executed = SSH(host.address, hostConfig) { client =>
        client.exec("rm -rf " + installDir + "/emcgrab").right.flatMap { rmOldGrabRes =>
          client.exec("cd " + installDir + " && tar -xvf " + uploadLocation + " && cd emcgrab && chmod +x ./emcgrab.sh && ./emcgrab.sh -lite -autoexec -legal -OUTDir " + grabLocation).right.map { runGrabRes =>
            (rmOldGrabRes.exitCode, runGrabRes.exitCode)
          }
        }
      } == Right((Some(0), Some(0)))

      if (executed) {
        SSH(host.address, hostConfig) { client =>
          try client.download(grabLocation + "/", outputDir).right.map { _ =>
          } finally client.close()
        }
      } else {
        throw new RuntimeException("An error occurred running the grab")
      }



      val cleanedUp = SSH(host.address, hostConfig) { client =>
        client.exec("rm -rf " + installDir + "/emcgrab").right.flatMap { rmGrabRes =>
          client.exec("rm -rf " + uploadLocation).right.flatMap { rmUploadRes =>
            client.exec("rm -rf " + grabLocation + "/").right.map { finalRes =>
              (rmGrabRes.exitCode, rmUploadRes.exitCode, finalRes.exitCode)
            }
          }
        }
      } == Right((Some(0), Some(0), Some(0)))

      val copied = new File(outputDir).exists()

      if (!copied) {
        throw new RuntimeException("Linux grab failed to download")
      }
      if (!cleanedUp) {
        throw new RuntimeException("Grab completed but there was an error cleaning up the host afterwards.")
      }

      /** sys.process.Process(Seq(
        "mv", outputDir + File.separator + grabId + File.separator + grabFile,  outputDir + File.separator + grabFile), new File(outputDir)).!
      sys.process.Process(Seq(
        "@RD", "/S", "/Q", outputDir + File.separator + grabId), new File(outputDir)).! */

    } else {
      throw new RuntimeException("Failed to upload Linux grab")
    }



    new HostAnalysisResult("%s grab processed successfully using ssh".format(host.address))
  }
}
