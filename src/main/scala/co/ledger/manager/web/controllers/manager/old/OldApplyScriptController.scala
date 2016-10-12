package co.ledger.manager.web.controllers.manager.old

import java.io.StringWriter
import java.util.Date

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.Location
import co.ledger.manager.web.Application
import co.ledger.manager.web.controllers.manager.{ApiDependantController, ManagerController}
import co.ledger.manager.web.core.filesystem.ChromeFileSystem
import co.ledger.manager.web.core.utils.PermissionsHelper
import co.ledger.manager.web.services.{ApiService, DeviceService, WindowService}
import co.ledger.wallet.core.device.ethereum.LedgerApi
import co.ledger.wallet.core.device.ethereum.LedgerCommonApiInterface.LedgerApiException
import co.ledger.wallet.core.net.WebSocket
import co.ledger.wallet.core.utils.HexUtils
import co.ledger.wallet.core.utils.logs.LogExporter
import org.json.JSONObject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
  *
  * OldApplyScriptController
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 11/10/2016.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2016 Ledger
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  */
class OldApplyScriptController(val windowService: WindowService,
                               deviceService: DeviceService,
                               val $scope: Scope,
                               $location: Location,
                               $route: js.Dynamic,
                               $routeParams: js.Dictionary[String],
                               val apiService: ApiService) extends Controller
  with ManagerController with ApiDependantController {

  val category = {
    $routeParams("category") match {
      case "osu" => "firmwares"
      case "firmware" => "firmwares"
      case others => others
    }
  }
  val product = $routeParams("category")
  val identifier = $routeParams("identifier")
  val script = $routeParams("script")
  var hasError = false
  val pkg = {
    product match {
      case "osu" =>
        apiService.firmwares.value.get.get.find(_.asInstanceOf[js.Dynamic].identifier == identifier).get.asInstanceOf[js.Dynamic]
      case "firmware" =>
        apiService.firmwares.value.get.get.find(_.asInstanceOf[js.Dynamic].identifier == identifier).get.asInstanceOf[js.Dynamic]
      case others =>
        apiService.applications.value.get.get.find(_.asInstanceOf[js.Dynamic].identifier == identifier).get.asInstanceOf[js.Dynamic]
    }
  }
  println(product)
  println(identifier)

  def ellipsize(text: String): String = {
    text.substring(0, 4) + "..." + text.substring(text.length - 4, text.length)
  }

  def exportLogs() = {
    PermissionsHelper.requestIfNecessary("fileSystem") flatMap { _ =>
      PermissionsHelper.requestIfNecessary("fileSystem.write")
    } flatMap {_ =>
      ChromeFileSystem.chooseFileEntry(s"ledger-ethereum-chrome-logs-${new Date().getTime}.logs")
    } flatMap {(entry) =>
      LogExporter.toBlob flatMap {(content) =>
        entry.write(content)
      }
    } onComplete {
      case Success(_) =>
      case Failure(ex) => ex.printStackTrace()
    }
  }

  def next(): Unit = {
    if (hasError == false && category == "firmwares")
      return
    if (category == "apps") {
      $location.path("/old/apps/index/")
    } else {
      $location.path("/old/firmwares/index/")
    }
    $route.reload()
  }

  private def answer(socket: WebSocket)(response: js.Dynamic) = {
    socket.send(JSON.stringify(response))
  }

  private def performExchange(socket: WebSocket, message: JSONObject, promise: Promise[Unit]): Unit = {
    deviceService.lastConnectedDevice() onComplete {
      case Success(device) =>
        LedgerApi(device).exchange(HexUtils.decodeHex(message.getString("data"))) onComplete{
          case Success(data) =>
            answer(socket)(js.Dynamic.literal(
              nonce = message.getInt("nonce"),
              response = "success",
              data = HexUtils.encodeHex(data)
            ))
          case Failure(ex: LedgerApiException) =>
            answer(socket)(js.Dynamic.literal(
              nonce = message.getInt("nonce"),
              response = "error",
              data = Integer.toHexString(ex.sw)
            ))
          case Failure(ex: Throwable) =>
            answer(socket)(js.Dynamic.literal(
              nonce = message.getInt("nonce"),
              response = "fatal_error"
            ))
        }
      case Failure(ex) => promise.failure(ex)
    }
  }

  private def performBulkExchange(socket: WebSocket, message: JSONObject, promise: Promise[Unit]): Unit = {
    deviceService.lastConnectedDevice() onComplete {
      case Success(device) =>
        val apdus = message.getJSONArray("data")
        def iterate(index: Int = 0, lastResult: Array[Byte] = Array.empty[Byte]): Future[Array[Byte]] = {
          if (index >= apdus.length()) {
            Future.successful(lastResult)
          } else {
            LedgerApi(device).exchange(HexUtils.decodeHex(apdus.getString(index))) flatMap {
              iterate(index + 1, _)
            }
          }
        }
        iterate() onComplete {
          case Success(data) =>
            answer(socket)(js.Dynamic.literal(
              nonce = message.getInt("nonce"),
              response = "success",
              data = HexUtils.encodeHex(data)
            ))
          case Failure(ex: LedgerApiException) =>
            answer(socket)(js.Dynamic.literal(
              nonce = message.getInt("nonce"),
              response = "error",
              data = Integer.toHexString(ex.sw)
            ))
          case Failure(ex: Throwable) =>
            answer(socket)(js.Dynamic.literal(
              nonce = message.getInt("nonce"),
              response = "fatal_error"
            ))
        }
      case Failure(ex) => promise.failure(ex)
    }
  }

  val endpoint = new StringWriter()
  endpoint.append(s"/$script?")
  val params = {
    product match {
      case "osu" =>
        pkg.osu
      case "firmware" =>
        pkg.`final`
      case other =>
        if (script == "install")
          pkg.app
        else
          js.Dynamic.literal(appName = pkg.name, targetId = pkg.app.targetId)
    }
  }.asInstanceOf[js.Dictionary[js.Any]]
  var first = true
  params.foreach {
    case (k, v) =>
      if (!first)
        endpoint.append('&')
      endpoint.append(s"$k=$v")
      first = false
  }

  def applyScript(): Unit = {
    Application.webSocketFactory.connect(endpoint.toString) flatMap {(socket) =>
      val promise = Promise[Unit]()
      socket.onJsonMessage({(message) =>
        println("Received " + message.toString(2))
        println("Query: " + message.getString("query"))
        message.getString("query") match {
          case "exchange" =>
            performExchange(socket, message, promise)
          case "bulk" =>
            performBulkExchange(socket, message, promise)
          case "error" =>
            promise.failure(new Exception(message.optString("data", "Unknown error")))
          case "success" =>
            promise.success()
        }
      })
      socket.onClose {(ex) =>
        if (!promise.isCompleted)
          promise.failure(new Exception("Socket closed"))
      }
      promise.future
    } onComplete {
      case Success(_) =>
        next()
      case Failure(ex) =>
        ex.printStackTrace()
        hasError = true
        $scope.$digest()
    }
  }

  applyScript()
}

object OldApplyScriptController {

  def init(module: RichModule) = module.controllerOf[OldApplyScriptController]("OldApplyScriptController")

}
