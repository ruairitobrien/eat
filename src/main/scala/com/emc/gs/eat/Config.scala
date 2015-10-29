package com.emc.gs.eat

import java.io.File

import com.emc.gs.eat.util.FileUtil

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
                   esxiGrabLocation: String = FileUtil.getJarLocation.getParent + File.separator + "ESXi-GRAB" + File.separator + "emcgrab.exe",
                   wmiClientLocation: String = FileUtil.getJarLocation.getParent + File.separator + "Windows" + File.separator + "WMITest.exe",
                   nixGrabLocation: String = FileUtil.getJarLocation.getParent + File.separator + "NixGrabs",
                   workers: Int = 100,
                   verbose: Boolean = false,
                   debug: Boolean = false
                   )
