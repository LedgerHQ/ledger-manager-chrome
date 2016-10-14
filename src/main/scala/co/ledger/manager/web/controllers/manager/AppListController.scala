package co.ledger.manager.web.controllers.manager

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.Location
import biz.enef.angulate.{Controller, Scope}
import co.ledger.manager.web.Application
import co.ledger.manager.web.core.utils.UrlEncoder
import co.ledger.manager.web.services.{DeviceService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
  *
  * AppListController
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
class AppListController(val windowService: WindowService,
                        deviceService: DeviceService,
                        $scope: Scope,
                        $location: Location,
                        $route: js.Dynamic) extends Controller with ManagerController {

  var applications = js.Array[js.Dictionary[js.Any]]()
  var firmwares = js.Array[js.Dictionary[js.Any]]()

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

  def fetchFirmware(): Future[Unit] = {
    val provider =
      if (!js.isUndefined(js.Dynamic.global.LEDGER) && js.Dynamic.global.LEDGER.asInstanceOf[Boolean] == true)
        "?provider=ledger"
      else
        ""
    Application.httpClient.get("/firmwares" + provider).json map {
      case (json, _) =>
        if (json.has("nanos")) {
          val firms = json.getJSONArray("nanos")
          firmwares = JSON.parse(firms.toString).asInstanceOf[js.Array[js.Dictionary[js.Any]]]
        }
    }
  }

  def installFirmware(pkg: js.Dynamic): Unit = {
    install("firmware", pkg.name.asInstanceOf[String], pkg.`final`)
  }

  def installFirmwareOsu(pkg: js.Dynamic): Unit = {
    install("osu", pkg.name.asInstanceOf[String], pkg.`osu`)
  }

  def installApp(pkg: js.Dynamic): Unit = {
    install("application", pkg.name.asInstanceOf[String], pkg.app)
  }

  private def install(product: String, name: String, pkg: js.Dynamic): Unit = {
    $location.path(s"/apply/install/${UrlEncoder.encode(product)}/${js.Dynamic.global.encodeURIComponent(name)}/${UrlEncoder.encode(JSON.stringify(pkg))}/")
    $route.reload()
  }

  def uninstall(pkg: js.Dynamic): Unit = {
    val params = js.Dynamic.literal(
      appName = pkg,
      targetId = 0x31100002
    )
    val name = js.Dynamic.global.encodeURIComponent(pkg)
    $location.path(s"/apply/uninstall/application/$name/${UrlEncoder.encode(JSON.stringify(params))}/")
    $route.reload()
  }

  def batchInstall(): Unit = {
    $location.path(s"/batchapplist/")
    $route.reload()
  }

  def refresh(): Unit = {
    applications = js.Array[js.Dictionary[js.Any]]()
    firmwares = js.Array[js.Dictionary[js.Any]]()
    fetchApplications() flatMap {(_) =>
      fetchFirmware()
    } onComplete {
      case Success(_) => $scope.$apply()
      case Failure(ex) =>
        ex.printStackTrace()
    }
  }

  refresh()

}

object AppListController {

  def init(module: RichModule) = module.controllerOf[AppListController]("AppListController")

}
