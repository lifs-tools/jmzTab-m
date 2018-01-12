context("test-read_mztab_json.R")

test_that("reading of mztab json works", {
  testfile <- system.file("testdata", c("mztabTest.json"),package="rmztab")
  mzTabObject <- MzTab$new()
  mzTabObject$fromJSON(testfile)
  print(mzTabObject$toJSONString())
  expect_false(is.null(mzTabObject$metadata))
  expect_equal(object = mzTabObject$`metadata`$`mzTab-ID`, "ISAS-2017-12-01-LP-9891")
  expect_length(mzTabObject$`smallMoleculeSummary`,0)
  expect_length(mzTabObject$`smallMoleculeEvidence`,0)
  expect_length(mzTabObject$`smallMoleculeFeature`,0)
})

