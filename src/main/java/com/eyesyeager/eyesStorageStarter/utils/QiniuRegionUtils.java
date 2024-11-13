package com.eyesyeager.eyesStorageStarter.utils;

import com.qiniu.storage.Region;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 七牛云 region 工具类
 * 区域文档见：<a href="https://developer.qiniu.com/kodo/1239/java#5">文件上传</a>
 * @author artonyu
 * @date 2024-11-08 14:52
 */

public class QiniuRegionUtils {
    /**
     * 根据名称获取region
     * @param regionName 机房名称
     * @return Region
     */
    public static Region getRegion(String regionName) {
        Region region = MapContainer.map.get(regionName);
        if (Objects.isNull(region)) {
            throw new IllegalArgumentException("region " + regionName + " is not exist.");
        }
        return region;
    }

    // 利用静态内部类保证线程安全
    private static class MapContainer {
        private static final Map<String, Region> map;

        static {
            // region 信息可参见
            Map<String, Region> tMap = new HashMap<>();
            tMap.put("HuaDong", Region.createWithRegionId("z0")/*华东*/);
            tMap.put("HuaBei", Region.createWithRegionId("z1")/*华北*/);
            tMap.put("HuaNan", Region.createWithRegionId("z2")/*华南*/);
            tMap.put("BeiMei", Region.createWithRegionId("na0")/*北美*/);
            tMap.put("DongNanYa", Region.createWithRegionId("as0")/*东南亚*/);
            map = Collections.unmodifiableMap(tMap);
        }
    }
}
