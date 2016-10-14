package co.ledger.manager.web.controllers.manager.old

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.Location
import co.ledger.manager.web.Application
import co.ledger.manager.web.controllers.manager.{ApiDependantController, ManagerController}
import co.ledger.manager.web.core.remarkable.Remarkable
import co.ledger.manager.web.core.utils.UrlEncoder
import co.ledger.manager.web.services.{ApiService, DeviceService, SessionService, WindowService}

import scala.scalajs.js

/**
  *
  * OldNotesController
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 10/10/2016.
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
class OldNotesController(val windowService: WindowService,
                         deviceService: DeviceService,
                         val $scope: Scope,
                         $location: Location,
                         $route: js.Dynamic,
                         $routeParams: js.Dictionary[String],
                         val apiService: ApiService) extends Controller
  with ManagerController with ApiDependantController {


  val category = $routeParams("category")
  val identifier = $routeParams("identifier")

  override def onBeforeRefresh(): Unit = {

  }

  override def onAfterRefresh(): Unit = {}
  override def fullRefresh(): Unit = {
    super.fullRefresh()
    if (category == "firmwares") {
      $location.path("/old/firmwares/index")
      $route.reload()
    }
  }
  override def isLoading(): Boolean = super.isLoading()
  override def refresh(): Unit = super.refresh()

  def install(app: js.Dynamic): Unit = {
    val path = s"/old/apply/install/apps/$identifier"
    $location.path(path)
    $route.reload()
  }

  def uninstall(app: js.Dynamic): Unit = {
    val path = s"/old/apply/uninstall/apps/${app.identifier.asInstanceOf[String]}/"
    $location.path(path)
    $route.reload()
  }

  def installOsu(app: js.Dynamic): Unit = {
    val path = s"/old/apply/install/osu/${app.identifier.asInstanceOf[String]}/"
    $location.path(path)
    $route.reload()
  }

  def installFirmware(app: js.Dynamic): Unit = {
    val path = s"/old/apply/install/firmware/${app.identifier.asInstanceOf[String]}/"
    $location.path(path)
    $route.reload()
  }

  def icon(name: String) =
    js.Array(Application.httpClient.baseUrl + s"/assets/icons/$name", "images/icons/ic_placeholder.png")

  def openHelpCenter(): Unit = js.Dynamic.global.open("http://support.ledgerwallet.com/help_center")

  def toggleDevMode(): Unit = {
    SessionService.instance.currentSession.get.developerMode = !SessionService.instance.currentSession.get.developerMode
    if (category == "firmwares") {
      $location.path("/old/firmwares/index")
      $route.reload()
    }
  }

  val app = {
    if (category == "apps") {
      apiService.applications.value.get.get.find(_.asInstanceOf[js.Dynamic].identifier == identifier).get
    } else {
      js.Dynamic.literal(icon="")
    }
  }

  val firmware = {
    if (category == "firmwares") {
      apiService.firmwares.value.get.get.find(_.asInstanceOf[js.Dynamic].identifier == identifier).get
    } else {
      js.Dynamic.literal()
    }
  }

  val content: String = {
    category match {
      case "apps" =>
        if (js.isUndefined(app.asInstanceOf[js.Dynamic].notes)) "" else app.asInstanceOf[js.Dynamic].notes.asInstanceOf[String]
      case "firmwares" =>
        if (js.isUndefined(firmware.asInstanceOf[js.Dynamic].notes)) "" else firmware.asInstanceOf[js.Dynamic].notes.asInstanceOf[String]
    }
  }

}

object OldNotesController {
  def init(module: RichModule) = module.controllerOf[OldNotesController]("OldNotesController")
}
