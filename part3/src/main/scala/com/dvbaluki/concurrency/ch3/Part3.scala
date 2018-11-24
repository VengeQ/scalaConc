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
import scala.collection.{concurrent, _}
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

  object Ex3{
    class ConcurrentSortedList[T](implicit val ord: Ordering[T]) {

      private var list =new java.util.LinkedList[AtomicReference[T]] // связный список атомарных ссылок

      def add(x: T): Unit ={

      }

      def iterator: Iterator[T] = ???
    }

    val l=new ConcurrentSortedList[Int]()
  }

  object Ex5 {
    class PureLazyCell[T](initialization: => T) {
      @volatile var isInit=false
      def apply(): T = this.synchronized{initialization}
    }

  }

  object Ex6{
    class LazyCell[T](initialization: => T) {
      @volatile var isInit=false
      var init=initialization
      def apply(): T = if (!isInit){
        init
      } else init
    }

    private def x2(a: Int) = a + a

    def go = {
      val a = new LazyCell[Int](x2(3))
    }
  }

  //after
  object Ex7{
    class SyncConcurrentMap[A,B] extends scala.collection.concurrent.Map[A,B]{

      @volatile var myMap:Seq[(A,B)]=Seq()
      override def putIfAbsent(k: A, v: B): Option[B] = this.synchronized{
        myMap find((x:(A,B))=>x._1==k) match{
          case None => None
          case Some(a) =>{
            myMap=myMap.dropWhile(_==(k))
            myMap=myMap.+:((a._1,v))
            Option(v)
          }
        }
      }

      override def remove(k: A, v: B): Boolean = this.synchronized{
        this.get(k) match{
          case Some(x) if x==v =>{this.remove(k);true}
          case _ => false
        }
      }

      override def replace(k: A, oldvalue: B, newvalue: B): Boolean = this.synchronized{
        this.get(k) match {
          case Some(x) if x==oldvalue =>{this.put(k,newvalue);true}
          case _ => false
        }
      }

      override def replace(k: A, v: B): Option[B] = {
        this.get(k) match{
          case Some(x) => {this.put(k,v); Option(v)}
          case _ => None
        }
        this.put(k,v)

      }

      override def +=(kv: (A, B)): SyncConcurrentMap.this.type = this.synchronized{
        this.put(kv._1,kv._2)
        this
      }

      override def -=(key: A): SyncConcurrentMap.this.type = this.synchronized{

        this
      }

      override def get(key: A): Option[B] = this.synchronized{
        this.find(_._1==key) match{
          case Some((a,b)) => Some(b)
          case None => None
        }


      }


      override def iterator: Iterator[(A, B)] = this.synchronized{
        new Iterator[(A, B)] {
          private val keys=this.toList
          private var index=0
          private val lengthOf=keys.length
          override def hasNext: Boolean = if (index<lengthOf-1) true else false

          override def next(): (A, B) = {
            index=index+1
            keys(index-1)
          }
        }
      }
    }

    //Пробуем это безобразие
    def go={
      var st=new SyncConcurrentMap[Int,String]
      val ectx = ExecutionContext.global
      for ( i <- 1 to 2) { // n потоков
        ectx.execute(() => {
          for (i <- 1 to 4) { // по m операций
            try {
              Random.nextInt(5) match {
                case 0 => {val a=Random.nextInt(10);st += ((a,a.toString))}
                case 1 => st get Random.nextInt(10)
                case 2 => st -= Random.nextInt(10)
                case 3 => st replace (Random.nextInt(10),Random.nextInt(10).toString,Random.nextInt(10).toString)
                case 4 => st remove  (Random.nextInt(10),Random.nextInt(10).toString)
              }
            } catch {
              case ex: Exception => println(s"${ex.getLocalizedMessage}")
            }
          }
        })
      }
      Thread.sleep(500)
      st.foreach(println)
    }



  }
}

