package io.papermc.paper.configuration;

/**
 * Base class for Paper configuration parts.
 * This is a simplified implementation for Luminara compatibility.
 */
abstract class ConfigurationPart {

    public static abstract class Post extends ConfigurationPart {

        public abstract void postProcess();
    }

}
