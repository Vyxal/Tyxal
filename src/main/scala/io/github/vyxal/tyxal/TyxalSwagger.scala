package io.github.vyxal.tyxal

import org.scalatra.ScalatraServlet
import org.scalatra.swagger.{ApiInfo, ContactInfo, LicenseInfo, NativeSwaggerBase, Swagger}

class ResourcesApp(using val swagger: Swagger) extends ScalatraServlet with NativeSwaggerBase

object TyxalApiInfo extends ApiInfo(
    "Tyxal",
    "The Vyxal Tooling API",
    "http://vyxal.github.io",
    ContactInfo(
      "Ginger",
      "http://github.com/GingerIndustries",
      ""
    ),
    LicenseInfo(
      "MIT",
      "http://opensource.org/licenses/MIT"
    )
)

class TyxalSwagger extends Swagger(Swagger.SpecVersion, "1.0.0", TyxalApiInfo)