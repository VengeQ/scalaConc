package com.dvbaluki.concurrency.ch8.actors

import akka.actor.{Actor, Props}
import akka.event.Logging

class HelloActor(val hello:String) extends Actor{
  val log=Logging(context.system, this)
  override def receive: Receive = {
    case a if a==hello =>
      log.info("msg Hello was received!")
    case _ =>
      log.info("I can't process this message!")
      context.stop(self)
  }
}

object HelloActor{
  def props(hello:String) = Props(new HelloActor(hello))
  def propsAlt(hello:String)=Props(classOf[HelloActor], hello)
}