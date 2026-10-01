package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Compile-only stand-in for the Forge mod annotation.
 * <p>
 * The fixture mod is compiled against the running server's Forge, so it must not pull a
 * Forge artifact into the build. This stub is never packaged: the fixture source set only
 * exposes the smoke mod's own compiled classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {

    String value();
}
