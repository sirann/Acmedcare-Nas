package com.acmedcare.nas.client.test;

import com.acmedcare.nas.api.NasFileService;
import com.acmedcare.nas.exts.api.NasExtType;
import com.acmedcare.nas.exts.api.NasServiceFactory;
import com.acmedcare.nas.exts.api.properties.NasPropertiesLoader;

/**
 * Nas Exts Test Application
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-03.
 */
public class NasExtsTestApplication {

  public static void main(String[] args) {

    NasPropertiesLoader nasPropertiesLoader =
        NasServiceFactory.getNasPropertiesLoader(NasExtType.QINIU);

    System.out.println(nasPropertiesLoader);
    System.out.println(nasPropertiesLoader.propertyFileName());

    NasFileService nasFileService = NasServiceFactory.getNasFileService(NasExtType.QINIU);
    System.out.println(nasFileService);
  }
}
