package co.ledger.manager.web.controllers.manager

import java.io.StringWriter
import java.util.Date

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.Location
import co.ledger.manager.web.Application
import co.ledger.manager.web.core.filesystem.ChromeFileSystem
import co.ledger.manager.web.core.utils.PermissionsHelper
import co.ledger.manager.web.services.{DeviceService, WindowService}
import co.ledger.wallet.core.device.ethereum.LedgerCommonApiInterface.LedgerApiException
import co.ledger.wallet.core.device.ethereum.{LedgerApi, LedgerCommonApiInterface}
import co.ledger.wallet.core.net.WebSocket
import co.ledger.wallet.core.utils.HexUtils
import co.ledger.wallet.core.utils.logs.LogExporter
import org.json.JSONObject

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}
/**
  *
  * ApplyUpdateController
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 09/08/2016.
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
class ApplyUpdateController(val windowService: WindowService,
                            deviceService: DeviceService,
                            $scope: Scope,
                            $route: js.Dynamic,
                            $location: Location,
                            $routeParams: js.Dictionary[String]) extends Controller with ManagerController {

  private val product = $routeParams("product")
  private val productName = $routeParams("name")

  var mode = "wait"
  var lastError = ""
  var progress = 0
  var total = 100

  def percent = ((progress.toDouble / total.toDouble) * 100).toInt

  private def setMode(newMode: String) = {
    if (newMode != mode) {
      mode = newMode
      $scope.$apply()
      true
    } else {
      false
    }
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
          progress = index
          total = apdus.length()
          if (!setMode("load"))
            $scope.$apply()
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

  def exportLogs(): Unit = {
    println("Exporting")
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

  def goHome(): Unit = {
    deviceService.lastConnectedDevice().map {(device) =>
      deviceService.unregisterDevice(device)
      device.disconnect()
    } onComplete {
        case _ =>
          $location.path("/launch")
          $route.reload()
    }
  }

  val endpoint = new StringWriter()
  endpoint.append(s"/${$routeParams("script")}?")
  val params = JSON.parse($routeParams("params")).asInstanceOf[js.Dictionary[js.Any]]
  var first = true
  params.foreach {
    case (k, v) =>
      if (!first)
        endpoint.append('&')
      endpoint.append(s"$k=$v")
      first = false
  }

  val displayableName = {
    product match {
      case "osu" =>
        var hash = params("hash").asInstanceOf[String]
        hash = hash.substring(0, 4) + "..." + hash.substring(hash.length - 4, hash.length)
        s"OSU for firmware $productName ($hash)"
      case "firmware" =>
        s"firmware $productName"
      case "application" =>
        s"$productName application".toLowerCase
    }
  }

  val DisplayableName = displayableName.charAt(0).toUpper + displayableName.substring(1)
  val action = if ($routeParams("script") == "uninstall") "Removing" else "Installing"
  val done = if ($routeParams("script") == "uninstall") "removed" else "installed"

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
      setMode("success")
    case Failure(ex) =>
      ex.printStackTrace()
      lastError = ex.getMessage
      setMode("error")
  }

}

object ApplyUpdateController {
  def init(module: RichModule) = module.controllerOf[ApplyUpdateController]("ApplyUpdateController")
}