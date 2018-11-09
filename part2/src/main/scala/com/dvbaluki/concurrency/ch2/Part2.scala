package com.dvbaluki
package concurrency.ch2

import com.typesafe.scalalogging.Logger
import org.slf4j._
import java.util.{Calendar, Date}

import com.dvbaluki.concurrency.ch2.Part2.Ex3.SyncVar

import scala.annotation.tailrec
import scala.collection._

package object ch2{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  def log(msg: String): Unit =
    println(s"${Thread.currentThread.getName}: $msg")

  def thread(body: =>Unit): Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }
}

import ch2._

object Part2 extends App{

  Ex5.go

  //parallel calculation
  object Ex1{
    def parallel[A, B](a: =>A, b: =>B): (A, B) = {

      var t1res: Option[Any] = None
      var t2res: Option[Any] = None

      def newThread(body: => Any): Thread = {
        val t = new Thread {
          override def run(): Unit =
            t1res = Some(a)
        }
        t.start()
        t.join()
        t
      }
      val t1=newThread(a)
      val t2=newThread(b)

      (a,b)
    }

    def go=println(parallel((1 to 60).reduceLeft(_+_),(1 until 6).reduceRight(_-_)))
  }

  //calculation block with timeout
  object Ex2{

    private [this] val counter:Int=10

    def periodically(duration: Long)(b: =>Unit): Unit ={
      val t=new Thread{
        override def run(): Unit = {
          for (c <- 1 to counter){
            b
            // println(c)
            Thread.sleep(duration)}
        }
      }
      t.start()
    }

    var i=1

    def go=periodically(1000){
      i=i+1
      println(i)
    }
  }

  // Ex3-5 realize producer and consumer threads
  object Ex3{

    class SyncVar[T] extends{

      logger.info("Start logging")
      @volatile
      private [this] var value:Option[T]=None


      def get(): T = {
        value match {
          case Some(v) => {value = None ;v}
          case None => throw new Exception("Container is empty!")
        }
      }

      def put(x: T): Unit = {
        value match{
          case None => value=Some(x)
          case Some(v) => throw new Exception("Container is full!")
        }
      }

    }

    val sync=new SyncVar[Double]

    // @volatile
    private var cont=true
    // @volatile
    private var i=1
    private val finishValue=150
    def go={
      val t1=thread {
        val name=Thread.currentThread.getName()
        while (cont ){
          try{
            sync.put(i)
            logger.info(s"Thread $name put value ${i}")
            println(s"$i was putted")
            i=i+1

          } catch {
            case ex:Exception =>logger.warn(s"${ex.getMessage}. Consume the message and Try later!")
          }
        }
      }

      val t2=thread{
        val name=Thread.currentThread.getName()
        while (cont ){
          try{
            val result=sync.get
            logger.info(s"Thread $name get value $result")
            println(s"i get result $result")
            if (i>=finishValue) cont=false
          } catch {
            case ex:Exception =>logger.warn(s"${ex.getMessage}. Put new message and try later!")
          }
        }
      }

      t1.join()
      t2.join()

    }

  }

  object Ex4{

    class SyncVar[T] extends{

      logger.info("Start logging")
      @volatile
      private var value:Option[T]=None


      def get(): T = {
        value match {
          case Some(v) => {value = None; v}
          case None => throw new Exception("Container is empty!")
        }
      }

      def put(x: T): Unit = {
        value match{
          case None => value=Some(x)
          case Some(v) => throw new Exception("Container is full!")
        }
      }

      def isEmpty=value match{
        case Some(v) => false
        case None => true
      }

      def nonEmpty=value match{
        case Some(v) => true
        case None => false
      }


    }

    val sync=new SyncVar[Double]

    @volatile
    private var cont=true
    @volatile
    private var i=1
    private val finishValue=15
    def go={
      val t1=thread {
        val name=Thread.currentThread.getName()
        while (cont){
          if (sync.isEmpty){
            sync.put(i)
            logger.info(s"Thread $name put value ${i}")
            println(s"$i was putted")

            i=i+1
          }
        }
      }

      val t2=thread{
        val name=Thread.currentThread.getName()
        while (cont ){
          if (sync.nonEmpty){
            val result=sync.get
            logger.info(s"Thread $name get value $result")
            println(s"i get result $result")
            if (i>=finishValue)
              cont=false
          }
        }
      }

      t1.join()
      t2.join()

    }
  }

  object Ex5{
    class SyncVar[T] extends{

      logger.info("Start logging")

      private var value:Option[T]=None

     final def getWait(): T = {
        this.synchronized{
          while (value==None) this.wait()
          println(s"get Value")
          val result =value.get
          value=None
          this.notify()
          result
        }
      }

      def putWait(x: T): Unit = {
        this.synchronized{
          while (value!=None) this.wait()
          println(s"put Value")
          this.notify()
          value = Option(x)
        }
      }
    }
    private var i=1

    private val finishValue=5
    val sync=new SyncVar[Double]

    def go={
      val t1=thread {
        val name=Thread.currentThread.getName()
        while (i<=finishValue) {
          sync.putWait(i)
          i = i + 1

        }
      }


      val t2=thread{
        val name=Thread.currentThread.getName()
        while (i<finishValue) {
          val result = sync.getWait()
          println(s"i get result $result")
        }
      }

      t1.join()
      t2.join()
    }

  }
}

