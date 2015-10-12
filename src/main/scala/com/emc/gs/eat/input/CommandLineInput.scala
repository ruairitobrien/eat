package com.emc.gs.eat.input

import java.io.File

import com.emc.gs.eat.Config
import scopt.OptionParser

/**
 * Helper factory to create a parser to parse and validate command line input.
 */
object CommandLineInput {

  /**
   * Created an instance of a command line option parse to process options passed to the application.
   *
   * @return the command line option parser
   */
  def createCommandLineOptionParser(): OptionParser[Config] = {
    new scopt.OptionParser[Config]("eat") {
      head("eat", "1.x")
      opt[File]('o', "out") required() valueName "<file>" action { (x, c) =>
        c.copy(out = x)
      } validate {
        x => validateOutputDir(x)
      } text "An output directory where grabs and other output files will be placed. If the directory doesn't exist it will be created."
      opt[Seq[File]]('i', "in") required() valueName "<file1.csv>,<file2.csv>..." action { (x, c) =>
        c.copy(hostFiles = x)
      } validate {
        x => validateInputFiles(x)
      } text "Required csv host data to process. Can be provided in one or more files."
      opt[Unit]("verbose") action { (_, c) =>
        c.copy(verbose = true)
      } text "print verbose output"
      opt[Unit]("debug") hidden() action { (_, c) =>
        c.copy(debug = true)
      } text "start application in debug mode"
      help("help") text "display available commands and information"

      /**
       * Checks that all input files exist.
       *
       * @param inputFiles the host input files
       * @return success if all files exists or failure if any do not
       */
      def validateInputFiles(inputFiles: Seq[File]): Either[String, Unit] = {
        val nonExistentFiles = inputFiles.filter(y => !y.exists)
        if (nonExistentFiles.isEmpty) success
        else
          failure("Could not find %s. Please check the file path and try again".format(nonExistentFiles.mkString(", ")))
      }

      /**
       * If the output directory doesn't exist it will be created.
       * If the output directory exists and is empty all is good.
       * If the output directory exists and is not empty, ask the user if that's OK.
       * if the output directory is a file return an error.
       *
       * @param outDir the output directory provided as an option
       * @return sucess or failure based on output directory validation criteria
       */
      def validateOutputDir(outDir: File): Either[String, Unit] = {
        if (!outDir.exists())
          if (outDir.mkdir()) success else failure("Could not create directory %s".format(outDir))
        else if (!outDir.isDirectory) failure("%s is not a valid directory".format(outDir))
        else {
          if (outDir.list().isEmpty) success
          else {
            val proceed = readLine("The output directory specified is not empty. Proceed anyway?(N)")
            if (proceed.toLowerCase == "y" || proceed.toLowerCase == "yes") {
              success
            } else {
              failure("Operation cancelled")
            }
          }
        }
      }

    }
  }

}
