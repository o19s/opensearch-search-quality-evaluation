package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the {@code libs} extension.
 */
@NonNullApi
public class LibrariesForLibsInPluginsBlock extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final OrgLibraryAccessors laccForOrgLibraryAccessors = new OrgLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibsInPluginsBlock(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

    /**
     * Group of libraries at <b>org</b>
     *
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public OrgLibraryAccessors getOrg() {
        org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
        return laccForOrgLibraryAccessors;
    }

    /**
     * Group of versions at <b>versions</b>
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Group of bundles at <b>bundles</b>
     *
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public BundleAccessors getBundles() {
        org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
        return baccForBundleAccessors;
    }

    /**
     * Group of plugins at <b>plugins</b>
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheLibraryAccessors laccForOrgApacheLibraryAccessors = new OrgApacheLibraryAccessors(owner);
        private final OrgOpensearchLibraryAccessors laccForOrgOpensearchLibraryAccessors = new OrgOpensearchLibraryAccessors(owner);

        public OrgLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgApacheLibraryAccessors getApache() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgApacheLibraryAccessors;
        }

        /**
         * Group of libraries at <b>org.opensearch</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgOpensearchLibraryAccessors getOpensearch() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgOpensearchLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgApacheLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheCommonsLibraryAccessors laccForOrgApacheCommonsLibraryAccessors = new OrgApacheCommonsLibraryAccessors(owner);
        private final OrgApacheLoggingLibraryAccessors laccForOrgApacheLoggingLibraryAccessors = new OrgApacheLoggingLibraryAccessors(owner);

        public OrgApacheLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.commons</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgApacheCommonsLibraryAccessors getCommons() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgApacheCommonsLibraryAccessors;
        }

        /**
         * Group of libraries at <b>org.apache.logging</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgApacheLoggingLibraryAccessors getLogging() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgApacheLoggingLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgApacheCommonsLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheCommonsCommonsLibraryAccessors laccForOrgApacheCommonsCommonsLibraryAccessors = new OrgApacheCommonsCommonsLibraryAccessors(owner);

        public OrgApacheCommonsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.commons.commons</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgApacheCommonsCommonsLibraryAccessors getCommons() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgApacheCommonsCommonsLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgApacheCommonsCommonsLibraryAccessors extends SubDependencyFactory {

        public OrgApacheCommonsCommonsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>lang3</b> with <b>org.apache.commons:commons-lang3</b> coordinates and
         * with version reference <b>org.apache.commons.commons.lang3</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public Provider<MinimalExternalModuleDependency> getLang3() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return create("org.apache.commons.commons.lang3");
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgApacheLoggingLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheLoggingLog4jLibraryAccessors laccForOrgApacheLoggingLog4jLibraryAccessors = new OrgApacheLoggingLog4jLibraryAccessors(owner);

        public OrgApacheLoggingLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.logging.log4j</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgApacheLoggingLog4jLibraryAccessors getLog4j() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgApacheLoggingLog4jLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgApacheLoggingLog4jLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheLoggingLog4jLog4jLibraryAccessors laccForOrgApacheLoggingLog4jLog4jLibraryAccessors = new OrgApacheLoggingLog4jLog4jLibraryAccessors(owner);

        public OrgApacheLoggingLog4jLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.logging.log4j.log4j</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgApacheLoggingLog4jLog4jLibraryAccessors getLog4j() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgApacheLoggingLog4jLog4jLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgApacheLoggingLog4jLog4jLibraryAccessors extends SubDependencyFactory {

        public OrgApacheLoggingLog4jLog4jLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>core</b> with <b>org.apache.logging.log4j:log4j-core</b> coordinates and
         * with version reference <b>org.apache.logging.log4j.log4j.core</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public Provider<MinimalExternalModuleDependency> getCore() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return create("org.apache.logging.log4j.log4j.core");
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgOpensearchLibraryAccessors extends SubDependencyFactory {
        private final OrgOpensearchClientLibraryAccessors laccForOrgOpensearchClientLibraryAccessors = new OrgOpensearchClientLibraryAccessors(owner);

        public OrgOpensearchLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.opensearch.client</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgOpensearchClientLibraryAccessors getClient() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgOpensearchClientLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgOpensearchClientLibraryAccessors extends SubDependencyFactory {
        private final OrgOpensearchClientOpensearchLibraryAccessors laccForOrgOpensearchClientOpensearchLibraryAccessors = new OrgOpensearchClientOpensearchLibraryAccessors(owner);

        public OrgOpensearchClientLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.opensearch.client.opensearch</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgOpensearchClientOpensearchLibraryAccessors getOpensearch() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgOpensearchClientOpensearchLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgOpensearchClientOpensearchLibraryAccessors extends SubDependencyFactory {
        private final OrgOpensearchClientOpensearchRestLibraryAccessors laccForOrgOpensearchClientOpensearchRestLibraryAccessors = new OrgOpensearchClientOpensearchRestLibraryAccessors(owner);

        public OrgOpensearchClientOpensearchLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.opensearch.client.opensearch.rest</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgOpensearchClientOpensearchRestLibraryAccessors getRest() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgOpensearchClientOpensearchRestLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgOpensearchClientOpensearchRestLibraryAccessors extends SubDependencyFactory {
        private final OrgOpensearchClientOpensearchRestHighLibraryAccessors laccForOrgOpensearchClientOpensearchRestHighLibraryAccessors = new OrgOpensearchClientOpensearchRestHighLibraryAccessors(owner);

        public OrgOpensearchClientOpensearchRestLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.opensearch.client.opensearch.rest.high</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgOpensearchClientOpensearchRestHighLibraryAccessors getHigh() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgOpensearchClientOpensearchRestHighLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgOpensearchClientOpensearchRestHighLibraryAccessors extends SubDependencyFactory {
        private final OrgOpensearchClientOpensearchRestHighLevelLibraryAccessors laccForOrgOpensearchClientOpensearchRestHighLevelLibraryAccessors = new OrgOpensearchClientOpensearchRestHighLevelLibraryAccessors(owner);

        public OrgOpensearchClientOpensearchRestHighLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.opensearch.client.opensearch.rest.high.level</b>
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public OrgOpensearchClientOpensearchRestHighLevelLibraryAccessors getLevel() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return laccForOrgOpensearchClientOpensearchRestHighLevelLibraryAccessors;
        }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class OrgOpensearchClientOpensearchRestHighLevelLibraryAccessors extends SubDependencyFactory {

        public OrgOpensearchClientOpensearchRestHighLevelLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>client</b> with <b>org.opensearch.client:opensearch-rest-high-level-client</b> coordinates and
         * with version reference <b>org.opensearch.client.opensearch.rest.high.level.client</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         *
         * @deprecated Will be removed in Gradle 9.0.
         */
        @Deprecated
        public Provider<MinimalExternalModuleDependency> getClient() {
            org.gradle.internal.deprecation.DeprecationLogger.deprecateBehaviour("Accessing libraries or bundles from version catalogs in the plugins block.").withAdvice("Only use versions or plugins from catalogs in the plugins block.").willBeRemovedInGradle9().withUpgradeGuideSection(8, "kotlin_dsl_deprecated_catalogs_plugins_block").nagUser();
            return create("org.opensearch.client.opensearch.rest.high.level.client");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        private final OrgVersionAccessors vaccForOrgVersionAccessors = new OrgVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org</b>
         */
        public OrgVersionAccessors getOrg() {
            return vaccForOrgVersionAccessors;
        }

    }

    public static class OrgVersionAccessors extends VersionFactory  {

        private final OrgApacheVersionAccessors vaccForOrgApacheVersionAccessors = new OrgApacheVersionAccessors(providers, config);
        private final OrgOpensearchVersionAccessors vaccForOrgOpensearchVersionAccessors = new OrgOpensearchVersionAccessors(providers, config);
        public OrgVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache</b>
         */
        public OrgApacheVersionAccessors getApache() {
            return vaccForOrgApacheVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.org.opensearch</b>
         */
        public OrgOpensearchVersionAccessors getOpensearch() {
            return vaccForOrgOpensearchVersionAccessors;
        }

    }

    public static class OrgApacheVersionAccessors extends VersionFactory  {

        private final OrgApacheCommonsVersionAccessors vaccForOrgApacheCommonsVersionAccessors = new OrgApacheCommonsVersionAccessors(providers, config);
        private final OrgApacheLoggingVersionAccessors vaccForOrgApacheLoggingVersionAccessors = new OrgApacheLoggingVersionAccessors(providers, config);
        public OrgApacheVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.commons</b>
         */
        public OrgApacheCommonsVersionAccessors getCommons() {
            return vaccForOrgApacheCommonsVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.org.apache.logging</b>
         */
        public OrgApacheLoggingVersionAccessors getLogging() {
            return vaccForOrgApacheLoggingVersionAccessors;
        }

    }

    public static class OrgApacheCommonsVersionAccessors extends VersionFactory  {

        private final OrgApacheCommonsCommonsVersionAccessors vaccForOrgApacheCommonsCommonsVersionAccessors = new OrgApacheCommonsCommonsVersionAccessors(providers, config);
        public OrgApacheCommonsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.commons.commons</b>
         */
        public OrgApacheCommonsCommonsVersionAccessors getCommons() {
            return vaccForOrgApacheCommonsCommonsVersionAccessors;
        }

    }

    public static class OrgApacheCommonsCommonsVersionAccessors extends VersionFactory  {

        public OrgApacheCommonsCommonsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.apache.commons.commons.lang3</b> with value <b>3.17.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getLang3() { return getVersion("org.apache.commons.commons.lang3"); }

    }

    public static class OrgApacheLoggingVersionAccessors extends VersionFactory  {

        private final OrgApacheLoggingLog4jVersionAccessors vaccForOrgApacheLoggingLog4jVersionAccessors = new OrgApacheLoggingLog4jVersionAccessors(providers, config);
        public OrgApacheLoggingVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.logging.log4j</b>
         */
        public OrgApacheLoggingLog4jVersionAccessors getLog4j() {
            return vaccForOrgApacheLoggingLog4jVersionAccessors;
        }

    }

    public static class OrgApacheLoggingLog4jVersionAccessors extends VersionFactory  {

        private final OrgApacheLoggingLog4jLog4jVersionAccessors vaccForOrgApacheLoggingLog4jLog4jVersionAccessors = new OrgApacheLoggingLog4jLog4jVersionAccessors(providers, config);
        public OrgApacheLoggingLog4jVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.logging.log4j.log4j</b>
         */
        public OrgApacheLoggingLog4jLog4jVersionAccessors getLog4j() {
            return vaccForOrgApacheLoggingLog4jLog4jVersionAccessors;
        }

    }

    public static class OrgApacheLoggingLog4jLog4jVersionAccessors extends VersionFactory  {

        public OrgApacheLoggingLog4jLog4jVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.apache.logging.log4j.log4j.core</b> with value <b>2.23.1</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getCore() { return getVersion("org.apache.logging.log4j.log4j.core"); }

    }

    public static class OrgOpensearchVersionAccessors extends VersionFactory  {

        private final OrgOpensearchClientVersionAccessors vaccForOrgOpensearchClientVersionAccessors = new OrgOpensearchClientVersionAccessors(providers, config);
        public OrgOpensearchVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.opensearch.client</b>
         */
        public OrgOpensearchClientVersionAccessors getClient() {
            return vaccForOrgOpensearchClientVersionAccessors;
        }

    }

    public static class OrgOpensearchClientVersionAccessors extends VersionFactory  {

        private final OrgOpensearchClientOpensearchVersionAccessors vaccForOrgOpensearchClientOpensearchVersionAccessors = new OrgOpensearchClientOpensearchVersionAccessors(providers, config);
        public OrgOpensearchClientVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.opensearch.client.opensearch</b>
         */
        public OrgOpensearchClientOpensearchVersionAccessors getOpensearch() {
            return vaccForOrgOpensearchClientOpensearchVersionAccessors;
        }

    }

    public static class OrgOpensearchClientOpensearchVersionAccessors extends VersionFactory  {

        private final OrgOpensearchClientOpensearchRestVersionAccessors vaccForOrgOpensearchClientOpensearchRestVersionAccessors = new OrgOpensearchClientOpensearchRestVersionAccessors(providers, config);
        public OrgOpensearchClientOpensearchVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.opensearch.client.opensearch.rest</b>
         */
        public OrgOpensearchClientOpensearchRestVersionAccessors getRest() {
            return vaccForOrgOpensearchClientOpensearchRestVersionAccessors;
        }

    }

    public static class OrgOpensearchClientOpensearchRestVersionAccessors extends VersionFactory  {

        private final OrgOpensearchClientOpensearchRestHighVersionAccessors vaccForOrgOpensearchClientOpensearchRestHighVersionAccessors = new OrgOpensearchClientOpensearchRestHighVersionAccessors(providers, config);
        public OrgOpensearchClientOpensearchRestVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.opensearch.client.opensearch.rest.high</b>
         */
        public OrgOpensearchClientOpensearchRestHighVersionAccessors getHigh() {
            return vaccForOrgOpensearchClientOpensearchRestHighVersionAccessors;
        }

    }

    public static class OrgOpensearchClientOpensearchRestHighVersionAccessors extends VersionFactory  {

        private final OrgOpensearchClientOpensearchRestHighLevelVersionAccessors vaccForOrgOpensearchClientOpensearchRestHighLevelVersionAccessors = new OrgOpensearchClientOpensearchRestHighLevelVersionAccessors(providers, config);
        public OrgOpensearchClientOpensearchRestHighVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.opensearch.client.opensearch.rest.high.level</b>
         */
        public OrgOpensearchClientOpensearchRestHighLevelVersionAccessors getLevel() {
            return vaccForOrgOpensearchClientOpensearchRestHighLevelVersionAccessors;
        }

    }

    public static class OrgOpensearchClientOpensearchRestHighLevelVersionAccessors extends VersionFactory  {

        public OrgOpensearchClientOpensearchRestHighLevelVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.opensearch.client.opensearch.rest.high.level.client</b> with value <b>2.16.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getClient() { return getVersion("org.opensearch.client.opensearch.rest.high.level.client"); }

    }

    /**
     * @deprecated Will be removed in Gradle 9.0.
     */
    @Deprecated
    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

    }

}
