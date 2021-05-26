package org.ffpy.plugin.coding.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 注释位置
 */
@Getter
@RequiredArgsConstructor
public enum CommentPosition {
    UP("节点上方"),
    DOWN("节点下方"),
    NONE("无"),
    ;
    private final String name;
}
