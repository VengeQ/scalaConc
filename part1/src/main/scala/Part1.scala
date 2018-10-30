package com.dvbaluki
package concurrency.ch1

object Part1 extends App{

  Exec4.go

}


object Exec1{
  def compose[A,B,C](g: B => C, f: A => B): A => C =
    (x:A)=>g(f(x))

  def g(a:Int):(Int=>String) = {println(s"convert Int  to String and add 2");a => (a+2) toString}
  def f(a:Double):(Double=>Int) ={println(s"convert Double to Int  and add 3");a=>(a+3) toInt}

  def go ={
    val a=(compose(g(3),f(4)))
    println(a(12))
  }
}

object Exec2{
  def fuse[A, B]
  (a: Option[A], b: Option[B]): Option[(A, B)] = {
    val tup= for (x <- a; y <-b ) yield Option(x,y)
    tup match{
      case Some(x)=>x
      case None => None
    }
  }

  def go={
    println("""With a=Option(2) and b=Option("Hello")""")
    println(fuse(Option(2),Option("Hello")))

    println("""With a=Option(2) and b=None""")
    println(fuse(Option(2),None))
  }
}

object Exec3{
  def check[T](xs: Seq[T])(pred: T => Boolean): Boolean ={
    var result=true
    for (x <- xs; if result) {
      result= try pred(x)
      catch {
        case ex: Exception => false
      }
    }
    result
  }

  def go={
    println(check(0 until 10)(40 / _ > 0))
  }
}

object Exec4{
  case class Pair[P, Q](val first: P, val second: Q){

    def get(f:P):Option[(P,Q)]=
      if (f==first) Some((first, second)) else None

  }
  object Pair{
    def apply[P,Q](f:P,s:Q)=new Pair(f,s)
  }

    def go= {
      val t = com.dvbaluki.concurrency.ch1.Exec4.Pair(2, 3)

      println(t.get(4) match {
        case Some(a) => a
        case None => None
      })
      println(t.get(2) match {
        case Some(a) => a
        case None => None
      })
    }
}

object Exec5{
  def permutations(x: String): Seq[String]={

    //Naraina algorithm
    var a=x.sortWith(_<=_) //start String
    var result:List[String]=List(a)
    //number of combo
    val cc=a.foldLeft(1)((res:Int,p:Char)=>(a.indexOf(p)+1)*res)

    for (i <- 1 to cc-1) {
      var b = a //curr string
      var curMax = -1
      var lMax = -1
      //find rightMax j where a(j)<a(j+1)
      for (j <- 0 until a.length - 1; if (a.charAt(j) < a.charAt(j + 1))) curMax = j
      // find lMax l where l>j and a(l)>a)j)
      if (curMax >= 0) for (l <- 0 until a.length; if a.charAt(l) > a.charAt(curMax)) lMax = l
      //new String 0..a(j) + a(j+1)..n where swap(a(j), a(l))
      if (curMax >= 0 && lMax>0) a = b.substring(0, curMax) + b.charAt(lMax) +(b.substring(curMax + 1, lMax) + b.charAt(curMax) + b.substring(lMax+1, b.length)).reverse

      //add to Seq
      result=a :: result
    }
    result
  }

  def go=println(permutations("1234"))
}