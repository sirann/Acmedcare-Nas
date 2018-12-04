package com.acmedcare.nas.exts.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Platform Dependent
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 */
public final class PlatformDependent {

  private static final Logger logger = LoggerFactory.getLogger(SystemPropertyUtil.class);

  /**
   * is android os system
   *
   * @return true/false
   */
  private static boolean isAndroid0() {
    String vmName = SystemPropertyUtil.get("java.vm.name");
    boolean isAndroid = "Dalvik".equals(vmName);
    if (isAndroid) {
      logger.debug("Platform: Android");
    }

    return isAndroid;
  }
}