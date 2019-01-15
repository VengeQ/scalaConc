package com.dvbaluki.concurrency.ch8.actors

import akka.actor.{Actor, Props}
import akka.event.Logging

class DeafActor extends Actor{
  val log=Logging(context.system, this)
  override def receive: Receive =PartialFunction.empty

  override def unhandled(message: Any) = message match{
    case msg:String => log.info(s"I dunno about this!")
    case msg => super.unhandled(msg)
  }

}

object DeafActor{
  def props= Props(new DeafActor())
  def propsAlt=Props(classOf[DeafActor])
}