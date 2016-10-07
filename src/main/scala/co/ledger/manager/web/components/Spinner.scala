package co.ledger.manager.web.components

import java.util.Date

import biz.enef.angulate.Directive
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import org.scalajs.dom

import scala.scalajs.js

/**
  *
  * Spinner
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 12/05/2016.
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
class Spinner extends Directive {
  import js.Dynamic.{ global => g, newInstance => jsnew }
  import js.timers._

  override def template: String =
    """
      |<div style="width: 50px; height: 50px">
      |  <canvas id="canvas" width="50" height="50"></canvas>
      |</div>
    """.stripMargin


  override def postLink(scope: ScopeType, element: JQLite, attrs: Attributes): Unit = {
    if (_interval == null) {
      _canvas =  element.find("#canvas").asInstanceOf[JQLite](0).asInstanceOf[dom.html.Canvas]
      _ctx = _canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
      _startTime = new Date().getTime
      _interval = js.Dynamic.global.requestAnimationFrame({() =>
        draw(_ctx)
      })
    }
  }

  private def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
    val width = _canvas.width
    val height = _canvas.height
    val lineWidth = 3
    val radius = Math.min(width, height) / 2 - lineWidth
    val x = width / 2
    val y = height / 2

    val t = new Date().getTime - _startTime

    //js.Dynamic.global.console.log(width, height, radius, x, y)

    ctx.clearRect(0, 0, width, height)
    ctx.save()

    // Move registration point to the center of the canvas
    ctx.translate(x, y)

    // Rotate 1 degree
    val e = (t % _revolutionTime).toDouble / _revolutionTime.toDouble
    def interpolation(e: Double) = Math.sin(Math.PI/2 * e) + 1
    def easing(): Double = {
      if (e >= 0.5d) {
        (-interpolation(-2 * e + 2) + 2) / 2
      } else {
        interpolation(2 * e) / 2
      }
    }
    ctx.rotate(((easing() * 360) * Math.PI) / 180)

    // Move registration point back to the top left corner of canvas
    ctx.translate(-x, -y)

    ctx.beginPath()
    ctx.arc(x, y, radius, 0, 2 * Math.PI, false)
    ctx.lineWidth = lineWidth
    ctx.strokeStyle = "#CCCCCC"
    ctx.stroke()

    // Cut the circle
    ctx.beginPath()
    ctx.globalAlpha = 1
    ctx.globalCompositeOperation = "destination-out"
    // Cut horizontal rect
    ctx.rect(width / 2, height / 2,  width / 2, height / 2)
    ctx.fill()
    ctx.restore()

    js.Dynamic.global.requestAnimationFrame({() =>
      draw(_ctx)
    })
  }

  private var _startTime = 0L
  private var _canvas: dom.html.Canvas = null
  private var _ctx: dom.CanvasRenderingContext2D = null
  private var _interval: js.Any = null
  private var _revolutionTime = 1000
}

object Spinner {

  def init(module: RichModule) = module.directiveOf[Spinner]("spinner")

}