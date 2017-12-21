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
public class FileUploadController {

    private final StorageService storageService;
    private final ValidationService validationService;

    @Autowired
    public FileUploadController(StorageService storageService, ValidationService validationService) {
        this.storageService = storageService;
        this.validationService = validationService;
    }

    @GetMapping("/")
    public ModelAndView listUploadedFiles() throws IOException {
        ModelAndView model = new ModelAndView("uploadForm");
        model.addObject("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "validateFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return model;
    }

    @GetMapping(value = "/files/{filename:.+}", produces = {"text/plain"})
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public ModelAndView handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        String filename = storageService.store(file);
        ModelAndView modelAndView = new ModelAndView("redirect:/");
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");
        return modelAndView;
    }

    @GetMapping(value="/validate/{filename:.+}")
    public ModelAndView validateFile(@PathVariable String filename) {
        ModelAndView modelAndView = new ModelAndView("validationResult");
        modelAndView.addObject("validationFile", filename);
        modelAndView.addObject("validationResults", validationService.validate(ValidationService.MzTabVersion.MZTAB_1_1, filename));
        return modelAndView;
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
