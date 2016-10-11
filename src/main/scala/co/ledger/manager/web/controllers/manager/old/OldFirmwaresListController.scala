package co.ledger.manager.web.controllers.manager.old

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.Location
import co.ledger.manager.web.Application
import co.ledger.manager.web.controllers.manager.{ApiDependantController, ManagerController}
import co.ledger.manager.web.core.utils.UrlEncoder
import co.ledger.manager.web.services.{ApiService, DeviceService, WindowService}

import scala.scalajs.js

/**
  *
  * OldFirmwaresListController
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
class OldFirmwaresListController(val windowService: WindowService,
                                     deviceService: DeviceService,
                                 val $scope: Scope,
                                     $location: Location,
                                     $route: js.Dynamic,
                                 val apiService: ApiService) extends Controller
  with ManagerController with ApiDependantController {

  var firmwares = js.Array[ApiService.Firmware]()

  def getFirmwares() = {
    firmwares.array.filter {(item) =>
      isInDevMode() || !item.asInstanceOf[js.Dictionary[js.Any]].dict.lift("developer").exists(_.asInstanceOf[Boolean] == true)
    }
  }
  def isEmpty() = getFirmwares().length == 0
  def toggleDevMode() = {
    Application.developerMode = !Application.developerMode
  }
  def isInDevMode() = Application.developerMode
  override def isLoading(): Boolean = super.isLoading()
  override def fullRefresh(): Unit = super.fullRefresh()
  override def onBeforeRefresh(): Unit = {
    super.onBeforeRefresh()
    firmwares = js.Array()
  }
  override def onAfterRefresh(): Unit = {
    super.onAfterRefresh()
    firmwares = apiService.firmwares.value.flatMap(_.toOption).getOrElse(js.Array())
    js.Dynamic.global.console.log("Firmwares", firmwares)
  }
  def navigateNotes(name: String) = {
    $location.path(s"/old/notes/firmwares/${UrlEncoder.encode(name)}")
    $route.reload()
  }

  refresh()
}

object OldFirmwaresListController {
  def init(module: RichModule) = module.controllerOf[OldFirmwaresListController]("OldFirmwaresListController")
}
