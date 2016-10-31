package co.ledger.manager.web.cli

import co.ledger.manager.web.core.utils.ChromeGlobalPreferences

import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSExport

/**
  *
  * LedgerConfigurationConsoleInterface
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 31/10/2016.
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
trait LedgerConfigurationConsoleInterface extends BaseConsoleInterface {
  private val preferences = new ChromeGlobalPreferences("ledger.manager.config")

  private var _factoryMode = preferences.boolean("factory_mode").getOrElse(false)
  @JSExport
  def enableFactoryMode() = {
    _factoryMode = true
    preferences.edit().putBoolean("factory_mode", _factoryMode).commit()
  }
  @JSExport
  def disableFactoryMode() = {
    _factoryMode = false
    preferences.edit().putBoolean("factory_mode", _factoryMode).commit()
  }
  def isInFactoryMode = _factoryMode


  private var _defaultProvider: Option[String] = preferences.string("default_provider")
  @JSExport
  def setDefaultProvider(provider: UndefOr[String]) = {
    _defaultProvider = provider.toOption
    if (_defaultProvider.isDefined && _defaultProvider.get.nonEmpty) {
      preferences.edit().putString("default_provider", _defaultProvider.get).commit()
    } else {
      preferences.edit().remove("default_provider").commit()
    }
  }
  @JSExport
  def getDefaultProvider(): UndefOr[String] = UndefOr.any2undefOrA[String](_defaultProvider.orNull)
  def defaultProvider = _defaultProvider


  var SELECTED_APPS = preferences.string("selected_app").map(_.split(",")).getOrElse(Array.empty[String])
  def saveSelectedApps(): Unit = {
    preferences.edit().putString("selected_app", SELECTED_APPS.mkString(",")).commit()
  }
}
