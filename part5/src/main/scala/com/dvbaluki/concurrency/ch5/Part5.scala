package com.dvbaluki
package concurrency.ch5

import java.io.{File, FileOutputStream, PrintWriter}
import java.lang.module.ModuleDescriptor.Requires
import java.util.concurrent.atomic.AtomicLong

import com.typesafe.scalalogging.Logger
import org.slf4j._
import java.util.{Calendar, Date}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
import scala.collection._
import scala.concurrent.Future
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.Source
import scala.reflect.io.Path
import scala.util.Random


package object ch5{
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
  def warmedTimed[T](n: Int = 200)(body: =>T): Double = {
    for (_ <- 0 until n) body
    timed(body)
  }

  @volatile var dummy: Any = _
  def timed[T](body: =>T): Double = {
    val start = System.nanoTime
    dummy = body
    val end = System.nanoTime
    ((end - start) / 1000) / 1000.0
  }
}

import ch5._

object Part5 extends App{

  val list = List.fill(1000000)("")
  val vector = Vector.fill(1000000)("")
  log(s"list conversion time: ${timed(list.par)} ms")
  log(s"vector conversion time: ${timed(vector.par)} ms")
}

