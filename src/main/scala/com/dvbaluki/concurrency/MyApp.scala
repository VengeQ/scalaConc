package com.dvbaluki
package concurrency

import scala.annotation.tailrec

//Not necessary code

object MyApp extends App {
  def fact(n:Int):Option[Int]=n match {

    case n if n>=0 =>
    @tailrec def go(current:Int, result:Int):Int= current match {
        case n if n<=1 => result
        case n if n>1 => go(n-1,result*n)
      }
      Some(go(n,1))

    case _ => None
  }

  val Reg="(\\d+)\\s*+->\\s*(\\w+)".r

  val b="123->first," +
    "10->second"

  for (Reg(number, name) <-  Reg findAllIn b)
    println(name+" "+number)



  for (a <-  Reg findAllIn b)
    println(a)

}
