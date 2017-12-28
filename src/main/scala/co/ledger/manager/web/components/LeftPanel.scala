package co.ledger.manager.web.components

import java.util.Date

import biz.enef.angulate.{Directive, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite, Location}
import co.ledger.manager.web.components.LeftPanel.LeftPanelScope
import co.ledger.manager.web.services.{ApiService, SessionService}
import co.ledger.wallet.core.device.utils.EventReceiver
import org.widok.moment.Moment

import scala.concurrent.duration
import scala.scalajs.js
import scala.scalajs.js.{Dictionary, UndefOr, timers}
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  *
  * LeftPanel
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 05/10/2016.
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
class LeftPanel( $location: Location,
                 $route: js.Dynamic,
                 $parse: js.Dynamic,
                 apiService: ApiService) extends Directive {
  override def templateUrl: String = "./templates/components/left-panel.html"
  override type ControllerType = js.Dynamic
  override type ScopeType = LeftPanel.LeftPanelScope

  val categories = js.Array(
    js.Dynamic.literal(id = "apps", icon = "th-large", titleKey = "common.applications", path = "/old/apps/index/"),
    js.Dynamic.literal(id = "firmwares", icon = "cog", titleKey = "common.firmwares", path = "/old/firmwares/index/")
  )


  override def isolateScope: Dictionary[String] = js.Dictionary[String](
    "onRefresh" -> "&"
  )

  override def postLink(scope: ScopeType, elem: JQLite, attrs: Attributes): Unit = {
    import timers._
    import duration._

    var interval: SetIntervalHandle = null
    val receiver = new EventReceiver {
      override def receive: Receive = {
        case ApiService.UpdateDoneEvent() =>
          scope.lastUpdate = apiService.lastUpdateDate.map {(date) =>
            Moment(date.getTime).fromNow().capitalize
          } getOrElse("Never")
          scope.asInstanceOf[Scope].$digest()
        case ignore =>
      }
    }
    scope.categories = categories
    scope.selected = attrs("selectedCategory")
    scope.navigate = {(path: String) =>
      $location.path(path)
      $route.reload()
    }
    scope.refresh = {() =>
      scope.asInstanceOf[js.Dynamic].onRefresh()
    }
    scope.lastUpdate = apiService.lastUpdateDate.map {(date) =>
      Moment(date.getTime).fromNow().capitalize
    } getOrElse("Never")
    scope.deviceName = SessionService.instance.currentSession.get.device._2.name
    scope.asInstanceOf[Scope].$on("$destroy", {() =>
      apiService.eventEmitter.unregister(receiver)
      if (interval != null)
        clearInterval(interval)
    })
    apiService.eventEmitter.register(receiver)
    interval = setInterval(1.minute) {
      scope.lastUpdate = apiService.lastUpdateDate.map {(date) =>
        Moment(date.getTime).fromNow().capitalize
      } getOrElse("Never")
      scope.asInstanceOf[Scope].$digest()
    }
  }

}

object LeftPanel {

  @ScalaJSDefined
  class LeftPanelScope extends js.Object {
    var selected: UndefOr[String] = _
    var categories: js.Array[js.Object with js.Dynamic] = _
    var navigate: js.Function = _
    var refresh: js.Function = _
    var lastUpdate: String = _
    var deviceName: String = _
  }

  def init(module: RichModule) = module.directiveOf[LeftPanel]("leftPanel")
}