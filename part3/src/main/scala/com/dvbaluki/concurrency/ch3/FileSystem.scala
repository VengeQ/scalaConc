package com.dvbaluki.concurrency.ch3

import java.io.File
import java.util._
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters._
import org.apache.commons.io.FileUtils

import scala.collection.concurrent.Map
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

object FileSystem {
  @tailrec private def prepareForDelete(entry: Entry): Boolean = {
    val s0 = entry.state.get
    s0 match {
      case i: Idle =>
        if (entry.state.compareAndSet(s0, new Deleting)) true
        else prepareForDelete(entry)
      case c: Creating =>
        println("File currently created, cannot delete."); false
      case c: Copying =>
        println("File currently copied, cannot delete."); false
      case d: Deleting =>
        false
    }
  }
  def execute(body: =>Unit) = ExecutionContext.global.execute(
    new Runnable { def run() = body }
  )



}

class FileSystem(val root: String) {
  def deleteFile(filename: String): Unit = {
    files.get(filename) match {
      case None =>
        println(s"Path $filename does not exist!")
      case Some(entry) if entry.isDir =>
        println(s"Path $filename is a directory!")
      case Some(entry) => FileSystem.execute {

        if (FileSystem.prepareForDelete(entry)) {
          println("file is ready")
          if (FileUtils.deleteQuietly(new File(filename)))
            files.remove(filename)
        }
      }
      case _ => println("what?!")
    }
  }

  val rootDir = new File(root)
  val files: Map[String, Entry]=new ConcurrentHashMap[String, Entry]().asScala

  for (f <- FileUtils.iterateFiles(rootDir, null, false).asScala)
    files.put(f.getName, new Entry(false))

  files.foreach((f)=>println(f._1))


}



