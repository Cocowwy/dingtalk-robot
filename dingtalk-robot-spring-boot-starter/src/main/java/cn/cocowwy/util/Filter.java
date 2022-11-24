package cn.cocowwy.util;

import cn.cocowwy.config.RobotsProperties;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 过滤器，过滤空，重复以及过滤白名单
 * @author cocowwy.cn
 * @create 2022-05-05-11:45
 */
public interface Filter {
    static List<String> filterDingUserIds(RobotsProperties.Robot robot, List<String> dingUserIds) {
        dingUserIds = dingUserIds.stream().filter(StrUtil::isNotEmpty).distinct().collect(Collectors.toList());

        if (CollectionUtils.isEmpty(robot.getWhitelist())) {
            return dingUserIds;
        }
        return new ArrayList<>(CollectionUtil.intersection(robot.getWhitelist(), dingUserIds));
    }
}
