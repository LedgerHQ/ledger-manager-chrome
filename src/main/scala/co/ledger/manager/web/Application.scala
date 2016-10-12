package co.ledger.manager.web

import java.net.URI

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate._
import biz.enef.angulate.core.HttpService
import biz.enef.angulate.ext.RouteProvider
import co.ledger.manager.web.components._
import co.ledger.manager.web.controllers.WindowController
import co.ledger.manager.web.controllers.manager.old.{OldApplyScriptController, OldAppsListController, OldFirmwaresListController, OldNotesController}
import co.ledger.manager.web.controllers.manager.{AppListController, ApplyUpdateController, BatchAppListController, LaunchController}
import co.ledger.manager.web.core.net.{JQHttpClient, JsWebSocketFactory}
import co.ledger.manager.web.core.utils.ChromePreferences
import co.ledger.manager.web.directives.{AsyncImageSource, MarkDown}
import co.ledger.manager.web.i18n.{I18n, TranslateProvider}
import co.ledger.manager.web.services.{ApiService, DeviceService, SessionService, WindowService}
import co.ledger.wallet.core.utils.logs._

import scala.scalajs.js
import scala.scalajs.js.JSApp

/**
  * Created by pollas_p on 28/04/2016.
  */

object Application extends JSApp{

  val httpClient = new JQHttpClient("http://localhost:3001/update")// new JQHttpClient("https://api.ledgerwallet.com/update")
  val webSocketFactory = new JsWebSocketFactory(URI.create("wss://api.ledgerwallet.com/update"))
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    run()
  }

  var developerMode = false

  def run(): Unit = {
    implicit val module = angular.createModule("app", Seq("ngRoute", "pascalprecht.translate"))
    _module = module

    // Components
    LButton.init(module)
    ActionsBottomBar.init(module)
    ProgressBar.init(module)
    Spinner.init(module)
    Selector.init(module)
    LeftPanel.init(module)

    // Directives
    AsyncImageSource.init(module)
    MarkDown.init(module)

    // Controllers
    WindowController.init(module)
    LaunchController.init(module)
    AppListController.init(module)
    BatchAppListController.init(module)
    ApplyUpdateController.init(module)

    OldAppsListController.init(module)
    OldFirmwaresListController.init(module)
    OldNotesController.init(module)
    OldApplyScriptController.init(module)

    // Services
    WindowService.init(module)
    DeviceService.init(module)
    SessionService.init(module)
    ApiService.init(module)

    // Filters

    ChromePreferences.init()
    module.config(initRoutes _)
    module.config(($compileProvider: js.Dynamic) => {
      $compileProvider.aHrefSanitizationWhitelist(js.RegExp("^\\s*(https?|ftp|mailto|file|chrome-extension):"))
      $compileProvider.imgSrcSanitizationWhitelist(js.RegExp("^\\s*(https?|ftp|mailto|file|chrome-extension):"))
    })
    module.config(initTranslate _)
    module.run(initApp _)
    LoggerPrintStream.init()
    LogSourceMapper.init()

  }

  def initApp($http: HttpService, $rootScope: js.Dynamic, $location: js.Dynamic) = {
    $rootScope.location = $location
  }

  def initRoutes($routeProvider: RouteProvider) = {
    Routes.declare($routeProvider)
  }

  def initTranslate($translateProvider: TranslateProvider) = {
    I18n.init($translateProvider)
  }

  private var _module: RichModule = _
  def module = _module

}