package com.emc.gs.eat.input

import java.io.File

import com.github.tototoshi.csv._

/**
 * Parses csv input.
 */
object InputParser {

  def parseCsvFiles(files: Seq[File]): Seq[List[String]] = {
    (for (file <- files) yield InputParser.parseCsvFile(file)).flatten
  }

  def parseCsvFile(file: File): Seq[List[String]] = {
    val reader = CSVReader.open(file)
    try {
      reader.all()
    } finally {
      reader.close()
    }
  }


}
