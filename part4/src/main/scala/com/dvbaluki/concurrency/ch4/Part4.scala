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
<<<<<<< HEAD

import org.apache.commons.io.FileUtils._

import scala.collection.convert.decorateAsScala._
import scala.util.{Failure, Success}
=======
import org.apache.commons.io.FileUtils._
import scala.collection.convert.decorateAsScala._
>>>>>>> ce2fff156baf29d55fbe7a7e8e2c02541c2eea1d

package object ch4{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  def log(msg: String): Unit =
    println(s"${Thread.currentThread.getName}: $msg")

  def execute(body: =>Unit) = ExecutionContext.global.execute(
    new Runnable { def run() = body }
  )
}

<<<<<<< HEAD

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
=======
import ch4._



object Part4 extends App {

  val netiquetteUrl = "http://www.ietf.org/rfc/rfc1855.txt"
  val netiquette = Future { Source.fromURL(netiquetteUrl).mkString }
  val urlSpecUrl = "http://www.w3.org/Addressing/URL/url-spec.txt"
  val urlSpec = Future { Source.fromURL(urlSpecUrl).mkString }

  val answer = for {
    nettext <- netiquette
    urltext <- urlSpec
  } yield {
    "First, read this: " + nettext + ". Now, try this: " + urltext
  }


 go()

  @tailrec def go():Unit={
    Thread.sleep(100)
    answer.isCompleted match {
      case true => println(answer)
      case false => go()
    }

  }
>>>>>>> ce2fff156baf29d55fbe7a7e8e2c02541c2eea1d
}

