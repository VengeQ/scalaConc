package com.dvbaluki
package concurrency.ch2

import java.io.{File, FileOutputStream, PrintWriter}
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic._

import com.typesafe.scalalogging.Logger
import org.slf4j._
import java.util.{Calendar, Date}
import scala.annotation.tailrec
import scala.concurrent._
import scala.collection._
import scala.io.Source


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

  object FileSystem{
    class Entry(val isDir: Boolean) {
      sealed trait State
      class Idle extends State
      class Creating extends State
      class Copying(val n: Int) extends State
      class Deleting extends State

      val state = new AtomicReference[State](new Idle)
    }
  }

}

