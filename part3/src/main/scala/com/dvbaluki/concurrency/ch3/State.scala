package com.dvbaluki.concurrency.ch3

import java.util.concurrent.atomic.AtomicReference

sealed trait State
class Idle extends State
class Creating extends State
class Copying(val n: Int) extends State
class Deleting extends State

class Entry(val isDir: Boolean) {
  val state = new AtomicReference[State](new Idle)
}