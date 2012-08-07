package com.bamf.bamfutils.services;

interface IRootService {  
  boolean toggleAppState(boolean state,String pName);
  boolean removeApp(String dLocation,String location);
  boolean isMounted();
  boolean remount(boolean readWrite);
  boolean blockAds(boolean blocked);
  void clearDalvik();
  void clearCache();
  void fixPerms();
  void setScheduler(String scheduler);
  void setVoltage(String file);
  void setKernelValue(String file, String value);
  String getLastBackupDate(String dir);
  boolean copyFile(String backupFrom, String backupTo, boolean isRestore);
}
