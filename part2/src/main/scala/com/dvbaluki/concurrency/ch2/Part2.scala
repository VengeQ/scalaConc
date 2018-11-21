package com.dvbaluki
package concurrency.ch2

import java.io.{File, FileOutputStream, PrintWriter}
import java.lang.module.ModuleDescriptor.Requires

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


  Ex10.go
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
      val t1=thread{
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

  object Ex8 {

    private val syncs:Array[mutable.Queue[() => Unit]]= (for (i <- 1 to 10) yield new mutable.Queue[() => Unit]).toArray// Массив, в котором находятся очереди Queue, индекс массива - приоритет

    // Проверяем, что все очереди пустые начиная с 1
    private def isThisEmpty:Boolean={
      @tailrec
      def go(s:Array[mutable.Queue[() => Unit]]):Boolean=s match{
        case x if x.length==1 => s(0).isEmpty
        case x  => if (!(s(0).isEmpty)) false else go(x.tail)
      }
      go(syncs)
    }

     //Если есть непустые очереди, то вытаскиваем самые приоритетный элемент, всегда надо проверять через  isThisEmpty
     private def dequeue:() => Unit={
      @tailrec
      def go(s:Array[mutable.Queue[() => Unit]]):() => Unit = s match{
        case s if s.length==1 => s(0).dequeue()
        case s => if (s(0).isEmpty) go(s.tail) else s(0).dequeue()
      }
      go(syncs)
    }

    //Поместить элемент в очередь
     private def enqueue(priority:Int, sync:() => Unit)=
      syncs(priority).enqueue(sync)

    //Поток демон
    object Worker extends Thread {
      setDaemon(true)
      // функция мониторинга очередей
      def poll() = syncs.synchronized {
        while (isThisEmpty) syncs.wait()
          dequeue
      }
      override def run() = while (true) {
        val task = poll()
        task()
      }
    }

    //метод асинхронного добавления в очередь
    def asynchronous(priority: Int)(task: =>Unit): Unit={
      require(priority<=9)
      syncs.synchronized{
        enqueue(priority,() => task)
        syncs.notify()
      }
    }

    //GO GO GO
    def go:Unit={
      Worker.start()
      asynchronous (2){ log("Hello ") }
      asynchronous (2){ log("Fucking ") }
      asynchronous (2){ log("World!") }
      asynchronous (1){ log("Priority 1!") }
      Thread.sleep(500)
    }
  }

  object Ex9{

    class MaxThreadCounterLimitException(message:String) extends Exception(message){
      println(message)
    }

    class PriorityTaskPool(val p:Int=2){
      private val syncs:Array[mutable.Queue[() => Unit]]= (for (i <- 1 to 10) yield new mutable.Queue[() => Unit]).toArray// Массив, в котором находятся очереди Queue, индекс массива - приоритет
      val workers:Array[MainWorker]= (for (i <- 1 to p) yield new MainWorker).toArray  // массив воркеров

      // Проверяем, что все очереди пустые начиная с 1
      def isThisEmpty:Boolean={
        @tailrec
        def go(s:Array[mutable.Queue[() => Unit]]):Boolean=s match{
          case x if x.length==1 => s(0).isEmpty
          case x  => if (!(s(0).isEmpty)) false else go(x.tail)
        }
        go(syncs)
      }

      //Если есть непустые очереди, то вытаскиваем самые приоритетный элемент, всегда надо проверять через  isThisEmpty
      def dequeue:() => Unit={
        @tailrec
        def go(s:Array[mutable.Queue[() => Unit]]):() => Unit = s match{
          case s if s.length==1 => s(0).dequeue()
          case s => if (s(0).isEmpty) go(s.tail) else s(0).dequeue()
        }
        go(syncs)
      }

      //Поместить элемент в очередь
      def enqueue(priority:Int, sync:() => Unit)= syncs(priority).enqueue(sync)

      //Потоки демоны
      class MainWorker extends Thread{
        setDaemon(true)
        // функция мониторинга очередей
        def poll() = syncs.synchronized {
          while (isThisEmpty) syncs.wait()
          dequeue
        }
        override def run() = while (true) {
          val task = poll()
          task()
        }
      }

      // запустить все потоки
      def pullStart= for (elem <- workers) elem.start()
      //метод асинхронного добавления в очередь
      def asynchronous(priority: Int)(task: =>Unit): Unit={
        require(priority<=9)
        syncs.synchronized{
          enqueue(priority,() => task)
          syncs.notifyAll()
        }
      }
    }



    //GO GO GO
    def go:Unit={
      val pool=new PriorityTaskPool()
      pool.pullStart
      val t1=thread {
        pool.asynchronous(2) {for (i <- 1 to 10000000){};log(s"Hello ${2}")}
        pool.asynchronous(8) {for (i <- 1 to 10000000){};log(s"MaxPriority ${8}")}
        pool.asynchronous(5) {for (i <- 1 to 10000000){};log(s"Fucking ${5}")}
        pool.asynchronous(7) {for (i <- 1 to 10000000){};log(s"Stupid ${7}")}
        pool.asynchronous(1) {for (i <- 1 to 10000000){};log(s"World ${1}")}
        pool.asynchronous(5) {for (i <- 1 to 10000000){};log(s"Any ${5}")}
        pool.asynchronous(1) {for (i <- 1 to 10000000){};log(s"Value ${1}")}
        pool.asynchronous(1) {for (i <- 1 to 10000000){};log(s"Stupid ${1}")}
        pool.asynchronous(6) {for (i <- 1 to 10000000){};log(s"World ${6}")}
        pool.asynchronous(4) {for (i <- 1 to 10000000){};log(s"Any ${4}")}
        pool.asynchronous(1) {for (i <- 1 to 10000000){};log(s"Value ${1}")}
      }

      t1.join()


      Thread.sleep(500)
    }
  }

  object Ex10{

    class MaxThreadCounterLimitException(message:String) extends Exception(message){
      println(message)
    }

    class PriorityTaskPool(val p:Int=4, val important:Int=3){
      private val syncs:Array[mutable.Queue[() => Unit]]= (for (i <- 1 to 10) yield new mutable.Queue[() => Unit]).toArray// Массив, в котором находятся очереди Queue, индекс массива - приоритет
      val workers:Array[MainWorker]= (for (i <- 1 to p) yield new MainWorker).toArray  // массив воркеров

      private var isBlocked=false // Флаг блокировки внесения новых заданий
      private var terminated=false // Флаг для корректного завершения

      // Проверяем, что все очереди пустые начиная с 1
      def isThisEmpty:Boolean={
        @tailrec
        def go(s:Array[mutable.Queue[() => Unit]]):Boolean=s match{
          case x if x.length==1 => s(0).isEmpty
          case x  => if (!(s(0).isEmpty)) false else go(x.tail)
        }
        go(syncs)
      }

      //Если есть непустые очереди, то вытаскиваем самые приоритетный элемент, всегда надо проверять через  isThisEmpty
      def dequeue:() => Unit={
        @tailrec
        def go(s:Array[mutable.Queue[() => Unit]]):() => Unit = s match{
          case s if s.length==1 => s(0).dequeue()
          case s => if (s(0).isEmpty) go(s.tail) else s(0).dequeue()
        }
        go(syncs)
      }

      //Поместить элемент в очередь
      def enqueue(priority:Int, sync:() => Unit)=
       // if (!isBlocked)
          syncs(priority).enqueue(sync)

      //Потоки демоны
      class MainWorker extends Thread{
        setDaemon(true)
        // функция мониторинга очередей
        def poll() = syncs.synchronized {
          while (isThisEmpty && !terminated) syncs.wait()
          if (!terminated)
            Some(dequeue)//dequeue
          else None
        }
        override def run() = poll() match {
          case Some(task) => task(); run()
          case None =>
        }
      }

      // запустить все потоки
      def pullStart= for (elem <- workers) elem.start()
      //метод асинхронного добавления в очередь
      def asynchronous(priority: Int)(task: =>Unit): Unit={
        require(priority<=9)
        syncs.synchronized{
          enqueue(priority,() => task)
          syncs.notifyAll()
        }
      }

      //Закончить выполнение
      def shutdown(): Unit= syncs.synchronized{
        isBlocked=true//Заблокировать внесение новых заданий
        terminated=true //Флаг завершения
        //for (i <- important to 9) syncs(i).clear()
        syncs.notify()
      }
    }

    //GO GO GO
    def go:Unit={
      val pool=new PriorityTaskPool()
      pool.pullStart
      println("!")
      val t1=thread {
        pool.asynchronous(2) {for (i <- 1 to 1000){};log(s"Hello ${2}")}
        pool.asynchronous(8) {for (i <- 1 to 1000){};log(s"MaxPriority ${8}")}
        pool.asynchronous(5) {for (i <- 1 to 100000){};log(s"Fucking ${5}")}
        pool.asynchronous(7) {for (i <- 1 to 10000){};log(s"Stupid ${7}")}
        pool.asynchronous(1) {for (i <- 1 to 10000){};log(s"World ${1}")}
        pool.asynchronous(5) {for (i <- 1 to 100){};log(s"Any ${5}")}
        pool.asynchronous(1) {for (i <- 1 to 100000){};log(s"Value ${1}")}
        pool.asynchronous(1) {for (i <- 1 to 10000){};log(s"Stupid ${1}")}
        pool.asynchronous(6) {for (i <- 1 to 100000){};log(s"World ${6}")}
        pool.asynchronous(4) {for (i <- 1 to 1){};log(s"Any ${4}")}
        pool.asynchronous(1) {for (i <- 1 to 1000000){};log(s"Value ${1}")}
        pool.shutdown()
      }

      t1.join()


      Thread.sleep(500)
    }
  }
}

