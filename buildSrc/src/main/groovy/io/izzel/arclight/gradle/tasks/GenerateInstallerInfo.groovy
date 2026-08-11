package io.izzel.arclight.gradle.tasks

import groovy.json.JsonOutput
import io.izzel.arclight.gradle.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyArtifact
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets
import java.nio.file.Files

abstract class GenerateInstallerInfo extends DefaultTask {

    private String minecraftVersion, neoforgeVersion, fabricLoaderVersion
    private Configuration configuration, fabricExtra

    @Classpath
    abstract ConfigurableFileCollection getInstallerLibraries()

    @Classpath
    abstract ConfigurableFileCollection getFabricExtraLibraries()

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    abstract RegularFileProperty getNeoforgeInstallerArtifact()

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    abstract RegularFileProperty getFabricLoaderArtifact()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    @Internal
    Configuration getConfiguration() { configuration }

    void setConfiguration(Configuration configuration) {
        this.configuration = configuration
        installerLibraries.from(configuration)
    }

    @Internal
    Configuration getFabricExtra() { fabricExtra }

    void setFabricExtra(Configuration fabricExtra) {
        this.fabricExtra = fabricExtra
        fabricExtraLibraries.from(fabricExtra)
    }

    @Input
    String getMinecraftVersion() {
        return minecraftVersion
    }

    void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion
    }


    @Input
    String getNeoforgeVersion() {
        return neoforgeVersion
    }

    void setNeoforgeVersion(String neoforgeVersion) {
        this.neoforgeVersion = neoforgeVersion
    }

    @Input
    String getFabricLoaderVersion() {
        return fabricLoaderVersion
    }

    void setFabricLoaderVersion(String fabricLoaderVersion) {
        this.fabricLoaderVersion = fabricLoaderVersion
    }

    private static List<String> configurationDeps(Configuration conf) {
        return conf.dependencies.collect { dep ->
            def classifier = null
            if (dep.artifacts) {
                dep.artifacts.each { DependencyArtifact artifact ->
                    if (artifact.classifier) {
                        classifier = artifact.classifier
                    }
                }
            }
            if (classifier) {
                return "${dep.group}:${dep.name}:${dep.version}:$classifier"
            } else {
                return "${dep.group}:${dep.name}:${dep.version}"
            }
        }.sort() as List<String>
    }

    private static SortedMap<String, String> artifactHashes(Configuration configuration, List<String> coordinates) {
        def resolved = configuration.resolvedConfiguration.resolvedArtifacts
        def hashes = new TreeMap<String, String>()
        coordinates.each { coordinate ->
            def notation = coordinate.split('@', 2)
            def parts = notation[0].split(':')
            def extension = notation.length == 2 ? notation[1] : 'jar'
            def classifier = parts.length == 4 ? parts[3] : null
            def matches = resolved.findAll { artifact ->
                artifact.moduleVersion.id.group == parts[0] &&
                    artifact.moduleVersion.id.name == parts[1] &&
                    artifact.moduleVersion.id.version == parts[2] &&
                    artifact.classifier == classifier &&
                    artifact.extension == extension
            }
            if (matches.size() != 1) {
                throw new IllegalStateException("Expected exactly one resolved artifact for ${coordinate}, found ${matches.size()}")
            }
            hashes[coordinate] = Utils.sha1(matches.iterator().next().file)
        }
        return hashes
    }

    @TaskAction
    void run() {
        def output = new LinkedHashMap<String, Object>()
        output.installer = new LinkedHashMap<String, String>([
            minecraft       : minecraftVersion,
            neoforge        : neoforgeVersion,
            neoforgeHash    : Utils.sha1(neoforgeInstallerArtifact.get().asFile),
            fabricLoader    : fabricLoaderVersion,
            fabricLoaderHash: Utils.sha1(fabricLoaderArtifact.get().asFile)
        ])
        output.libraries = artifactHashes(configuration, configurationDeps(configuration))
        output.fabricExtra = artifactHashes(fabricExtra, configurationDeps(fabricExtra))

        def outputPath = outputFile.get().asFile.toPath()
        Files.createDirectories(outputPath.parent)
        Files.writeString(outputPath, JsonOutput.toJson(output), StandardCharsets.UTF_8)
    }
}
