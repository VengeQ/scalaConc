package com.dvbaluki
package concurrency.ch4



import scala.collection.JavaConverters._
import com.typesafe.scalalogging.Logger
import org.slf4j._

import scala.annotation.tailrec
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.io.Source
import scala.async._
import java.io._
import java.util.{Timer, TimerTask}

import scala.util.{Failure, Random, Success, Try}
import sys.process._


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
        self map( p(_) ) recover {case ex:Exception =>false}
      }
    }

    def go={
      val a:Future[String]=Future(3/0 toString)
      val b =a.exists(_.contains("12"))
      val c=a.exists(_.contains("23"))
      while (!b.isCompleted || !a.isCompleted){Thread.sleep(30)}
      println(b.value.get.get)
      println(c.value.get.get)
    }
  }

  object Ex4{
    implicit class FutureExists[T](val self:Future[T]){
      def exists(p: T => Boolean): Future[Boolean]={
        val promise=Promise[Boolean]
        global.execute(() => try {
            self.onComplete{
              case Success(x) => promise success(p(x))
              case Failure(_) => promise success(false)
            }
        }catch {
          case ex:Exception =>println(s"${ex.getMessage}")
        })
        promise.future
      }
    }

    def go={
      val a:Future[String]=Future("123")
      val b =a.exists(_.contains("12"))
      val c=a.exists(_.contains("231"))
      while (!b.isCompleted || !a.isCompleted){Thread.sleep(30)}
      println(b.value.get.get)
      println(c.value.get.get)
    }
  }

  object Ex5{
    implicit class FutureExists[T](val self:Future[T]){
      def exists(p: T => Boolean): Future[Boolean]=Async.async{
        Async.await(self)
        self.value.get match {
          case Success(a) => p(a)
          case Failure(e) => false
        }
      }
    }
    def go={
      val a:Future[String]=Future(0/1 toString)
      val b =a.exists(_.contains("12"))
      val c=a.exists(_.contains("231"))
      while (!b.isCompleted || !a.isCompleted){Thread.sleep(30)}
      println(b.value.get.get)
      println(c.value.get.get)
    }
  }

  object Ex6{

    def spawn(command: String): Future[Int]={
      val p=Promise[Int]
      global.execute(new Runnable {
        override def run=try{
          p success (command!)
        }catch {
          case ex:Exception=>println(ex)
        }
      })
      p.future
    }


    def go = {
      for ( i <- 0 to 10){
        spawn( "cmd /c dir |findstr \""+i+"\"") onComplete{
          case Success(x) => println(x)
          case Failure(e) => println(e.getMessage)
        }
      }
    Thread.sleep(1000)
    }
  }

  object Ex7{
    class IMap[K, V] {
      private val inner=scala.collection.concurrent.TrieMap[K,V]()
      def update(k: K, v: V): Unit= {
        //двойная проверка с блокировкой при вставке, можно через атомик референц еще попробовать
        inner.get(k) match {
          case Some(_) => throw new Exception("Key already init exception")
          case None => inner.synchronized{ inner.get(k) match{
            case Some(_) => throw new Exception("Key already init exception")
            case None => inner.+=((k,v))
           }
          }
        }
      }
      def apply(k: K): Future[V]= {
        val promise=Promise[V]
        @tailrec def tryAgainGetValue:Any= {
          inner.get(k) match {
            case Some(x) => promise.success(x)
            case None => tryAgainGetValue
          }
        }
        global.execute(() => {tryAgainGetValue})
        promise.future
      }

      def show=inner.foreach(println)
    }

    def go={
      val a=new IMap[Int,String]

      for (i <-1 to 5){
        global.execute { () =>
          for (j<- 1 to 5) {
            try {
              a.update(i, Random.nextInt(10000) toString)
            } catch {
              case ex: Exception => 1
            }
          }
        }
      }
      Thread.sleep(500)
      a.show
      for (i <-1 to 5) a(i).onComplete(println)
    }
  }

  object Ex8{
    implicit class PromiseCompose[T](val self:Promise[T]){
      def compose[S](f: S => T): Promise[S] = {
        val promise=Promise[S]
        global.execute(() => {
          val future=promise.future
          future onComplete {
            case x if !self.isCompleted => self.complete(x map(f(_)))
            case x if self.isCompleted => 1
          }
        })
        promise
      }
    }
    def go= {
      val a = Promise[Int]
      val b = Promise[String]
      val c = a.compose((x: Int) => x * 2)
      val d = b.compose((x: String) => x * 2)
      d success ("hello")
      Thread.sleep(100)
      println(b)
    }
  }

  Ex8.go

}
