package com.emc.gs.eat.util

import java.io.File
import java.nio.file.{Path, Paths}

import com.emc.gs.eat.Main

object FileUtil {

  /**
   * Get the location of the actual JAR file at runtime and create a File object with the path.
   * This is to help with relative file access without being affected by where the jar is executed from.
   *
   * @return a File object referencing the JAR containing this class.
   */
  def getJarLocation: File = {
    new File(Main.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath)
  }

  /**
   * Creates a temporary directory where the final path node is the given name passed to the function
   *
   * @param tmpName the name of the temporary directory
   * @return a FIle object referencing a temporary directory
   */
  def createTempDir(tmpName: String): File = {
    val tmpDir = Paths.get(System.getProperty("java.io.tmpdir"))
    val name: Path = tmpDir.getFileSystem.getPath(tmpName)
    if (name.getParent != null) throw new IllegalArgumentException("Invalid prefix or suffix")
    new File(tmpDir.resolve(name).toString)
  }

}
