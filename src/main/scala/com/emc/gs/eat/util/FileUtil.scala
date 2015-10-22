package com.emc.gs.eat.util

import java.io.File
import java.nio.file.{Path, Paths}

import com.emc.gs.eat.Main

object FileUtil {

  def getJarLocation: File = {
    new File(Main.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath)
  }

  def createTempDir(tmpName: String): File = {
    val tmpDir = Paths.get(System.getProperty("java.io.tmpdir"))
    val name: Path = tmpDir.getFileSystem.getPath(tmpName)
    if (name.getParent != null) throw new IllegalArgumentException("Invalid prefix or suffix")
    new File(tmpDir.resolve(name).toString)
  }

}
