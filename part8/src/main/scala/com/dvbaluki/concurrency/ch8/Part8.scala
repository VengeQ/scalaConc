package com.dvbaluki
package concurrency.ch8


import com.typesafe.scalalogging.Logger
import org.slf4j._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.concurrent.blocking
import scala.util.Random
import java.util.concurrent.Executor

import akka.actor.{ActorSystem, Props}
import com.dvbaluki.concurrency.ch8.actors.{CountDownActor, DeafActor, DictionaryActor, HelloActor}




package object ch8{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  def log(msg: String): Unit =
    logger.info(msg)

  def thread(body: =>Unit): Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }

}

import ch8._

object Part8  extends App{

  lazy val system=ActorSystem("hello-akka")
  val actor=system.actorOf(Props[CountDownActor], name="deaf")

  val dict = system.actorOf(Props[DictionaryActor],"dict")
  dict ! DictionaryActor.IsWord("program")
  Thread.sleep(1000)
  dict ! DictionaryActor.Init("/words.txt")
  Thread.sleep(1000)
  dict ! DictionaryActor.IsWord("dictionary")
  Thread.sleep(1000)
  dict ! DictionaryActor.IsWord("balabas")
  Thread.sleep(1000)
  dict ! DictionaryActor.End
  Thread.sleep(1000)
  system.terminate()
}
