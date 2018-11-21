package com.dvbaluki
package concurrency.ch3

import java.io.{File, FileOutputStream, PrintWriter}
import java.util
import java.util.concurrent.{ForkJoinPool, LinkedBlockingQueue}
import java.util.concurrent.atomic._

import scala.collection.JavaConverters._
import com.typesafe.scalalogging.Logger
import org.slf4j._
import java.util.{Calendar, Date, EmptyStackException}

import scala.annotation.tailrec
import scala.concurrent._
import scala.collection._
import scala.io.Source
import scala.util.Random


package object ch2{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  def log(msg: String): Unit =
    println(s"${Thread.currentThread.getName}: $msg")

  def execute(body: =>Unit) = ExecutionContext.global.execute(
    new Runnable { def run() = body }
  )
}

import ch2._


object Part3 extends App{
  /*
  val f=new FileSystem(".")
  f.deleteFile("text.txt")
  Thread.sleep(1000)
*/

 Ex2.go

  object Ex1{
    class PiggyBackContext extends ExecutionContext{
      override def execute(runnable: Runnable): Unit = {
        try {
          runnable.run()
        } finally {
          println("!")
        }
      }
      override def reportFailure(cause: Throwable): Unit = println("!!")
    }

    def go={
      val a =new Ex1.PiggyBackContext
      println(Thread.currentThread.getName)
      a.execute(() => execute(println(s"${Thread.currentThread.getName}")))
      println(Thread.currentThread.getName)
      logger.info("Ex1 done!")
    }

  }

  object Ex2{
    class TrieberStack[T] extends Traversable[T] {
      override def foreach[U](f: T => U): Unit =linked.get.foreach(f)
      //Вначале пустой список, при большом кол-ве потоков очевидно работать будет медленно
      private val linked=new AtomicReference[immutable.List[T]]

      def show =linked.get.foreach(print(_)+" ")

      linked.set(Nil)
      //помещаем в список через CAS
      def push(x: T): Unit = {
        @tailrec def go:Unit={
          val thisList=linked.get
          if (linked.compareAndSet(thisList, x :: thisList)) Unit
          else go
        }
        go
      }
      //Выталкиваем из списка. Если длина списка ноль, то оставляем пустой список и выкидываем первый элемент
      // его не существует, исключение NoSuchElementException обрабатывается уже не в классе
      def pop(): T = {
        @tailrec def go:T={
          val thisList=linked.get
          if (linked.compareAndSet(thisList, thisList match {
            case a:List[T] if (a.length<=1) =>Nil
            case _ => thisList.tail
          })) thisList.head
          else go
        }
        go
      }
    }

    //Пробуем это безобразие
    def go={
      val st=new TrieberStack[Int]
        val ectx = ExecutionContext.global
      for ( i <- 1 to 2) { // n потоков
        ectx.execute(() => {
          for (i <- 1 to 4) { // по m операций
            try {
              Random.nextInt(2) match {
                case 1 => println(st.push(Random.nextInt(10))+" "+Thread.currentThread.getName) //добавляем элемент от 0 до 9
                case 0 => println(st.pop()+ " "+Thread.currentThread.getName) //извлекаем
              }
            } catch {
              case ex: NoSuchElementException => println("getting value from empty stack")
            }
          }
        })
      }
      Thread.sleep(500)
      st.show


    }
  }
}

