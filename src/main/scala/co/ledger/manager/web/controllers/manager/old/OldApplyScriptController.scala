package co.ledger.manager.web.controllers.manager.old

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.Location
import co.ledger.manager.web.controllers.manager.{ApiDependantController, ManagerController}
import co.ledger.manager.web.services.{ApiService, DeviceService, WindowService}

import scala.scalajs.js

/**
  *
  * OldApplyScriptController
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
class OldApplyScriptController(val windowService: WindowService,
                               deviceService: DeviceService,
                               val $scope: Scope,
                               $location: Location,
                               $route: js.Dynamic,
                               $routeParams: js.Dictionary[String],
                               val apiService: ApiService) extends Controller
  with ManagerController with ApiDependantController {

  val category = $routeParams("category")

}

object OldApplyScriptController {

  def init(module: RichModule) = module.controllerOf[OldApplyScriptController]("OldApplyScriptController")

}
