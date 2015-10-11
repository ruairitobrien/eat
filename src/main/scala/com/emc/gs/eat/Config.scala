package com.emc.gs.eat

import java.io.File

/**
 * The configuration created when the app is run form the command line.
 *
 * This will contain any command line arguments.
 *
 * @param hostFiles - the csv files containing host information.
 * @param out - the output directory for the host data. Will be created if doesn't exist.
 * @param verbose - print out extra information while running.
 * @param debug - run in debug mode.
 */
case class Config(
                   hostFiles: Seq[File] = Seq(),
                   out: File = new File("."),
                   verbose: Boolean = false,
                   debug: Boolean = false
                   )
