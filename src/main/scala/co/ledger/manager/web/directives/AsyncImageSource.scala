package co.ledger.manager.web.directives

import biz.enef.angulate.{Directive, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import org.scalajs.dom.raw.XMLHttpRequest

import scala.scalajs.js
import scala.scalajs.js.Dictionary

/**
  *
  * AsyncImageSource
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 07/10/2016.
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
class AsyncImageSource extends Directive {
  override val restrict: String = "A"
  override type ScopeType = Scope
  override type ControllerType = js.Dynamic


  override def isolateScope: Dictionary[String] = js.Dictionary(
    "asyncSrc" -> "&asyncSrc"
  )

  override def controller(ctrl: ControllerType, scope: ScopeType, elem: JQLite, attrs: Attributes): Unit = {
    setImageSource(elem, scope.asInstanceOf[js.Dynamic].asyncSrc().asInstanceOf[js.Array[String]])
  }

  private def setImageSource(elem: JQLite, data: js.Array[String]): Unit = {
    val source = data(0)
    val default = data.lift(1)

    default foreach {
      elem(0).asInstanceOf[js.Dynamic].src = _
    }

    def applyBlob(blob: js.Any): Unit = {
      elem(0).asInstanceOf[js.Dynamic].src = js.Dynamic.global.URL.createObjectURL(blob)
    }

    if (AsyncImageSource.Cache.dict.lift(source).isEmpty) {
      val xhr = new XMLHttpRequest()
      xhr.open("GET", source, true)
      xhr.responseType = "blob"
      xhr.onload = {(e: js.Any) =>
        if(xhr.status == 200) {
          AsyncImageSource.Cache(source) = xhr.response
          applyBlob(xhr.response)
        }
      }
      xhr.send()
    } else {
      applyBlob(AsyncImageSource.Cache(source))
    }

  }

}

object AsyncImageSource {
  val Cache = js.Dictionary[js.Any]()

  def init(module: RichModule) = module.directiveOf[AsyncImageSource]("asyncSrc")
}