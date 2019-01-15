package com.dvbaluki.concurrency.ch8.actors

import akka.actor.Actor
import akka.event.Logging

class CountDownActor extends Actor{
  val log=Logging(context.system, this)
  var n = 10

  def counting:Actor.Receive={
    case "count" =>
      n-=1
      log.info(s"n=$n")
      n match {
        case 0 => context.become(done)
        case a if a%2==0 => context.become(odd)
        case a if a%2!=0 => context.become(even)
      }

  }
  def done:Receive=PartialFunction.empty
  def odd ={
    log.info(s"$n is odd")
    counting
  }
  def even ={
    log.info(s"$n is even")
    counting
  }
  override def receive: Receive = counting

}
