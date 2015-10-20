package com.emc.gs.eat.input

import java.io.File

import com.emc.gs.eat.host.Host
import org.scalatest.{FlatSpec, Matchers}

/**
 * Unit tests for Host Input Parser
 */
class HostInputParserTest extends FlatSpec with Matchers {

  val hosts = List(
    new Host("10.66.77.33", "windows", "john", "secret"),
    new Host("10.66.77.34", "unix", "harry", "supersecret"),
    new Host("10.66.77.35", "linux", "mary", "password")
  )


  behavior of "A Host Input Parser"


  it should "parse a valid csv file" in {
    val res: Seq[Host] = HostInputParser.parseInputToHosts(List(new File("src/test/resources/sample.csv")))
    res.length should be(3)
    hosts.foreach(host =>
      res.find(x => x.address == host.address).get should equal(host)
    )
  }

  it should "handle invalid hosts inputs gracefully" in {
    val res: Seq[Host] = HostInputParser.parseInputToHosts(List(new File("src/test/resources/badHosts.csv")))
    res.length should be(0)
  }

}
