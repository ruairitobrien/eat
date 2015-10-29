package com.emc.gs.eat.clients

import java.io.File
import java.util.UUID

import com.decodified.scalassh.HostKeyVerifiers.DontVerify
import com.decodified.scalassh._
import com.emc.gs.eat.actors.AnalyzeHostResult
import com.emc.gs.eat.host.Host

class SSHClient(nixGrabLocation: String, outputDir: String) {

  val installDir = "/tmp"

  /**
   * COnnect to the host over SSH. Determine the correct grab to use. Run it and downlaod the results.
   *
   * @param host the host to run the grab on
   * @param index the index of the host int he csv input
   * @return the analysis result
   */
  def runSSHGrab(host: Host, index: Int): AnalyzeHostResult = {
    println("running Linux")
    val grabId = UUID.randomUUID().toString
    val grabLocation = installDir + "/" + grabId
    val hostConfig = new HostConfig(new PasswordLogin(host.username, new SimplePasswordProducer(host.password)), host.address, 22, None, None, None, false, DontVerify)

    val nixGrabMap = buildNixGrabMap(nixGrabLocation + File.separator)

    SSH(host.address, hostConfig) { client =>
      try {
        determineOsType(client) match {
          case Right(os) =>
            val grab = nixGrabLocation + File.separator + nixGrabMap(os)
            val uploadLocation = installDir + "/" + new File(grab).getName
            uploadGrab(client, grab) match {
              case Right(x) =>
                if (x.contains(new File(grab).getName))
                  executeGrab(client, grabLocation, uploadLocation) match {
                    case Right(y) =>
                      if (y ==(Some(0), Some(0)))
                        downloadGrab(client, grabLocation) match {
                          case Right(_) => cleanup(client, grabLocation, uploadLocation) match {
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
    } match {
      case Right(x) => x
      case Left(x) => throw new RuntimeException(x)
    }
  }

  /**
   * Upload the appropriate grab file directly to the host
   *
   * @param client the ssh client to use
   * @param grab the grab file to upload
   * @return a ls of the upload directory to confirm the upload
   */
  def uploadGrab(client: SshClient, grab: String): Either[String, String] = {
    try client.upload(grab, installDir + "/").right.flatMap { _ =>
      client.exec("ls " + installDir).right.map { result =>
        result.stdOutAsString()
      }
    }
  }

  /**
   * Runt he grab on the host
   *
   * @param client ssh client to use
   * @param grabLocation the location of the extracted grab output on the host
   * @param uploadLocation - the uploaded grab location
   * @return
   */
  def executeGrab(client: SshClient, grabLocation: String, uploadLocation: String): Either[String, (Option[Int], Option[Int])] = {
    client.exec("rm -rf " + installDir + "/emcgrab").right.flatMap { rmOldGrabRes =>
      client.exec("cd " + installDir + " && tar -xvf " + uploadLocation + " && cd emcgrab && chmod +x ./emcgrab.sh && ./emcgrab.sh -lite -autoexec -legal -OUTDir " + grabLocation).right.map { runGrabRes =>
        (rmOldGrabRes.exitCode, runGrabRes.exitCode)
      }
    }
  }

  /**
   * Once the grab has been run, download the grab file from the host
   *
   * @param client the ssh client to use
   * @param grabLocation the expected location of the grab output on the host
   * @return success or an error message
   */
  def downloadGrab(client: SshClient, grabLocation: String): Either[String, _] = {
    client.download(grabLocation + "/", outputDir).right.map { _ =>
    }
  }

  /**
   * Remove all created files form the host
   *
   * @param client the ssh client to use
   * @param grabLocation the location of the grab on the host
   * @param uploadLocation the location of the uploaded grab utilities on the host
   * @return status of each delete operation
   */
  def cleanup(client: SshClient, grabLocation: String, uploadLocation: String): Either[String, (Option[Int], Option[Int], Option[Int])] = {
    client.exec("rm -rf " + installDir + "/emcgrab").right.flatMap { rmGrabRes =>
      client.exec("rm -rf " + uploadLocation).right.flatMap { rmUploadRes =>
        client.exec("rm -rf " + grabLocation + "/").right.map { finalRes =>
          (rmGrabRes.exitCode, rmUploadRes.exitCode, finalRes.exitCode)
        }
      }
    }
  }

  /**
   * Figure out what OS we are connected to
   * @param client the ssh client to use
   * @return the OS string which is just the output of uname -s
   */
  def determineOsType(client: SshClient): Either[String, String] = {
    client.exec("uname -s").right.map { os =>
      os.stdOutAsString().filter(_ >= ' ')
    }
  }

  /**
   * Check the directory of nix grabs and map the available grabs against the supported operating systems
   * @param path the path to all the nix grabs
   * @return a map of supported OS and grab files
   */
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
