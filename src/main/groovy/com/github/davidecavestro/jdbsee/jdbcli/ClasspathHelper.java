package com.github.davidecavestro.jdbsee.jdbcli;

import org.reflections.Reflections;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Extracted from org.reflections.util.ClasspathHelper
 * see https://github.com/ronmamo/reflections
 */
public class ClasspathHelper {

  /**
   * Returns a distinct collection of URLs based on URLs derived from class loaders.
   * <p>
   * This finds the URLs using {@link URLClassLoader#getURLs()} using the specified
   * class loader, searching up the parent hierarchy.
   * <p>
   * If the optional {@link ClassLoader}s are not specified, then both {@link #contextClassLoader()}
   * and {@link #staticClassLoader()} are used for {@link ClassLoader#getResources(String)}.
   * <p>
   * The returned URLs retains the order of the given {@code classLoaders}.
   *
   * @return the collection of URLs, not null
   */
  public static Collection<URL> forClassLoader(ClassLoader... classLoaders) {
    final Collection<URL> result = new ArrayList<URL> ();
    final ClassLoader[] loaders = classLoaders(classLoaders);
    for (ClassLoader classLoader : loaders) {
      while (classLoader != null) {
        if (classLoader instanceof URLClassLoader) {
          URL[] urls = ((URLClassLoader) classLoader).getURLs();
          if (urls != null) {
            result.addAll(Arrays.asList(urls));
          }
        }
        classLoader = classLoader.getParent();
      }
    }
    return distinctUrls(result);
  }

  /**
   * Returns an array of class Loaders initialized from the specified array.
   * <p>
   * If the input is null or empty, it defaults to both {@link #contextClassLoader()} and {@link #staticClassLoader()}
   *
   * @return the array of class loaders, not null
   */
  public static ClassLoader[] classLoaders(ClassLoader... classLoaders) {
    if (classLoaders != null && classLoaders.length != 0) {
      return classLoaders;
    } else {
      ClassLoader contextClassLoader = contextClassLoader(), staticClassLoader = staticClassLoader();
      return contextClassLoader != null ?
          staticClassLoader != null && contextClassLoader != staticClassLoader ?
              new ClassLoader[]{contextClassLoader, staticClassLoader} :
              new ClassLoader[]{contextClassLoader} :
          new ClassLoader[] {};

    }
  }

  /**
   * Gets the class loader of this library.
   * {@code Reflections.class.getClassLoader()}.
   *
   * @return the static library class loader, may be null
   */
  public static ClassLoader staticClassLoader() {
    return Reflections.class.getClassLoader();
  }


  /**
   * Gets the current thread context class loader.
   * {@code Thread.currentThread().getContextClassLoader()}.
   *
   * @return the context class loader, may be null
   */
  public static ClassLoader contextClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  //http://michaelscharf.blogspot.co.il/2006/11/javaneturlequals-and-hashcode-make.html
  private static Collection<URL> distinctUrls(Collection<URL> urls) {
    Map<String, URL> distinct = new LinkedHashMap<String, URL> (urls.size());
    for (URL url : urls) {
      distinct.put(url.toExternalForm(), url);
    }
    return distinct.values();
  }
}
