package org.hesperides.core.application.platforms;

import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.commands.PlatformCommands;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.exceptions.ApplicationNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.DuplicatePlatformException;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
public class PlatformUseCases {

    private final PlatformCommands commands;
    private final PlatformQueries queries;

    @Autowired
    public PlatformUseCases(PlatformCommands commands, PlatformQueries queries) {
        this.commands = commands;
        this.queries = queries;
    }

    public Platform.Key createPlatform(Platform platform, User user) {
        if (queries.platformExists(platform.getKey())) {
            throw new DuplicatePlatformException(platform.getKey());
        }
        return commands.createPlatform(platform, user);
    }

    public PlatformView getPlatform(Platform.Key platformKey) {
        return queries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
    }

    public void updatePlatform(Platform.Key platformKey, Platform platform, boolean copyProperties, User user) {
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platform.getKey());
        }
        commands.updatePlatform(platformKey, platform, copyProperties, user);
    }

    public void deletePlatform(Platform.Key platformKey, User user) {
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        commands.deletePlatform(platformKey, user);
    }

    public ApplicationView getApplication(String applicationName) {
        return queries.getApplication(applicationName)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationName));
    }

    public List<ModulePlatformView> getPlatformUsingModule(Module.Key moduleKey) {
        return queries.getPlatformsUsingModule(moduleKey);
    }

    public List<SearchPlatformResultView> searchPlatforms(String applicationName, String platformName) {
        return queries.searchPlatforms(applicationName, platformName);
    }

    public List<SearchApplicationResultView> searchApplications(String applicationName) {
        return queries.searchApplications(applicationName);
    }

    public Optional<InstanceView> getInstanceModel(Platform.Key platformKey, String modulePath) {
        // Récupération de la plateforme correspondante
        PlatformView platform =  queries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));

        // Comme on a récupéré toutes les informations de la platforme, on peut se permettre d'utiliser cette réponse pour faire un filtre sur les DeployedModules avec le modulePath
        return platform.getDeployedModules().stream()
                .filter(deployedModuleView -> deployedModuleView.getPropertiesPath().equals(modulePath)).findFirst()
                // On récupère la première instance car cela n'a pas d'importance, elles ont toutes le même nombre de propriétés
                .flatMap(deployedModuleView -> deployedModuleView.getInstances().stream().findFirst());
    }
}
