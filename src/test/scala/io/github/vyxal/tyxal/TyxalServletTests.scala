package io.github.vyxal.tyxal

import org.scalatra.test.scalatest._

class TyxalServletTests extends ScalatraFunSuite {

  addServlet(classOf[TyxalServlet], "/*")

  test("GET / on TyxalServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
