# mzTab-M reference implementation and validation API.
# 
# This is the mzTab-M reference implementation and validation API service.
# 
# OpenAPI spec version: 2.0.0
# Contact: nils.hoffmann@isas.de
# Generated by: https://github.com/swagger-api/swagger-codegen.git


#' ColumnParameterMapping Class
#'
#' Defines the used unit for a column in the mzTab-M file. The format of the value has to be \\{column name}&#x3D;\\{Parameter defining the unit}. This field MUST NOT be used to define a unit for quantification columns. The unit used for small molecule quantification values MUST be set in small_molecule-quantification_unit.
#'
#' @field column_name 
#' @field param 
#'
#' mzTab-M specification example(s)
#' \preformatted{
#' COM	colunit for optional small molecule summary column with the name 'opt_global_cv_MS:MS:1002954_collisional_cross_sectional_area'
MTD	colunit-small_molecule	opt_global_cv_MS:MS:1002954_collisional_cross_sectional_area=[UO,UO:00003241, square angstrom,]

#' }
#' 
#'
#' @importFrom R6 R6Class
#' @importFrom jsonlite fromJSON toJSON
#' @export
ColumnParameterMapping <- R6::R6Class(
  'ColumnParameterMapping',
  public = list(
    `column_name` = NULL,
    `param` = NULL,
    initialize = function(`column_name`, `param`){
      if (!missing(`column_name`)) {
        stopifnot(is.character(`column_name`), length(`column_name`) == 1)
        self$`column_name` <- `column_name`
      }
      if (!missing(`param`)) {
        stopifnot(R6::is.R6(`param`))
        self$`param` <- `param`
      }
    },
    toJSON = function() {
      ColumnParameterMappingObject <- list()
      if (!is.null(self$`column_name`)) {
        ColumnParameterMappingObject[['column_name']] <- self$`column_name`
      }
      if (!is.null(self$`param`)) {
        ColumnParameterMappingObject[['param']] <- self$`param`$toJSON()
      }

      ColumnParameterMappingObject
    },
    fromJSON = function(ColumnParameterMappingJson) {
      ColumnParameterMappingObject <- jsonlite::fromJSON(ColumnParameterMappingJson, simplifyVector = FALSE)
      if (!is.null(ColumnParameterMappingObject$`column_name`)) {
        self$`column_name` <- ColumnParameterMappingObject$`column_name`
      }
      if (!is.null(ColumnParameterMappingObject$`param`)) {
        `paramObject` <- Parameter$new()
        `paramObject`$fromJSON(jsonlite::toJSON(ColumnParameterMappingObject$param, auto_unbox = TRUE))
        self$`param` <- `paramObject`
      }
    },
    toJSONString = function() {
       sprintf(
        '{
           "column_name": %s,
           "param": %s
        }',
        self$`column_name`,
        self$`param`$toJSON()
      )
    },
    fromJSONString = function(ColumnParameterMappingJson) {
      ColumnParameterMappingObject <- jsonlite::fromJSON(ColumnParameterMappingJson, simplifyVector = FALSE)
      self$`column_name` <- ColumnParameterMappingObject$`column_name`
      ParameterObject <- Parameter$new()
      self$`param` <- ParameterObject$fromJSON(jsonlite::toJSON(ColumnParameterMappingObject$param, auto_unbox = TRUE))
    }
  )
)
