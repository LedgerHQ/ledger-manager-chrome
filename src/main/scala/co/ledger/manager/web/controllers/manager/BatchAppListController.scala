package co.ledger.manager.web.controllers.manager

/**
  *
  * BatchAppListController
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 29/09/2016.
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

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.Location
import co.ledger.manager.web.Application
import co.ledger.manager.web.services.{DeviceService, WindowService}

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class BatchAppListController(val windowService: WindowService,
                             deviceService: DeviceService,
                             $scope: Scope,
                             $route: js.Dynamic,
                             $location: Location,
                             $routeParams: js.Dictionary[String]) extends Controller with ManagerController {

  var applications = js.Array[js.Dictionary[js.Any]]()

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

  def isChecked(application: js.Dynamic): Boolean = {
    BatchAppListController.SELECTED_APPS.contains(application.identifier.asInstanceOf[String])
  }

  def toggle(application: js.Dynamic): Unit = {
    if (isChecked(application)) {
      BatchAppListController.SELECTED_APPS = BatchAppListController.SELECTED_APPS.filter(_ != application.identifier.asInstanceOf[String])
    } else {
      BatchAppListController.SELECTED_APPS = BatchAppListController.SELECTED_APPS :+ application.identifier.asInstanceOf[String]
    }
  }

  def back(): Unit = {
    $location.path("/applist/")
    $route.reload()
  }

  def installBatch(): Unit = {
    val apps = applications.filter({(item) => isChecked(item.asInstanceOf[js.Dynamic])}).map(_.asInstanceOf[js.Dynamic])
    apps foreach {
     js.Dynamic.global.console.log("Install ", _)
    }
    ApplyUpdateController.APP_BATCH = apps.toArray
    $location.path(s"/apply/batch/application/${js.Dynamic.global.encodeURIComponent(apps(0).name)}/${JSON.stringify(apps(0).app)}/")
  }

  def refresh(): Unit = {
    applications = js.Array[js.Dictionary[js.Any]]()
    fetchApplications()  onComplete {
      case Success(_) => $scope.$apply()
      case Failure(ex) =>
        ex.printStackTrace()
    }
  }

  refresh()

}

object BatchAppListController {

  // Don't do that
  var SELECTED_APPS = Array[String]()

  def init(module: RichModule) = {
    module.controllerOf[BatchAppListController]("BatchAppListController")
  }

}