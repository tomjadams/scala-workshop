package example

import com.redbubble.pricer.App
import org.scalatest._

class HelloSpec extends FlatSpec with Matchers {
  "The Hello object" should "say hello" in {
    App.greeting shouldEqual "hello"
  }
}
