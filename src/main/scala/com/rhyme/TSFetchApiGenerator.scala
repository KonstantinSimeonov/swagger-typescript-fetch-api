package com.rhyme.codegen

import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.CodegenConstants.MODEL_PROPERTY_NAMING_TYPE;
import io.swagger.codegen.languages.AbstractTypeScriptClientCodegen;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

class TSFetchApiGenerator extends AbstractTypeScriptClientCodegen {
  this.importMapping.clear();

  this.outputFolder = "generated-code/ts-fetch-api"
  this.templateDir = "typescript-fetch-api"
  this.embeddedTemplateDir = templateDir

  def getHelp = "Generates TS bindings by a swagger definition"
  def getName = "ts-fetch-api"

  override def getModelPropertyNaming = "original"

  override def processOpts(): Unit = {
    super.processOpts();
    this.supportingFiles.add(new SupportingFile("api.mustache", "", "api.ts"))
  }

  override def getTypeDeclaration(p: Property): String =
    p match {
      case ap: ArrayProperty => s"${this.getSwaggerType(ap)}<${this.getTypeDeclaration(ap.getItems())}>"
      case mp: MapProperty   => s"{ [key: string]: ${this.getTypeDeclaration(mp.getAdditionalProperties())}; }"
      case _: FileProperty | _: ObjectProperty => "any"
      case _ => super.getTypeDeclaration(p)
    }

  override def postProcessParameter(p: CodegenParameter): Unit = {
    super.postProcessParameter(p)
    p.dataType = this.addModelPrefix(p.dataType)
  }

  override def getSwaggerType(p: Property): String = {
    val st = super.getSwaggerType(p)
    if (this.languageSpecificPrimitives.contains(st))
      st
    else
      this.addModelPrefix(st)
  }

  override def postProcessModels(objs: Map[String, Object]): Map[String, Object] = {
    val models = this.postProcessModelsEnum(objs)
      .get("models")
      .asInstanceOf[List[Map[String, CodegenModel]]]

    models.forEach { mo =>
        val cm = mo.get("model")
        cm.imports = new TreeSet(cm.imports)
        cm.vars.forEach { v =>
          if (v.isEnum) {
            val name = s"${cm.classname}${v.enumName}"
            v.datatypeWithEnum = v.datatypeWithEnum.replace(v.enumName, name)
            v.enumName = name
          }
        }
    }

    objs
  }

  override protected def addAdditionPropertiesToCodeGenModel(cm: CodegenModel, sm: ModelImpl): Unit = {
    cm.additionalPropertiesType = this.getSwaggerType(sm.getAdditionalProperties())
    this.addImport(cm, cm.additionalPropertiesType)
  }

  private def addModelPrefix(swaggerType: String): String =
    if (this.typeMapping.containsKey(swaggerType))
      typeMapping.get(swaggerType)
    else
      swaggerType
}
