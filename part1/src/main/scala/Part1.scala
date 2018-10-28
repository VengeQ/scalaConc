package com.dvbaluki
package concurrency.ch1

object Part1 extends App{
  Exec1.go
}


object Exec1{

  def compose[A,B,C](g: B => C, f: A => B): A => C =
    (x:A)=>g(f(x))


  def g(a:Int):(Int=>String) = {println(s"convert Int $a to String $a");a => a.toString}
  def f(a:Double):(Double=>Int) ={println(s"convert Double $a to String $a");a=>a.toInt}

  def go ={
    val a=(compose(g(3),f(4)))
    println(a(3))
  }
}