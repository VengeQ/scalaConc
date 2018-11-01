package com.dvbaluki
package concurrency.ch2
package object ch2{
  def log(msg: String): Unit =
    println(s"${Thread.currentThread.getName}: $msg")
}

import ch2._

object Part2 extends App{



  def thread(body: => Unit)={
    val t=new Thread{
      override def run()=body
    }
    t.start()
    t
  }



  var uidCount = 0L
  def getUniqueId() =this.synchronized {
    val freshUid = uidCount + 1
    uidCount = freshUid
    freshUid

  }

  def printUniqueIds(n: Int): Unit = {
    val uids = for (i<- 0 until n) yield getUniqueId()
    log(s"Generated uids: $uids")
  }
  val t = thread { printUniqueIds(5) }
  printUniqueIds(5)
  t.join()


}

