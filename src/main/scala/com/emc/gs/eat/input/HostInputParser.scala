package com.emc.gs.eat.input

import java.io.File

import com.emc.gs.eat.host.Host
import com.github.tototoshi.csv._

/**
 * Parses csv input.
 */
object HostInputParser {

  def parseInputToHosts(files: Seq[File]): Seq[Host] = {
    parseCsvFiles(files).map(hostInfo => parseHost(hostInfo))
  }

  /**
   * Parses a series of host input files
   *
   * @param files series of host input files
   * @return list of the combined rows of all csv files
   */
  protected def parseCsvFiles(files: Seq[File]): Seq[List[String]] = {
    (for (file <- files) yield parseCsvFile(file)).flatten
  }

  /**
   * Parses a single host input file
   *
   * @param file host input file
   * @return list of the rows of the csv file
   */
  protected def parseCsvFile(file: File): Seq[List[String]] = {
    val reader = CSVReader.open(file)
    try {
      reader.all()
    } finally {
      reader.close()
    }
  }

  /**
   * Convert a line of CSV to a host
   *
   * @param hostInfo the host values contained in a line of CSV
   * @return a host populated with values form CSV
   */
  protected def parseHost(hostInfo: List[String]): Host = {
    Host(hostInfo.head, hostInfo(1), hostInfo(2), hostInfo(3))
  }


}
