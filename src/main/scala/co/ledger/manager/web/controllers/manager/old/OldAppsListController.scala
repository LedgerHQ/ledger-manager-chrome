package co.ledger.manager.web.controllers.manager.old

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.Location
import co.ledger.manager.web.Application
import co.ledger.manager.web.controllers.manager.ManagerController
import co.ledger.manager.web.core.net.JQHttpClient
import co.ledger.manager.web.services.{DeviceService, WindowService}
import org.scalajs.dom.raw.XMLHttpRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON
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
                                $scope: Scope,
                                $location: Location,
                                $route: js.Dynamic) extends Controller with ManagerController {

  var applications = js.Array[js.Dictionary[js.Any]]()
  var images = scala.collection.mutable.Map[String, js.Any]()

  def fetchApplications(): Future[Unit] = {
    val provider =
      if (!js.isUndefined(js.Dynamic.global.LEDGER) && js.Dynamic.global.LEDGER.asInstanceOf[Boolean] == true)
        "?provider=ledger"
      else
        ""
    Application.httpClient.get("/applications" + provider).json map {
      case (json, _) =>
        if (json.has("nanos")) {
          val apps = json.getJSONArray("nanos")
          applications = JSON.parse(apps.toString).asInstanceOf[js.Array[js.Dictionary[js.Any]]]
        }
    }
  }

  def isLoading() = true
  def isEmpty() = true

  def isInDevMode() = Application.developerMode

  def getApplications() = {
    applications.array.filter {(item) =>
     isInDevMode() || !item.dict.lift("developer").exists(_.asInstanceOf[Boolean] == true)
    }
  }

  def toggleDevMode() = {
    Application.developerMode = !Application.developerMode
  }

  def icon(name: String) =
    js.Array(Application.httpClient.baseUrl + s"/assets/icons/$name", "images/icons/icon_placeholder.png")

  def refresh(): Unit = {
    applications = js.Array[js.Dictionary[js.Any]]()
    fetchApplications() onComplete {
      case Success(_) => $scope.$apply()
      case Failure(ex) =>
        ex.printStackTrace()
    }
  }

  refresh()

}

object OldAppsListController {
  def init(module: RichModule) = module.controllerOf[OldAppsListController]("OldAppsListController")
}