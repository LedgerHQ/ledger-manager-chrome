package co.ledger.manager.web

import java.net.URI

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate._
import biz.enef.angulate.core.HttpService
import biz.enef.angulate.ext.RouteProvider
import co.ledger.manager.web.components._
import co.ledger.manager.web.controllers.WindowController
import co.ledger.manager.web.controllers.manager.{AppListController, ApplyUpdateController, LaunchController}
import co.ledger.manager.web.core.net.{JQHttpClient, JsWebSocketFactory}
import co.ledger.manager.web.core.utils.ChromePreferences
import co.ledger.manager.web.i18n.{I18n, TranslateProvider}
import co.ledger.manager.web.services.{DeviceService, SessionService, WindowService}
import co.ledger.wallet.core.utils.logs._

import scala.scalajs.js
import scala.scalajs.js.JSApp

/**
  * Created by pollas_p on 28/04/2016.
  */

object Application extends JSApp{

  val httpClient = new JQHttpClient("http://localhost:3000/update")
  val webSocketFactory = new JsWebSocketFactory(URI.create("ws://localhost:3001"))
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    run()
  }

  def run(): Unit = {
    implicit val module = angular.createModule("app", Seq("ngRoute", "pascalprecht.translate"))
    _module = module

    // Components
    LButton.init(module)
    ActionsBottomBar.init(module)
    ProgressBar.init(module)
    Spinner.init(module)
    Selector.init(module)

    // Controllers
    WindowController.init(module)
    LaunchController.init(module)
    AppListController.init(module)
    ApplyUpdateController.init(module)

    // Services
    WindowService.init(module)
    DeviceService.init(module)
    SessionService.init(module)

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