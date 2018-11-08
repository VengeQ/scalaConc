package com.dvbaluki
package concurrency.ch1

import scala.annotation.tailrec

object Part1 extends App{
  Exec1.go
  Exec2.go
  Exec3.go
  Exec4.go
  Exec5.go
  Exec6.go
  Exec7.go

  object Exec1{
    println("\nExercise1\n----------------------------")
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
    println("\nExercise2\n----------------------------")
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
    println("\nExercise3\n----------------------------")
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
    println("\nExercise4\n----------------------------")
    case class Pair[P, Q](val first: P, val second: Q){

      def get(f:P):Option[(P,Q)]=
        if (f==first) Some((first, second)) else None

    }
    object Pair{
      def apply[P,Q](f:P,s:Q)=new Pair(f,s)
    }

    def go= {
      val t = com.dvbaluki.concurrency.ch1.Part1.Exec4.Pair(2, 3)

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
    println("\nExercise5\n----------------------------")
    def permutations(x: String)(pred:String=>String): Seq[String]={


      var a=pred(x.sortWith(_<=_))//start String
      var result:List[String]=List(a) //resultList

      //Naraina algorithm

      //number of combo
      val b=a
      while (b!=a.reverse) {
        // need merge this rec functions
        @tailrec
        def findMaxJ(str:String):Int=
          if (str.length<2) -1 else
          if (str.charAt(str.length-2)<str.charAt(str.length-1)) str.length-2 else findMaxJ(str.substring(0,str.length-1))
        @tailrec
        def findMaxL(str:String,x:Int,startString:String):Int=
          if (str.length==0) -1 else
          if (startString(x)<str.charAt(str.length-1)) str.length+x else findMaxL(str.substring(0,str.length-1),x,startString)

        val b = a //curr string
        val curMax=findMaxJ(b) // find max j where Aj<Aj+1
        val lMax=findMaxL(b.substring(curMax+1),curMax,b) // find max l where l>j for Al>Aj

        //new String 0..a(j) + a(j+1)..n where swap(a(j), a(l))
        if (curMax >= 0 && lMax>0) a = b.substring(0, curMax) + b.charAt(lMax) +(b.substring(curMax + 1, lMax) + b.charAt(curMax) + b.substring(lMax+1, b.length)).reverse

        result=a :: result //add to Seq
      }
      result

    }

    def go=println(permutations("hello")((x:String)=>x.toLowerCase))
  }

  object Exec6{
    println("\nExercise6\n----------------------------")
    def combinations(n: Int, xs: Seq[Int]): Iterator[Seq[Int]]= {
      var a: Set[Seq[Int]] = Set()
      def loop(n: Int, xs: Seq[Int]): Unit = {
        var i, j = 0
        if (n == xs.length) {
          val seq:Seq[Int] = for (i <- 0 to n - 1) yield xs(i)
          a = a + seq
        }
        else
          for (j <- xs.indices)
            j match {
              case 0 => loop(n, xs.drop(1))
              case x => loop(n, xs.take(x) ++ xs.drop(x+1))
            }
      }
      loop(n, xs)
      a foreach(println)
      a toIterator
    }

    def go=combinations(3,(1 to 5).toSeq)
  }

  object Exec7 {
    println("\nExercise7\n----------------------------")
    def matcher(regex: String): PartialFunction[String, List[String]] = {
      val reg = regex.r

      val result: PartialFunction[String, List[String]] = {
        case a if ((reg findFirstIn a) match {
          case Some(x) => true
          case None => false
        }) => (for (matchString <- reg findAllMatchIn a) yield matchString toString) toList
      }
      result
    }

    def go={
      val a=matcher("h")
      if (a.isDefinedAt("hoho"))   println(a("hoho"))
      if (a.isDefinedAt("baba"))   println(a("baba"))
    }
  }
}

