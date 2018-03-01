package co.ledger.manager.web.directives

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Directive, Scope}
import biz.enef.angulate.core.{Attributes, JQLite}
import co.ledger.manager.web.core.remarkable.Remarkable

import scala.scalajs.js
import scala.scalajs.js.Dictionary
import org.scalajs.jquery.JQuery

/**
  *
  * MarkDown
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 12/10/2016.
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
class MarkDown($compile: js.Dynamic) extends Directive {
  override type ScopeType = Scope

  override def isolateScope: Dictionary[String] = js.Dictionary(
    "markDown" -> "&"
  )

  override def postLink(scope: ScopeType, element: JQLite, attrs: Attributes): Unit = {
    def apply(content: String): Unit = {
      element.html(remarkable.render(content))
      element.find("a").asInstanceOf[JQuery].click({ e: js.Dynamic =>
        e.preventDefault()
        js.Dynamic.global.open(e.target.href)
      })
      $compile(element.contents())(scope)
    }

    if (!js.isUndefined(attrs("markDown"))) {
      apply(scope.asInstanceOf[js.Dynamic].markDown().asInstanceOf[String])
    }
    scope.$watch(attrs("markDown"), {() =>
      apply(scope.asInstanceOf[js.Dynamic].markDown().asInstanceOf[String])
    })
  }

  val remarkable = new Remarkable()
}

object MarkDown {
  def init(module: RichModule) = module.directiveOf[MarkDown]("markDown")
}