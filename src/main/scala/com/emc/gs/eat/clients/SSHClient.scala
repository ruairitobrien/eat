package com.emc.gs.eat.clients

import java.io.File
import java.util.UUID

import com.decodified.scalassh.HostKeyVerifiers.DontVerify
import com.decodified.scalassh._
import com.emc.gs.eat.actors.{AnalyzeHostError, AnalyzeHostResult}
import com.emc.gs.eat.host.Host

class SSHClient(nixGrabLocation: String, outputDir: String) {

  val installDir = "/tmp"

  def runSSHGrab(host: Host, index: Int): Either[AnalyzeHostError, AnalyzeHostResult] = {
    println("running Linux")
    val grabId = UUID.randomUUID().toString
    val grabLocation = installDir + "/" + grabId
    val hostConfig = new HostConfig(new PasswordLogin(host.username, new SimplePasswordProducer(host.password)), host.address, 22, None, None, None, false, DontVerify)

    val nixGrabMap = buildNixGrabMap(nixGrabLocation + File.separator)


    determineOsType(host, hostConfig) match {
      case Right(os) =>
        val grab = nixGrabLocation + File.separator + nixGrabMap(os)
        val uploadLocation = installDir + "/" + new File(grab).getName
        uploadGrab(host, hostConfig, grab) match {
          case Right(x) =>
            if (x.contains(new File(grab).getName))
              executeGrab(host, hostConfig, grabLocation, uploadLocation) match {
                case Right(y) =>
                  if (y ==(Some(0), Some(0)))
                    downloadGrab(host, hostConfig, grabLocation) match {
                      case Right(_) => cleanup(host, hostConfig, grabLocation, uploadLocation) match {
                        case Right(_) => Right(new AnalyzeHostResult(host, "%s grab processed successfully using ssh".format(host.address)))
                        case Left(a) => throw new RuntimeException(a)
                      }
                      case Left(z) => throw new RuntimeException(z)
                    }
                  else
                    throw new RuntimeException("Failed to execute grab on host")

                case Left(y) => throw new RuntimeException(y)
              }
            else
              throw new RuntimeException("Failed to upload grab to host")
          case Left(x) => throw new RuntimeException(x)

        }

      case Left(err) => throw new RuntimeException(err)
    }

  }

  def uploadGrab(host: Host, hostConfig: HostConfig, grab: String): Either[String, String] = {
    SSH(host.address, hostConfig) { client =>
      try client.upload(grab, installDir + "/").right.flatMap { _ =>
        client.exec("ls " + installDir).right.map { result =>
          result.stdOutAsString()
        }
      } finally client.close()
    }
  }

  def executeGrab(host: Host, hostConfig: HostConfig, grabLocation: String, uploadLocation: String): Either[String, (Option[Int], Option[Int])] = {
    SSH(host.address, hostConfig) { client =>
      client.exec("rm -rf " + installDir + "/emcgrab").right.flatMap { rmOldGrabRes =>
        client.exec("cd " + installDir + " && tar -xvf " + uploadLocation + " && cd emcgrab && chmod +x ./emcgrab.sh && ./emcgrab.sh -lite -autoexec -legal -OUTDir " + grabLocation).right.map { runGrabRes =>
          (rmOldGrabRes.exitCode, runGrabRes.exitCode)
        }
      }
    }
  }

  def downloadGrab(host: Host, hostConfig: HostConfig, grabLocation: String): Either[String, _] = {
    SSH(host.address, hostConfig) { client =>
      try client.download(grabLocation + "/", outputDir).right.map { _ =>
      } finally client.close()
    }
  }

  def cleanup(host: Host, hostConfig: HostConfig, grabLocation: String, uploadLocation: String): Either[String, (Option[Int], Option[Int], Option[Int])] = {
    SSH(host.address, hostConfig) { client =>
      client.exec("rm -rf " + installDir + "/emcgrab").right.flatMap { rmGrabRes =>
        client.exec("rm -rf " + uploadLocation).right.flatMap { rmUploadRes =>
          client.exec("rm -rf " + grabLocation + "/").right.map { finalRes =>
            (rmGrabRes.exitCode, rmUploadRes.exitCode, finalRes.exitCode)
          }
        }
      }
    }
  }

  def determineOsType(host: Host, hostConfig: HostConfig): Either[String, String] = {
    SSH(host.address, hostConfig) { client =>
      client.exec("uname -s").right.map { os =>
        os.stdOutAsString().filter(_ >= ' ')
      }
    }
  }


  def buildNixGrabMap(path: String): Map[String, String] = {
    var nixGrabMap = Map[String, String]()
    val osList = List("SunOS", "SOLARIS", "AIX", "Linux", "HP-UX")
    new File(path).listFiles.foreach(file => osList.foreach(os =>
      if (file.getName.contains(os))
        nixGrabMap += os -> file.getName
    ))
    nixGrabMap
  }

}
