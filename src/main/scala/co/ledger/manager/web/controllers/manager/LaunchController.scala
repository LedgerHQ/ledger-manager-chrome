package co.ledger.manager.web.controllers.manager

import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.Location
import biz.enef.angulate.ext.Route
import co.ledger.manager.web.controllers.WindowController
import co.ledger.manager.web.core.utils.ChromeGlobalPreferences
import co.ledger.manager.web.services.{ApiService, DeviceService, SessionService, WindowService}
import co.ledger.wallet.core.device.{Device, DeviceFactory}
import co.ledger.wallet.core.device.DeviceFactory.{DeviceDiscovered, DeviceLost, ScanRequest}
import co.ledger.wallet.core.device.ethereum.LedgerApi
import co.ledger.wallet.core.device.ethereum.LedgerBolosApi.FirmwareVersion

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.scalajs.js.timers.setTimeout
import scala.util.{Failure, Random, Success, Try}

/**
  *
  * LaunchController
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 02/08/2016.
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
class LaunchController(val windowService: WindowService,
                       deviceService: DeviceService,
                       sessionService: SessionService,
                       override val $scope: Scope,
                       $route: js.Dynamic,
                       apiService: ApiService,
                       $location: Location) extends Controller with ManagerController {

  if (js.isUndefined(js.Dynamic.global.isFlashing)) {
    js.Dynamic.global.isFlashing = false
  }
  var isFlashing: Boolean = js.Dynamic.global.isFlashing.asInstanceOf[Boolean]
  private var _scanRequest: Option[ScanRequest] = None
  
  def startDeviceDiscovery(): Unit = {
    println("device discovery")
    if (_scanRequest.isEmpty) {
      _scanRequest = Option(deviceService.requestScan())
      _scanRequest.get.onScanUpdate {
        case DeviceDiscovered(device) =>
          if (_scanRequest.isDefined) {
            connectDevice(device)
            _scanRequest.get.stop()
            _scanRequest = None
          }
        case DeviceLost(device) =>
      }
      _scanRequest.get.duration = DeviceFactory.InfiniteScanDuration
      _scanRequest.get.start()
    }
  }

  def connectDevice(device: Device): Unit = {
    device.connect() flatMap { (_) =>
      LedgerApi(device).needFix()
    } flatMap { (fixNeeded) =>
      val promise = Promise[FirmwareVersion]()
      if (fixNeeded) {
        println("fixNeeded")
        js.Dynamic.global.isFlashing = true
        isFlashing = js.Dynamic.global.isFlashing.asInstanceOf[Boolean]
        setTimeout(0) {
          $scope.$apply()
        }
        LedgerApi(device).getFirmwareVersion() map { (result) =>
          LedgerApi(device).fixMcu() map { (_) =>
            js.Dynamic.global.isFlashing = false
            isFlashing = js.Dynamic.global.isFlashing.asInstanceOf[Boolean]
            setTimeout(0) {
              $scope.$apply()
            }
            promise.success(result)
          }
        }
      } else {
        println("notneeded")
        LedgerApi(device).getFirmwareVersion() map { (result) =>
          promise.success(result)
        }
      }
      promise.future
    } flatMap {(version) =>
      sessionService.startNewSessions(LedgerApi(device)) map {(_) =>
        version
      }
    } flatMap {(version) =>

      def defaultOpen() = {
        $location.path("/old/apps/index/")
        $route.reload()
        Future.successful(null)
      }

      if (version.isOSU) {
        apiService.refresh() map {(_) =>
          deviceService.registerDevice(device)
          val identifier = apiService.firmwares.value.get.toOption flatMap {(firmwares) =>
            firmwares.find(_.name == version.OSUVersion)
          }
          if (identifier.nonEmpty) {
            $location.path(s"/old/apply/install/firmware/${identifier.get.identifier}")
            $route.reload()
          } else {
            defaultOpen()
          }
        }
      } else {
        deviceService.registerDevice(device)
        defaultOpen()
      }
    } onFailure {
      case ex: Throwable =>
        startDeviceDiscovery()
    }
  }

  def stopDeviceDiscovery(): Unit = {
    _scanRequest foreach {(r) =>
      r.stop()
      _scanRequest = None
    }
  }

  def openHelpCenter(): Unit = js.Dynamic.global.open("http://support.ledgerwallet.com/")

  $scope.$on("$destroy", {() =>
    stopDeviceDiscovery()
  })
  println(" start discovery")
  startDeviceDiscovery()
}


object LaunchController {
  def init(module: RichModule) = module.controllerOf[LaunchController]("LaunchController")
}