import org.scalatest.FunSuite
import com.dvbaluki.concurrency.ch2.Part2._
class Ex8isThisEmptyTest extends FunSuite {

  test("This method need no generate exception. Enqueue and dequeue function") {
    Ex8.enqueue(1, ()=>println(1))
    if (Ex8.isThisEmpty){
      val e=Ex8.dequeue
      println(e)
    }

  }


  test("Dequeue from empty Array of queues.Generate NullPointerException") {
      val a=Ex8.dequeue
    println(a)
  }
}