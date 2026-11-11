/*
 * Copyright 2026 LIFS Tools Consortium.
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
package org.lifstools.mztab2.cli;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.lifstools.mztab2.cvmapping.CvParameterLookupService;
import org.lifstools.mztab2.cvmapping.local.Downloader;
import org.lifstools.mztab2.cvmapping.local.InvalidResourcesException;
import org.lifstools.mztab2.cvmapping.local.LookupPolicy;
import org.lifstools.mztab2.cvmapping.local.LookupPolicy.Source;
import org.lifstools.mztab2.cvmapping.local.OlsCurrentReleaseSource;
import org.lifstools.mztab2.cvmapping.local.PreferredResources;
import org.lifstools.mztab2.cvmapping.local.ResolvedVocabulary;
import org.lifstools.mztab2.cvmapping.local.VocabularyCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;

/**
 * The options of the semantic validation's controlled vocabulary lookup, the
 * {@link LookupPolicy} they make, and the report line of each vocabulary used.
 *
 * <ul>
 * <li>{@code --cvSource local|ols}: versioned releases from the local cache
 * (default), or OLS's current release only;</li>
 * <li>{@code --cvCache <folder>}: the cache folder, by default
 * {@link VocabularyCache#defaultRoot()};</li>
 * <li>{@code --cvResources <file>}: a preferred-resources file whose entries
 * replace the shipped default's;</li>
 * <li>{@code --cvOffline}: download nothing;</li>
 * <li>{@code --cvForce}: a vocabulary version that cannot be had is a
 * warning, and a substitute is used.</li>
 * </ul>
 */
final class CvLookupOptions {

    static final String SOURCE = "cvSource";
    static final String CACHE = "cvCache";
    static final String RESOURCES = "cvResources";
    static final String OFFLINE = "cvOffline";
    static final String FORCE = "cvForce";

    /** The time to connect, to get the answer's headers, and to get each further piece of a download. */
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(60);

    private static final Logger LOGGER = LoggerFactory.getLogger(CvLookupOptions.class);

    private CvLookupOptions() {
    }

    /**
     * Adds the long options; none has a short form.
     *
     * @param options the options to add to
     */
    static void addTo(Options options) {
        options.addOption(null, SOURCE, true,
            "Example: --cvSource ols. Where semantic validation looks up terms: 'local' (default) checks them"
            + " against the vocabulary releases the file declares, downloaded once into the cache;"
            + " 'ols' asks OLS, which serves only its current release, so it needs --cvForce.");
        options.addOption(null, CACHE, true,
            "Example: --cvCache /path/to/cv. The folder of the downloaded vocabulary releases, default "
            + "$XDG_CACHE_HOME/jmztabm/cv, else ~/.cache/jmztabm/cv.");
        options.addOption(null, RESOURCES, true,
            "Example: --cvResources my-resources.json. A preferred-resources file (schema"
            + " cv/preferred-resources.schema.json) whose entries replace the default's, per vocabulary.");
        options.addOption(null, OFFLINE, false,
            "Example: --cvOffline. Download nothing: a vocabulary release that is not in the cache cannot be had.");
        options.addOption(null, FORCE, false,
            "Example: --cvForce. A vocabulary release that cannot be had is a warning instead of an error:"
            + " the nearest newer release, then the latest, then OLS is used instead.");
    }

    /**
     * The lookup policy the options ask for.
     *
     * @param line the parsed command line
     * @return the policy
     * @throws IllegalArgumentException if {@code --cvSource} is neither
     * {@code local} nor {@code ols}, or the {@code --cvResources} file is
     * invalid; the message says why
     */
    static LookupPolicy policy(CommandLine line) {
        Source source = source(line.getOptionValue(SOURCE, "local"));
        boolean forced = line.hasOption(FORCE);
        PreferredResources resources = PreferredResources.defaults();
        if (line.hasOption(RESOURCES)) {
            Path file = Path.of(line.getOptionValue(RESOURCES).trim());
            try {
                resources = resources.overriddenBy(PreferredResources.load(file));
            } catch (InvalidResourcesException e) {
                throw new IllegalArgumentException("The resources file given with --" + RESOURCES
                    + " is invalid; semantic validation was not run: " + e.getMessage(), e);
            }
        }
        OlsCurrentReleaseSource ols = new OlsCurrentReleaseSource(
            new OLSClient(CvParameterLookupService.defaultConfig()));
        VocabularyCache cache = null;
        if (source == Source.LOCAL) {
            Path root = line.hasOption(CACHE)
                ? Path.of(line.getOptionValue(CACHE).trim())
                : VocabularyCache.defaultRoot();
            boolean offline = line.hasOption(OFFLINE);
            cache = new VocabularyCache(root, Downloader.http(DOWNLOAD_TIMEOUT), offline, null);
            LOGGER.info("Looking up CV terms in local vocabulary releases ({}, {}), cache folder: {}",
                forced ? "forced" : "strict", offline ? "offline" : "downloads allowed", root.toAbsolutePath());
        } else {
            LOGGER.info("Looking up CV terms in OLS ({}); OLS serves only its current releases",
                forced ? "forced" : "strict");
        }
        return new LookupPolicy(source, forced, cache, resources, ols);
    }

    private static Source source(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "local" -> Source.LOCAL;
            case "ols" -> Source.OLS;
            default -> throw new IllegalArgumentException("Unknown --" + SOURCE + " '" + value
                + "': use 'local' or 'ols'; semantic validation was not run.");
        };
    }

    /**
     * The report line of a vocabulary used:
     * {@code CV <label>: <version used> (<reason>) from <source>}. The source
     * is the URL of the release for a local one, else OLS with the version it
     * reported; a vocabulary that a later run may not answer alike is marked
     * {@code , not re-runnable}.
     *
     * @param v the vocabulary
     * @return the line
     */
    static String reportLine(ResolvedVocabulary v) {
        String from = v.release().map(r -> r.source().toString())
            .orElseGet(() -> "OLS" + v.olsVersion().map(o -> " (version " + o + ")").orElse(""));
        return "CV " + v.label() + ": " + v.versionUsed() + " (" + v.reason() + ") from " + from
            + (v.reRunnable() ? "" : ", not re-runnable");
    }
}
