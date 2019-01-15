package com.dvbaluki
package concurrency.ch6

import rx.lang.scala.{Observable, Observer, Scheduler, Subscriber, Subscription}
import com.typesafe.scalalogging.Logger
import org.slf4j._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.concurrent.blocking
import scala.util.Random
import scala.swing._
import scala.swing.event._
import java.util.concurrent.Executor

import rx.schedulers.Schedulers.{from => fromExecutor}
import javax.swing.SwingUtilities.invokeLater


package object ch6{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  def log(msg: String): Unit =
    logger.info(msg)

  def thread(body: =>Unit): Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }

}

import ch6._

object Part6 extends  SimpleSwingApplication {

  def top = new BrowserFrame  with BrowserLogic
}

abstract class BrowserFrame extends MainFrame {
  val swingScheduler = new Scheduler {
    val asJavaScheduler = fromExecutor(new Executor {
      def execute(r: Runnable) = invokeLater(r)
    })
  }

  implicit class ButtonOps(val self: Button) {
    def clicks = Observable.apply[Unit] { obs =>
      self.reactions += {
        case ButtonClicked(_) => obs.onNext(())
      }
      Subscription()
    }
  }
  implicit class TextFieldOps(val self: TextField) {
    def texts = Observable.apply[String] { obs =>
      self.reactions += {
        case ValueChanged(_) => obs.onNext(self.text)
      }
      Subscription()
    }
  }
  title = "MiniBrowser"
  val specUrl = "http://www.w3.org/Addressing/URL/url-spec.txt"
  val urlfield = new TextField(specUrl)
  val pagefield = new TextArea
  val button = new Button {
    text = "Feeling Lucky"
  }
  contents = new BorderPanel {
    import BorderPanel.Position._
    layout(new BorderPanel {
      layout(new Label("URL:")) = West
      layout(urlfield) = Center
      layout(button) = East
    }) = North
    layout(pagefield) = Center
  }
  size = new Dimension(1024, 768)

}

trait BrowserLogic {
  self: BrowserFrame =>
  def suggestRequest(term: String): Observable[String] = {
    val url = s"http://suggestqueries.google.com/complete/search?client=firefox&q=$term"
    val request = Future { Source.fromURL(url).mkString }
    Observable.from(request)
      .timeout(0.5.seconds)
      .onErrorReturn(e => "(no suggestion)")
  }
  def pageRequest(url: String): Observable[String] = {
    val urlWithProtocol= url match {
      case startWithHttp:String if startWithHttp.startsWith("http://") => startWithHttp
      case anotherStartSymbols:String=> "http://"+anotherStartSymbols
    }
    val request = Future { Source.fromURL(urlWithProtocol).mkString }
    Observable.from(request)
      .timeout(4.seconds)
      .onErrorReturn(e => s"Could not load page: $e")
  }
  urlfield.texts.map(suggestRequest).concat
    .observeOn(swingScheduler)
    .subscribe(response => pagefield.text = response)
  button.clicks.map(_ => pageRequest(urlfield.text)).concat
    .observeOn(swingScheduler)
    .subscribe(response => pagefield.text = response)
}
