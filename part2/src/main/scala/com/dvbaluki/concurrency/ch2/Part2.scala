package com.dvbaluki
package concurrency.ch2


import java.util.{Calendar, Date}
import concurrency.ch2.DeadLock
import scala.collection._

package object ch2{
  def log(msg: String): Unit =
    println(s"${Thread.currentThread.getName}: $msg")

  def thread(body: =>Unit): Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }
}

import ch2._

object Part2 extends App{


}

//parallel calculat
object Ex1{
  def parallel[A, B](a: =>A, b: =>B): (A, B) = {

    var t1res: Option[Any] = None
    var t2res: Option[Any] = None

    def newThread(body: => Any): Thread = {
      val t = new Thread {
        override def run(): Unit =
          t1res = Some(a)
      }
      t.start()
      t.join()
      t
    }
    val t1=newThread(a)
    val t2=newThread(b)

    (a,b)
  }

  def go=println(parallel((1 to 60).reduceLeft(_+_),(1 until 6).reduceRight(_-_)))
}

object Ex2{

  private [this] val counter:Int=10

  def periodically(duration: Long)(b: =>Unit): Unit ={
    val t=new Thread{
      override def run(): Unit = {
        for (c <- 1 to counter){
          b
          println(c)
          Thread.sleep(duration)}
        }
    }
    t.start()
  }

  def go=periodically(1000){

  }
}