package com.dvbaluki
package concurrency.ch2

import java.io.{File, FileOutputStream, PrintWriter}

import com.typesafe.scalalogging.Logger
import org.slf4j._
import java.util.{Calendar, Date}

import com.dvbaluki.concurrency.ch2.Part2.Ex3.SyncVar

import scala.annotation.tailrec
import scala.collection._
import scala.io.Source
import scala.reflect.io.Path
import scala.util.Random


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


  //private val out = new PrintWriter(file)

  Ex8.go

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

  object Ex6{
    class SyncQueue[A](n:Int){
      private val queue=new mutable.Queue[A]

      final def getWait[B >: A](): B = {
        this.synchronized{
          while (queue.isEmpty) this.wait()
          lines.println(s"current queue before Get: ${sync.queue}")
          val result = queue.dequeue()
          lines.println(s"current queue after Get: ${sync.queue}")
          this.notify()
          result
        }
      }

      def putWait(x: A): Unit = {
        this.synchronized {
          while (queue.length == n) this.wait()
          lines.println(s"current queue before Put: ${sync.queue}")
          val result=queue.enqueue(x)
          lines.println(s"current queue after Put: ${sync.queue}")
          this.notify()
          result
        }
      }

      def isEmpty=queue.isEmpty
    }
    private var i=1
    private val finishValue=12
    private val sync=new SyncQueue[Int](5)
    val file = new File("res").toPath
    val lines = new PrintWriter(new File("res").getPath)


    def go={

      val t1=thread {
        while (i<=finishValue) {
          sync.putWait(i)
          i=i+1

        }
      }

      val t2=thread{

        while (i<=finishValue) sync.getWait()
        while (!sync.isEmpty) sync.getWait()
        println(sync.isEmpty)
        if (i>=finishValue && sync.isEmpty) lines.close()

      }

      t1.join()
      t2.join()
    }

  }
  //incorrect working
  object Ex7{
      private val transfers = mutable.ArrayBuffer[String]()

      def logTransfer(name: String, n: Int) = transfers.synchronized {
        transfers += s"transfer to account ‘$name’ = $n"
      }
        private var uidCount = 0L

        def getUniqueId() = this.synchronized {
          val freshUid = uidCount + 1
          uidCount = freshUid
          freshUid
        }

        class Account(val name: String,@volatile var money: Int) {

          val uid = getUniqueId()
        }
    object Account{
      def send(a1: Account, a2: Account, n: Int) {
        def adjust() {
          if (a1.money>=n) {
            a1.money -= n
            a2.money += n
          }
        }

        if (a1.uid < a2.uid)
          a1.synchronized {
            a2.synchronized {
              adjust()
            }
          }
        else a2.synchronized {
          a1.synchronized {
            adjust()
          }
        }
      }
      def add(account: Account, n: Int) = account.synchronized {
        account.money += n
        if (n > 10) logTransfer(account.name, n)
      }

      def sendAll(accounts: Set[Account], target: Account): Unit = {

        @tailrec
        def go(accounts: Set[Account], target: Account): Unit = accounts match {
          case a:Set[Account] if a.isEmpty => Unit
          case a:Set[Account] if a.tail == Nil => send(a.head,target ,a.head.money)
          case a:Set[Account] => {send(a.head,target ,a.head.money);go(accounts.tail,target)}
        }

          go(accounts,target)

      }
    }

    def go={
      val accs=Array(new Account("Acc1",1000), new Account("Acc2",1320), new Account("Acc3",500),
        new Account("Acc4",710), new Account("Acc5",120), new Account("Acc6",124))

      for (i <- 1 to 2){
        val t=thread {
          val from = newR
          val to = Random.nextInt(5) + 1
          println(s"from Accs values\n first: ${accs(from._1).name} with ${accs(from._1).money}\n" +
            s"second: ${accs(from._2).name} with ${accs(from._2).money}\n" +
            s"third: ${accs(from._3).name} with ${accs(from._3).money}\n")
          println(s"to Accs values: ${accs(to).name} with ${accs(to).money}\n--------------------\n")
          Account.sendAll(Set(accs(from._1), accs(from._2), accs(from._3)), accs(to))
        }
      }

      Thread.sleep(2000)
      accs.foreach((x:Account)=>println(x.money))

      def newR={
        val t1=Random.nextInt(5)+1
        var temp=Random.nextInt(5)+1


        while (temp==t1) temp=Random.nextInt(5)+1

        var temp2=Random.nextInt(5)+1
        while (temp2==t1 || temp2==temp) temp2=Random.nextInt(5)+1
        (t1,temp,temp2)

      }
    }
  }


  //need fix nullPointer in dequeue and enqueue
  object Ex8 {

    private val tasks = mutable.Queue[() => Unit]()

    private val syncs:Array[mutable.Queue[() => Unit]] =new Array(10)

    private def isThisEmpty:Boolean={
      @tailrec
      def go(array:Array[mutable.Queue[() => Unit]]):Boolean=array match {
        case a:Array[mutable.Queue[() => Unit]] if a.length==0 => true
        case a:Array[mutable.Queue[() => Unit]] if a.length==1 => {println(a.length);if (a(0).isEmpty) true else false}
        case a:Array[mutable.Queue[() => Unit]] => {println(a(0));if (a.head==null || !(a.head.length==0)){ print(a.tail.length);false} else {go(a.tail)}}
      }
      go(syncs)
    }

    private def dequeue={
      @tailrec
      def go(array:Array[mutable.Queue[() => Unit]]):() => Unit=array match{
        case a:Array[mutable.Queue[() => Unit]]  if (a.length==1) => a.head.dequeue()
        case a:Array[mutable.Queue[() => Unit]] => if (a.head==null || !a.head.isEmpty) a.head.dequeue() else go(a.tail)
      }
      go(syncs)
    }
    private def enqueue(priority:Int, sync:() => Unit)={
      println("eneq")
      val a =(syncs(priority)).enqueue(sync)
    }

    object Worker extends Thread {
      setDaemon(true)

      def poll() = syncs.synchronized {
        while (isThisEmpty) syncs.wait()
        dequeue
      }


      override def run() = while (true) {
        val task = poll()
        task()
      }
    }

    def asynchronous(priority: Int)(task: =>Unit): Unit={
      require(priority<=9)
      syncs.synchronized{
        println("sync")
        enqueue(priority,() => task)
        syncs.notify()
      }
    }

    def go:Unit={
      Worker.start()
      asynchronous (1){ log("Hello ") }
      asynchronous (1){ log("Fucking ") }
      asynchronous (1){ log("World!") }
      Thread.sleep(500)
    }

  }
}

