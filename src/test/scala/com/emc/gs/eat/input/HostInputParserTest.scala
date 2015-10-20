package com.emc.gs.eat.input

import java.io.File

import com.emc.gs.eat.host.Host
import org.scalatest.Matchers
import org.scalatest.fixture

/**
 * Unit tests for Host Input Parser
 */
class HostInputParserTest extends fixture.FlatSpec with Matchers {

  type FixtureParam = List[Host]


  def withFixture(test: OneArgTest) {
    val hosts = List(
      new Host("10.66.77.33", "windows", "john", "secret"),
      new Host("10.66.77.34", "unix", "harry", "supersecret"),
      new Host("10.66.77.35", "linux", "mary", "password")
    )
    test(hosts)
  }

  behavior of "A Host Input Parser"


  it should "parse a valid csv file" in { hosts =>
    val res: Seq[Host] = HostInputParser.parseInputToHosts(List(new File("src/test/resources/sample.csv")))
    res.length should be(3)
    hosts.foreach(host =>
      res.find(x => x.address == host.address) should be(host)
    )

  }

}
