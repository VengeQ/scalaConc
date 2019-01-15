package com.dvbaluki.concurrency.ch8.actors

import akka.actor.Actor
import akka.event.Logging

import scala.collection.mutable
import scala.io.Source

class DictionaryActor extends Actor{
  private val log = Logging(context.system, this)
  private val dictionary = mutable.Set[String]()
  def receive = uninitialized

  def uninitialized: PartialFunction[Any, Unit] = {
    case DictionaryActor.Init(path) =>
      val stream = getClass.getResourceAsStream(path)
      val words = Source.fromInputStream(stream)
      for (w <- words.getLines) dictionary += w
      context.become(initialized)
  }

  def initialized: PartialFunction[Any, Unit] = {
    case DictionaryActor.IsWord(w) =>
      log.info(s"word $w exists: ${dictionary(w)}")
    case DictionaryActor.End =>
      dictionary.clear()
      context.become(uninitialized)
  }
  override def unhandled(msg: Any) = {
    log.info(s"cannot handle message $msg in this state.")
  }
}


object DictionaryActor{
  case class Init(path:String)
  case class IsWord(w: String)
  case object End
}
