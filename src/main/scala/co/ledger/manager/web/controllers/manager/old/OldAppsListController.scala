package co.ledger.manager.web.controllers.manager.old

import java.util.Date

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.Location
import co.ledger.manager.web.Application
import co.ledger.manager.web.controllers.manager.{ApiDependantController, ManagerController}
import co.ledger.manager.web.core.net.JQHttpClient
import co.ledger.manager.web.core.utils.UrlEncoder
import co.ledger.manager.web.services.{ApiService, DeviceService, SessionService, WindowService}
import org.scalajs.dom.raw.XMLHttpRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.{JSON, timers}
import scala.util.{Failure, Success}

/**
  *
  * OldAppsListController
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 06/10/2016.
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
class OldAppsListController(val windowService: WindowService,
                                deviceService: DeviceService,
                            val $scope: Scope,
                                $location: Location,
                                $route: js.Dynamic,
                            val apiService: ApiService) extends Controller
  with ManagerController with ApiDependantController {

  var applications = js.Array[ApiService.App]()
  var images = scala.collection.mutable.Map[String, js.Any]()

  def isEmpty() = getApplications().length == 0

  def isInDevMode() = SessionService.instance.currentSession.get.developerMode

  def getApplications() = {
    applications.array.filter {(item) =>
     isInDevMode() || !item.asInstanceOf[js.Dictionary[js.Any]].dict.lift("developer").exists(_.asInstanceOf[Boolean] == true)
    }
  }

  def toggleDevMode() = {
    SessionService.instance.currentSession.get.developerMode = !SessionService.instance.currentSession.get.developerMode
  }

  def icon(name: String) =
    js.Array(Application.httpClient.baseUrl + s"/assets/icons/$name", "images/icons/ic_placeholder.png")

  def navigateNotes(identifier: String) = {
    $location.path(s"/old/notes/apps/$identifier/")
    $route.reload()
  }

  def install(app: js.Dynamic): Unit = {
    val path = s"/old/apply/install/apps/${app.identifier}/"
    $location.path(path)
    $route.reload()
  }

  def uninstall(app: js.Dynamic): Unit = {
    val path = s"/old/apply/uninstall/apps/${app.identifier}/"
    $location.path(path)
    $route.reload()
  }

  override def onBeforeRefresh(): Unit = {
    println("YO")
    applications = js.Array()
    js.Dynamic.global.console.log(applications)
  }


  override def onAfterRefresh(): Unit = {
    applications = apiService.applications.value.flatMap(_.toOption).getOrElse(js.Array())
    js.Dynamic.global.console.log(applications)
  }

  override def fullRefresh(): Unit = super.fullRefresh()
  override def isLoading(): Boolean = super.isLoading()

  refresh()
}

object OldAppsListController {
  def init(module: RichModule) = module.controllerOf[OldAppsListController]("OldAppsListController")
}