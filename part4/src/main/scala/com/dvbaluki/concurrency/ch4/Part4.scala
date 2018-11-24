package com.dvbaluki
package concurrency.ch4



import scala.collection.JavaConverters._
import com.typesafe.scalalogging.Logger
import org.slf4j._

import scala.annotation.tailrec
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.io.Source
import java.io._

import org.apache.commons.io.FileUtils._

import scala.collection.convert.decorateAsScala._
import scala.util.{Failure, Success}

package object ch4{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  def log(msg: String): Unit =
    println(s"${Thread.currentThread.getName}: $msg")

  def execute(body: =>Unit) = ExecutionContext.global.execute(
    new Runnable { def run() = body }
  )
}


object Part4 extends App {

 val r1=Future{
   (1 to 100) filter(_%2==0) reduceLeft(_+_)
 }

  val r2=Future{
    (1 to 100) filter(_%2==1) reduceLeft(_+_)
  }

  val answer = for{
    x <- r1
    y <- r2

  } yield {println(x+" "+y );x+y}


  answer.onComplete( _ match{
    case Success(ext) => println(ext)
    case Failure(est) => println(est)
  })

  Thread.sleep(100)
}

