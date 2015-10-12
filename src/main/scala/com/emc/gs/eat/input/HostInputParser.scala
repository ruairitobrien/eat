package com.emc.gs.eat.input

import java.io.File

import com.github.tototoshi.csv._

/**
 * Parses csv input.
 */
object HostInputParser {

  /**
   * Parses a series of host input files
   *
   * @param files series of host input files
   * @return list of the combined rows of all csv files
   */
  def parseCsvFiles(files: Seq[File]): Seq[List[String]] = {
    (for (file <- files) yield HostInputParser.parseCsvFile(file)).flatten
  }

  /**
   * Parses a single host input file
   *
   * @param file host input file
   * @return list of the rows of the csv file
   */
  def parseCsvFile(file: File): Seq[List[String]] = {
    val reader = CSVReader.open(file)
    try {
      reader.all()
    } finally {
      reader.close()
    }
  }


}
