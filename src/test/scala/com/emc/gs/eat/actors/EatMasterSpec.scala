package com.emc.gs.eat.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class EatMasterSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("EatMasterSpec"))


  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "An EatMaster Actor" should "be able to process hosts" in {
    /*val hostFiles = Seq()
    val outputDir = new File(".")
    val listener = TestActorRef(Props[HostAnalysisListener])
    val eatMaster = TestActorRef(Props(new EatMaster(hostFiles, outputDir, listener)))

    eatMaster ! ProcessHosts
    val eatMasterInstance = eatMaster.underlyingActor.asInstanceOf[EatMaster]*/
  }


}
