/*
 * Copyright 2017 Leibniz Institut für Analytische Wissenschaften - ISAS e.V..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.isas.lipidomics.mztab.validator.webapp.controller;

import de.isas.lipidomics.mztab.validator.webapp.service.StorageService;
import de.isas.lipidomics.mztab.validator.webapp.service.ValidationService;
import de.isas.lipidomics.mztab.validator.webapp.service.storage.StorageFileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.ws.rs.QueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@Controller
public class ValidationController {

    private final StorageService storageService;
    private final ValidationService validationService;
    private int maxErrors = 100;

    @Autowired
    public ValidationController(StorageService storageService, ValidationService validationService) {
        this.storageService = storageService;
        this.validationService = validationService;
    }

    @GetMapping("/")
    public ModelAndView listUploadedFiles() throws IOException {
        ModelAndView model = new ModelAndView("index");
//        model.addObject("files", storageService.loadAll().map(
//                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                        "validateFile", path.getFileName().toString()).build().toString())
//                .collect(Collectors.toList()));
        model.addObject("allVersions", ValidationService.MzTabVersion.values());
        model.addObject("version", ValidationService.MzTabVersion.MZTAB_1_1);
        model.addObject("maxErrors", Integer.valueOf(maxErrors));
        return model;
    }

//    @GetMapping(value = "/files/{filename:.+}", produces = {"text/plain"})
//    @ResponseBody
//    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//        Resource file = storageService.loadAsResource(filename);
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
//                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
//    }

    @PostMapping("/")
    public ModelAndView handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("version") String version, @RequestParam("maxErrors") int maxErrors, RedirectAttributes redirectAttributes) {
        String filename = storageService.store(file);
        ModelAndView modelAndView = new ModelAndView(
            "redirect:/validate/"+filename+
            "?version="+(version==null?ValidationService.MzTabVersion.MZTAB_1_1:ValidationService.MzTabVersion.valueOf(version))+
            "&maxErrors="+(maxErrors>0?maxErrors:this.maxErrors));
//        redirectAttributes.addFlashAttribute("message",
//                "You successfully uploaded " + file.getOriginalFilename() + "!");
        return modelAndView;
    }

    @GetMapping(value="/validate/{filename:.+}")
    public ModelAndView validateFile(@PathVariable String filename, @QueryParam("version") ValidationService.MzTabVersion version, @QueryParam("maxErrors") int maxErrors) {
        ModelAndView modelAndView = new ModelAndView("validationResult");
        modelAndView.addObject("validationFile", filename);
        if(version!=null) {
            modelAndView.addObject("validationVersion", version);
        } else {
            modelAndView.addObject("validationVersion", ValidationService.MzTabVersion.MZTAB_1_1);
        }
        if(maxErrors>0) {
            modelAndView.addObject("validationMaxErrors", maxErrors);
        } else {
            modelAndView.addObject("validationMaxErrors", 100);
        }
        modelAndView.addObject("validationResults", validationService.validate(ValidationService.MzTabVersion.MZTAB_1_1, filename, maxErrors));
        return modelAndView;
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
