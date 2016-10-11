package co.ledger.manager.web.controllers.manager

import java.util.Date

import biz.enef.angulate.Scope
import co.ledger.manager.web.services.ApiService

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.timers
import scala.scalajs.js.timers._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  * ApiDependantController
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
trait ApiDependantController {

  val apiService: ApiService
  val $scope: Scope

  private var _loading = false
  def isLoading() = _loading

  def onBeforeRefresh(): Unit = {}

  def onAfterRefresh(): Unit = {}

  def refresh(): Unit = {
    onBeforeRefresh()
    _loading = !apiService.applications.isCompleted
    val startDate = new Date().getTime
    val applyUi = {() =>
      if (_loading) {
        import timers._
        val wait = Math.max(1500, new Date().getTime - startDate)
        setTimeout(wait) {
          _loading = false
          $scope.$apply()
        }
      } else {
        _loading = false
        $scope.$apply()
      }
    }
    apiService.applications onComplete {
      case Success(apps) =>
        onAfterRefresh()
        applyUi()
      case Failure(ex) =>
        ex.printStackTrace()
        applyUi()
    }
  }

  def fullRefresh(): Unit = {
    if (!_loading) {
      apiService.refresh()
      refresh()
    }
  }

}
