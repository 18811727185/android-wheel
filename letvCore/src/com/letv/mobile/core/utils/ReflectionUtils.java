package com.letv.mobile.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class will find method name in given class and function name.
 * // TODO(qingxia): This function should be tested later
 * @author qingxia
 *
 */
public class ReflectionUtils {
  private static final Map<String, Set<Method>> METHOD_CACHE = new HashMap<String, Set<Method>>();

  private ReflectionUtils() {
  }

  /**
   * Finds methods with the given name on the given class.
   *
   * @param clazz
   *          the class
   * @param name
   *          the method name
   * @return the methods
   */
  public static Set<Method> findMethods(Class<?> clazz, String name) {
    String cacheKey = clazz.getName().concat("::").concat(name);
    if (METHOD_CACHE.containsKey(cacheKey)) {
      return METHOD_CACHE.get(cacheKey);
    }
    Set<Method> methods = new HashSet<Method>();
    for (Method method : clazz.getMethods()) {
      if (method.getName().equals(name)) {
        methods.add(method);
      }
    }
    methods = Collections.unmodifiableSet(methods);
    METHOD_CACHE.put(cacheKey, methods);
    return methods;
  }

  /**
   * Finds methods with the given name on the given class.
   *
   * @param clazz
   *          the class
   * @param name
   *          the method name
   * @return the methods
   * @throws NoSuchMethodException
   */
  public static Method findOnlyMethod(Class<?> clazz, String name)
      throws NoSuchMethodException {
    Set<Method> set = findMethods(clazz, name);
    if (set.size() != 1)
      throw new NoSuchMethodException(name + " zero or not exactly one");
    return set.iterator().next();
  }

  /**
   * Get the object by given class instance and field name.
   * @param field
   * @param classInstance
   * @return
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public static Object getFieldValueSafely(Field field, Object classInstance)
      throws IllegalArgumentException, IllegalAccessException {
    boolean oldAccessibleValue = field.isAccessible();
    field.setAccessible(true);
    Object result = field.get(classInstance);
    field.setAccessible(oldAccessibleValue);
    return result;
  }
}
