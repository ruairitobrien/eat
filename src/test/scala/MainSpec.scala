import org.scalatest._

class MainSpec extends FlatSpec with Matchers {
  "Hello" should "have tests" in {
    true should be === true
  }
}
