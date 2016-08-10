package co.ledger.manager.web.controllers.manager

import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.Location
import biz.enef.angulate.ext.Route
import co.ledger.manager.web.controllers.WindowController
import co.ledger.manager.web.services.{DeviceService, WindowService}
import co.ledger.wallet.core.device.{Device, DeviceFactory}
import co.ledger.wallet.core.device.DeviceFactory.{DeviceDiscovered, DeviceLost, ScanRequest}
import co.ledger.wallet.core.device.ethereum.LedgerApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Random, Success}

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
                       $scope: Scope,
                       $route: js.Dynamic,
                       $location: Location) extends Controller with ManagerController {

  private var _scanRequest: Option[ScanRequest] = None

  val id = Random.nextInt()

  println(s"LAUNCH $id")
  def startDeviceDiscovery(): Unit = {
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
    device.connect() flatMap {(_) =>
      LedgerApi(device).getFirmwareVersion()
    } onComplete {
      case Success(_) =>
        deviceService.registerDevice(device)
        $location.path("/applist")
        $route.reload()
      case Failure(ex) =>
        startDeviceDiscovery()
    }
  }

  def stopDeviceDiscovery(): Unit = {
    _scanRequest foreach {(r) =>
      r.stop()
      _scanRequest = None
    }
  }

  $scope.$on("$destroy", {() =>
    stopDeviceDiscovery()
  })

  startDeviceDiscovery()
}

object LaunchController {
  def init(module: RichModule) = module.controllerOf[LaunchController]("LaunchController")
}