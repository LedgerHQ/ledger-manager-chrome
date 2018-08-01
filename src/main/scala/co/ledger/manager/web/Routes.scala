package co.ledger.manager.web

import biz.enef.angulate.ext.{Route, RouteProvider}

/**
  *
  * Routes
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 03/05/2016.
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
object Routes {

  def declare($routeProvider: RouteProvider) = {
    $routeProvider
      .when("/launch/", Route(templateUrl = "./templates/manager/launch.html"))
      .when("/go/live", Route(templateUrl = "./templates/manager/go_live.html"))
      .when("/applist/", Route(templateUrl = "./templates/manager/app_list.html"))
      .when("/batchapplist/", Route(templateUrl = "./templates/manager/batch_app_list.html"))
      .when("/apply/:script/:product/:name/:params", Route(templateUrl = "./templates/manager/apply_update.html"))
      // Old UI for first Nano S version
      .when("/old/apps/index/", Route(templateUrl = "./templates/manager/old/apps/index.html"))
      .when("/old/notes/:category/:identifier/", Route(templateUrl = "./templates/manager/old/notes.html"))
      .when("/old/firmwares/index/", Route(templateUrl = "./templates/manager/old/firmwares/index.html"))
      .when("/old/plug/", Route(templateUrl = "./templates/manager/old/plug.html"))
      .when("/old/apply/:script/:category/:identifiers*/", Route(templateUrl = "./templates/manager/old/apply.html"))
      .otherwise(Route( redirectTo = "/launch/"))
  }
}
