package co.ledger.manager.web.controllers.manager

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.Location
import co.ledger.manager.web.core.utils.ChromeGlobalPreferences
import co.ledger.manager.web.services.{ApiService, DeviceService, SessionService, WindowService}

import scala.scalajs.js

/**
  * Describe your class here.
  *
  * User: Pierre Pollastri
  * Date: 30-07-2018
  * Time: 12:10
  *
  */
class LedgerLiveController(val windowService: WindowService,
                           deviceService: DeviceService,
                           sessionService: SessionService,
                           override val $scope: Scope,
                           $route: js.Dynamic,
                           apiService: ApiService,
                           $location: Location)
  extends Controller with ManagerController {

  val CounterName = "counter_1"
  val CounterGoal = 2

  def download(): Unit = js.Dynamic.global.open("http://ledger.com/live")

  def continue(): Unit = {
    $location.path("/old/apps/index/")
    $route.reload()
  }

  def openHelpCenter(): Unit = js.Dynamic.global.open("http://support.ledgerwallet.com/")

  private val prefs = new ChromeGlobalPreferences("ledger_live")

  if (prefs.int(CounterName).exists(_ >= CounterGoal))
    continue()
  else
    prefs.edit().putInt(CounterName, 1 + prefs.int(CounterName).getOrElse(0)).commit()

}


object LedgerLiveController {
  def init(module: RichModule) = module.controllerOf[LedgerLiveController]("LedgerLiveController")
}