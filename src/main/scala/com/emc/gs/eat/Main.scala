package com.emc.gs.eat

/**
 *
 */
object Main {

  val usage = "Usage: eat filename"


  def main(args: Array[String]): Unit = {
    if (args.length == 0) println(usage)
    val arglist = args.toList
    type OptionMap = Map[Symbol, Any]

    def nextOption(map: OptionMap, list: List[String]): OptionMap = {
      def isSwitch(s: String) = s(0) == '-'
      list match {
        case Nil => map
        case string :: opt :: tail if isSwitch(opt) =>
          nextOption(map ++ Map('infile -> string), list.tail)
        case string :: Nil => nextOption(map ++ Map('infile -> string), list.tail)
        case option :: tail => println("Unknown option " + option)
          sys.exit(1)
      }
    }
    val options = nextOption(Map(), arglist)
    if (options.nonEmpty) {
      println(options)

      /** val bufferedSource = io.Source.fromFile(options.get("infile").)

      bufferedSource.close() */
    }
  }

}
