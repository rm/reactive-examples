package observable

import scala.language.postfixOps
import rx.lang.scala.{ Observable, Subscription }
import scala.concurrent._
import duration._
import java.util.Calendar

/* This worksheet demonstrates some of the code snippets from
* Week4, Lecture 2, "Basic Combinators on Observables".
* How concat really works -
* The later observables (in the concat sequence) aren't subscribed to until
* the earlier observables are finished.
* So, there are additional delays, as can be seen by the elapsed time in seconds.
* In comparison, flatten merges the Observables, so they all run simultaneously
* (i.e. asynchronously).
*/

object ob21 {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  def printOut[T](i: Int)(obs: Observable[T])(num: Int)(indent: Int)(etime: () => Double): Unit = {
    blocking { Thread.sleep(20) }
    val is: String = i.toString.padTo(indent, ' ')
    val obsP =
      if (num > 0) obs.take(num)
      else obs
    obsP.subscribe(
      it => {
        val now = etime()
        val itOut = it.toString
        println(f"$is ( $now%5.2f ) $itOut")
      },
      error => {
        val now = etime()
        println(f"$is ( $now%5.2f ) Ooops")
      },
      () => {
        val now = etime()
        println(f"$is ( $now%5.2f ) Completed")
      })
  }                                               //> printOut: [T](i: Int)(obs: rx.lang.scala.Observable[T])(num: Int)(indent: I
                                                  //| nt)(etime: () => Double)Unit

  def block(i: Int)(num: Int) = {
    val t0 = System.nanoTime()
    def etime() = ((System.nanoTime() - t0).toDouble / 1e+9)
    println("Observable: " + i.toString)
    val xs: Observable[Int] = Observable(3, 2, 1)
    val yss: Observable[Observable[Int]] =
      xs.map(x => Observable.interval(x seconds).map(_ => x).take(2))
    val zs: Observable[Int] =
      if (i == 0) yss.concat
      else yss.flatten
    // Unlike the iterable case, we are able to "traverse" the observable
    // multiple "times" through multiple subscriptions.
    printOut(i)(xs)(num)(1)(etime)
    printOut(i)(yss)(num)(11)(etime)
    printOut(i)(zs)(num)(21)(etime)

  }                                               //> block: (i: Int)(num: Int)Unit
  val gap = 15000                                 //> gap  : Int = 15000
  block(0)(-1)                                    //> Observable: 0
                                                  //| 0 (  0.09 ) 3
                                                  //| 0 (  0.10 ) 2
                                                  //| 0 (  0.10 ) 1
                                                  //| 0 (  0.10 ) Completed
                                                  //| 0           (  0.22 ) rx.lang.scala.Observable$$anon$9@5f326484
                                                  //| 0           (  0.22 ) rx.lang.scala.Observable$$anon$9@656546ef
                                                  //| 0           (  0.22 ) rx.lang.scala.Observable$$anon$9@5c1428ea
                                                  //| 0           (  0.22 ) Completed

  blocking { Thread.sleep(gap) } // needed for asynchronous worksheets
                                                  //> 0                     (  3.25 ) 3
                                                  //| 0                     (  6.25 ) 3
                                                  //| 0                     (  8.26 ) 2
                                                  //| 0                     ( 10.26 ) 2
                                                  //| 0                     ( 11.26 ) 1
                                                  //| 0                     ( 12.26 ) 1
                                                  //| 0                     ( 12.26 ) Completed
  block(1)(-1)                                    //> Observable: 1
                                                  //| 1 (  0.02 ) 3
                                                  //| 1 (  0.02 ) 2
                                                  //| 1 (  0.02 ) 1
                                                  //| 1 (  0.02 ) Completed
                                                  //| 1           (  0.05 ) rx.lang.scala.Observable$$anon$9@7d95d4fe
                                                  //| 1           (  0.05 ) rx.lang.scala.Observable$$anon$9@77d2b01b
                                                  //| 1           (  0.05 ) rx.lang.scala.Observable$$anon$9@2927fa12
                                                  //| 1           (  0.05 ) Completed
  blocking { Thread.sleep(gap) } // needed for asynchronous worksheets
                                                  //> 1                     (  1.08 ) 1
                                                  //| 1                     (  2.08 ) 2
                                                  //| 1                     (  2.08 ) 1
                                                  //| 1                     (  3.07 ) 3
                                                  //| 1                     (  4.08 ) 2
                                                  //| 1                     (  6.07 ) 3
                                                  //| 1                     (  6.08 ) Completed
  println("Done")                                 //> Done

}