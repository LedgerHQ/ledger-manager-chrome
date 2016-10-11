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



  override def onBeforeRefresh(): Unit = {}

  override def onAfterRefresh(): Unit = {}

  val category = $routeParams("category")
  val identifier = $routeParams("identifier")

  println(category)

  def install(app: js.Dynamic): Unit = {
    val path = s"/old/apply/install/apps/${UrlEncoder.encode(app.name.asInstanceOf[String])}"
    $location.path(path)
    $route.reload()
  }

  def uninstall(app: js.Dynamic): Unit = {
    val path = s"/old/apply/uninstall/apps/${UrlEncoder.encode(app.name.asInstanceOf[String])}/"
    $location.path(path)
    $route.reload()
  }

  def installOsu(app: js.Dynamic): Unit = {
    val path = s"/old/apply/install/osu/${UrlEncoder.encode(app.name.asInstanceOf[String])}"
    $location.path(path)
    $route.reload()
  }

  def installFirmware(app: js.Dynamic): Unit = {
    val path = s"/old/apply/install/firmware/${UrlEncoder.encode(app.name.asInstanceOf[String])}"
    $location.path(path)
    $route.reload()
  }

  def icon(name: String) =
    js.Array(Application.httpClient.baseUrl + s"/assets/icons/$name", "images/icons/icon_placeholder.png")

  val app = {
    if (category == "apps") {
      apiService.applications.value.get.get.find(_.asInstanceOf[js.Dynamic].name == identifier).get
    } else {
      js.Dynamic.literal(icon="")
    }
  }

  val firmware = {
    if (category == "firmwares") {
      apiService.firmwares.value.get.get.find(_.asInstanceOf[js.Dynamic].name == identifier).get
    } else {
      js.Dynamic.literal()
    }
  }

  val content = js.Array(
    js.Dynamic.literal(
      title = "Release notes",
      text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
    )
  )

}

object OldNotesController {
  def init(module: RichModule) = module.controllerOf[OldNotesController]("OldNotesController")
}
