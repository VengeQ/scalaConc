package com.dvbaluki.concurrency.ch2

/**
  * Created by Daniil.Balukin on 05.11.2018.
  */
import ch2.{log,thread}

object DeadLock {

  class Person(name: String) {
    def eat(a: Cutlery, b: Cutlery) = a.synchronized {
      println(s"${this.name} say: ${a.name} is Mine")
      b.synchronized {
        println(s"${this.name} say: ${b.name} is Mine")
      }
    }
  }

  //tow cutlery
  object Cutlery {
    val fork = new Cutlery("Fork")
    val knife = new Cutlery("Knife")
  }
  class Cutlery(val name: String)


  //two person wanna to eat!
  def go={
    val me = new Person("Daniil")
    val you = new Person("Anna")
    //val t1 = thread {for (i <- 0 until 10) me.eat(Cutlery.fork, Cutlery.knife)} //-incorrect order of resources. Call deadlock
    val t1 = thread {for (i <- 0 until 100) me.eat(Cutlery.knife, Cutlery.fork)} //correct order
    val t2 = thread {for (i <- 0 until 10) you.eat(Cutlery.knife, Cutlery.fork)}
    t1.join()

    t2.join()
  }
}

