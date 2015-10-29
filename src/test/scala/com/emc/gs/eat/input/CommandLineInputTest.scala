package com.emc.gs.eat.input

import java.io.File
import java.util.UUID

import com.emc.gs.eat.Config
import com.emc.gs.eat.util.FileUtil
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

/**
 * Test command line input parsing
 */
class CommandLineInputTest extends FlatSpec with Matchers with BeforeAndAfter {

  val in = "src/test/resources/one.csv"
  val tempDir = System.getProperty("java.io.tmpdir")
  var out: String = ""

  behavior of "A Command Line Input Parser"


  before {
    out = tempDir + File.separator + UUID.randomUUID()
  }

  after {
    val outputFile = new File(out)
    if (outputFile.exists) {
      outputFile.delete()
    }
  }

  it should "executes error branch when inputs are empty" in {
    val args = Array[String]()
    val parser = CommandLineInput.createCommandLineOptionParser()
    parser.parse(args, Config()) should be(None)
  }


  it should "create a good config when all inputs are valid" in {
    val args = Array[String]("-o", out, "-i", in)
    val parser = CommandLineInput.createCommandLineOptionParser()
    val config: Config = parser.parse(args, Config()).orNull

    config should not be null
    config.out should equal(new File(out))
    config.hostFiles should equal(Seq[File](new File(in)))
    config.esxiGrabLocation should equal(FileUtil.getJarLocation.getParent + File.separator + "ESXi-GRAB" + File.separator + "emcgrab.exe")
    config.wmiClientLocation should equal(FileUtil.getJarLocation.getParent + File.separator + "Windows" + File.separator + "WMITest.exe")
    config.nixGrabLocation should equal(FileUtil.getJarLocation.getParent + File.separator + "NixGrabs")
    config.workers should be(100)
    config.verbose should be(right = false)
    config.debug should be(right = false)

  }

  it should "set the nix directory" in {
    val nixDir = "test/nix/dir"
    val args = Array[String]("-i", in, "-n", nixDir)
    val parser = CommandLineInput.createCommandLineOptionParser()
    val config: Config = parser.parse(args, Config()).orNull

    config should not be null
    config.nixGrabLocation should equal(nixDir)
  }


}
