package com.dvbaluki
package concurrency

import scala.annotation.tailrec

//Not necessary code

object MyApp extends App {
  def fact(n:Int):Option[Int]=n match {

    case n if n>=0 =>
    @tailrec def go(current:Int, result:Int):Int= current match {
        case n if n<=1 => result
        case n if n>1 => go(n-1,result*n)
      }
      Some(go(n,1))

    case _ => None
  }

  val s=new Animal("SOBAKA")
  val s2=new Animal("KOSHKA")
 val s3=new Animal("KOT")
  val s34=new Animal("KOT2")
  val q=new AnimalList[Animal]
  q.add(s)
  q.add(s2)
  q.add(s3)
    q.add(s34)
  q.foreach(println)

}


class AnimalList[A] extends Traversable[A] {

  private var firstLink:Option[Link]=None
  private var currLink:Option[Link]=None

  private case class Link(val value:A){

     var nextLink:Option[Link]=None
  }

  def add(animal:A)={
    currLink match {
      case None =>   {firstLink=new Some(Link(animal));currLink=firstLink}
      case Some(x) =>{x.nextLink=new Some(Link(animal));currLink=x.nextLink}
    }
  }

  @tailrec
  final def foreachMatch[U](l:Option[Link])(f: A => U):Unit={
    l match {
      case None => None
      case Some(x) =>{
        f(x.value)
        foreachMatch(x.nextLink)(f)
      }
    }
  }

  //implement single abstract method from Traversable
  override def foreach[U](f: A => U): Unit = {
    firstLink match {
      case None => None
      case Some(x) => {
        f(x.value)
        foreachMatch(x.nextLink)(f)}
    }
  }

}
case class Animal(val name:String)
class Dog(override val name:String) extends Animal(name)