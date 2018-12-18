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
import java.util.{Timer, TimerTask}

import org.apache.commons.io.FileUtils._

import scala.collection.convert.decorateAsScala._
import scala.util.{Failure, Success, Try}
import org.apache.commons.io.FileUtils._

import scala.{None, util}
import scala.concurrent.duration._
import scala.collection.convert.decorateAsScala._
import scala.util.control.NonFatal


package object ch4{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  def log(msg: String): Unit =
    println(s"${Thread.currentThread.getName}: $msg")

  def execute(body: =>Unit) = ExecutionContext.global.execute(
    new Runnable { def run() = body }
  )
  private val timer = new Timer(true)
  def timeout(t: Long): Future[Unit] = {
    val p = Promise[Unit]
    timer.schedule(new TimerTask {
      def run() = {
        p success ()
        timer.cancel()
      }
    }, t)
    p.future
  }
  implicit class FutureOps[T](val self: Future[T]) {
    def or(that: Future[T]): Future[T] = {
      val p = Promise[T]
      self onComplete { case x => p tryComplete x }
      that onComplete { case y => p tryComplete y }
      p.future
    }
  }

}


object Part4 extends App {
  import ch4._


  object Ex1{
    @tailrec
    def readAndValidateUrl:String={
      println("""type url in format "http://www.example.com".""")
      val read:String=scala.io.StdIn.readLine()
      val pattern="""^http(s*)://(www\.)?\w+.\w+""".r
      pattern findFirstIn read match{
        case Some(x)=>read
        case None =>readAndValidateUrl
      }
    }

    def getFromUrl(string: String):Future[String]={
      val p=Promise[String]
      global.execute(() => try {
        p success (Source.fromURL(string)("UTF-8").mkString)
      } catch {
        case ex: Exception => println(ex)
      })
      p.future
    }


    @tailrec
    def checkCompleteUrl[T](future:Future[T]):Unit={
      if (future.isCompleted) {
       println(s"\n${future.value.get.get}")
      }
      else {
        print(".")
        Thread.sleep(50)
        checkCompleteUrl(future)
      }
    }

    def go={
      val urlString=readAndValidateUrl
      val htmlFromUrl=getFromUrl(urlString) or timeout(5000).map(_=>("timeout!"))
      checkCompleteUrl(htmlFromUrl)
    }
  }

  object Ex2{
    class IVar[T]() {
      private val p = Promise[T]
      def apply(): T = {
        if (p.isCompleted) p.future.value.get.get
        else throw new Exception("not initialize IVar")
      }

      def :=(x: T): Unit = {
        if (p.future.isCompleted) throw new Exception("Ivar already init")
        else {
          p.success(x)
          p.future

        }
        Unit
      }
    }

    def go={
      val a=new IVar[Int]
      a:=23
      println(a())

    }
  }

  object Ex3{
    implicit class FutureExists[T](val self:Future[T]){
      def exists(p: T => Boolean): Future[Boolean]={
        self map( p(_) )
      }
    }

    def go={
      val a:Future[String]=Future("124")
      val b =a.exists(_.contains("12"))
      val c=a.exists(_.contains("23"))
      while (!b.isCompleted || !a.isCompleted){Thread.sleep(30)}
      println(b.value.get.get)
      println(c.value.get.get)
    }
  }

  Ex1.go


}